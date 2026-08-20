package com.kodeelite.nooreislam.feature.home.presentation

import androidx.compose.ui.geometry.Offset
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.feature.miqat.domain.MiqatTime
import kotlinx.datetime.LocalTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The closed loop both bodies ride, measured in points clockwise from the nadir: 6 is the east horizon,
 * 12 the zenith, 18 the west horizon, 24 back to the bottom. A prayer pins itself to a point and stays
 * there in every month and every city — the clock only decides how fast the gap to the next one is crossed.
 */
const val LOOP_POINTS = 24f

/** Half a turn apart, so the two bodies can never meet. */
const val MOON_LEAD = LOOP_POINTS / 2f

/**
 * Where each prayer sits on the loop. Eyeballed against the ridge in the sky lab, not calculated:
 * 6.1 is where the loop leaves the eastern peaks, 18 is where it meets the western ones.
 */
val LOOP_ANCHORS = listOf(
    Miqat.Fajr to 5f,
    Miqat.Sunrise to 6.1f,
    Miqat.Dhuhr to 12f,
    Miqat.Asr to 15f,
    Miqat.Maghrib to 18f,
    Miqat.Isha to 19f,
    Miqat.Midnight to 24f,
    Miqat.LastThird to 26f,
)

/**
 * The sun's place on the loop at [now]. The day is one full turn from Fajr back to Fajr, so the value
 * runs past [LOOP_POINTS] and wraps cleanly — the motion never restarts, which is what kept the old arc
 * jumping at its ends.
 */
fun sunPointAt(now: LocalTime, times: List<MiqatTime>): Float {
    fun minuteOf(m: Miqat): Int? =
        times.firstOrNull { it.miqat == m }?.at?.time?.let { it.hour * 60 + it.minute }

    val fajr = minuteOf(Miqat.Fajr) ?: (5 * 60)
    val pts = LOOP_ANCHORS
        .mapNotNull { (m, p) -> minuteOf(m)?.let { (if (it < fajr) it + 1440 else it) to p } }
        .sortedBy { it.first } + ((fajr + 1440) to (LOOP_ANCHORS.first().second + LOOP_POINTS))

    val n = now.hour * 60 + now.minute
    val nAdj = if (n >= fajr) n else n + 1440
    val i = pts.indexOfLast { it.first <= nAdj }.coerceIn(0, pts.size - 2)
    val (aMin, aPos) = pts[i]
    val (bMin, bPos) = pts[i + 1]
    return aPos + (bPos - aPos) * ((nAdj - aMin).toFloat() / (bMin - aMin).coerceAtLeast(1))
}

/** Where the ridge cuts the loop, measured in the lab. Everything else about visibility comes from these. */
const val RIDGE_EAST = 6.1f
const val RIDGE_WEST = 18f

/** How far under the ridge the splash still carries. Fajr and Isha sit inside this, which is the point. */
private const val SPLASH_SPAN = 2f

/** The disc exists between the peaks and nowhere else, so Fajr and Maghrib never show one. */
fun discShows(point: Float): Boolean = point.mod(LOOP_POINTS).let { it > RIDGE_EAST && it < RIDGE_WEST }

/** Splash strength: full while the body is up, spent [SPLASH_SPAN] points under the ridge. */
fun splashAt(point: Float): Float {
    if (discShows(point)) return 1f
    val p = point.mod(LOOP_POINTS)
    val under = minOf((RIDGE_EAST - p).mod(LOOP_POINTS), (p - RIDGE_WEST).mod(LOOP_POINTS))
    return (1f - under / SPLASH_SPAN).coerceIn(0f, 1f)
}

/** Screen position of a loop [point] on the ellipse the frame squashes the loop into. */
fun loopOffset(point: Float, cx: Float, cy: Float, rx: Float, ry: Float): Offset {
    val t = point / LOOP_POINTS * 2.0 * PI
    return Offset(cx - rx * sin(t).toFloat(), cy + ry * cos(t).toFloat())
}
