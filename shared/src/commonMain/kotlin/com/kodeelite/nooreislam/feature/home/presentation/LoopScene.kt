package com.kodeelite.nooreislam.feature.home.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.feature.miqat.domain.MiqatTime
import kotlinx.datetime.LocalTime
import kotlin.math.abs

/** Sky colours and night depth as before, with both bodies given as places on the loop. */
data class LoopSky(
    val sky: List<Color>,
    val night: Float,
    val sunPoint: Float,
    val moonPoint: Float,
    val moonFull: Float,
    val moonWaxing: Boolean,
)

fun loopSky(now: LocalTime, times: List<MiqatTime>, hijriDay: Int): LoopSky {
    val palette = skyPalette(now, times)
    val sun = sunPointAt(now, times)
    return LoopSky(palette.sky, palette.night, sun, sun + MOON_LEAD, moonFullness(hijriDay), moonWaxing(hijriDay))
}

/** Which way the terminator leans. The shape is the real one; the lean is ours. */
private const val MOON_TILT = 135f

private val stars = listOf(
    0.10f to 0.22f, 0.20f to 0.46f, 0.34f to 0.16f, 0.48f to 0.36f, 0.60f to 0.24f,
    0.72f to 0.42f, 0.84f to 0.18f, 0.92f to 0.52f, 0.16f to 0.62f, 0.44f to 0.60f,
    0.68f to 0.56f, 0.88f to 0.66f,
)

/**
 * The mountain scene with both bodies riding the loop. Nothing here animates position — the point moves
 * a hair a minute and wraps on its own, so there is no tween to fight and no end to snap at.
 * [showPoints] draws the numbered ruler over the top; it is for the lab, never for Home.
 */
@Composable
fun LoopScene(state: LoopSky, modifier: Modifier = Modifier, showPoints: Boolean = false) {
    val density = LocalDensity.current
    val topInsetPx = WindowInsets.statusBars.getTop(density).toFloat()
    val measurer = rememberTextMeasurer()
    val night by animateFloatAsState(state.night, tween(700), label = "night")
    val c0 by animateColorAsState(state.sky[0], tween(700), label = "c0")
    val c1 by animateColorAsState(state.sky[1], tween(700), label = "c1")
    val c2 by animateColorAsState(state.sky[2], tween(700), label = "c2")
    val glow by rememberInfiniteTransition(label = "glow").animateFloat(
        0.20f, 0.50f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "glow",
    )

    val sunColor = Color(0xFFFFD54F)
    val moonColor = Color(0xFFE8EAF6)
    val baseMtn = lerp(Color(0xFF1A2A3A), Color(0xFF070B16), night)

    Box(modifier.background(Brush.verticalGradient(listOf(c0, c1, c2)))) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val horizonY = h * 0.66f
            val rx = w * 0.38f
            val ry = (horizonY - (topInsetPx + 64.dp.toPx())).coerceAtLeast(1f)
            val cx = w / 2f

            fun at(point: Float) = loopOffset(point, cx, horizonY, rx, ry)

            if (night > 0.05f) {
                stars.forEach { (xf, yf) ->
                    drawCircle(Color.White.copy(alpha = night * 0.7f), 1.6.dp.toPx(), Offset(xf * w, yf * horizonY))
                }
            }

            val moonAt = at(state.moonPoint)
            val moonSplash = splashAt(state.moonPoint)
            // a sliver barely lights the sky, a full moon washes it — brightness rides the month
            val lit = 0.55f + 0.45f * state.moonFull
            if (moonSplash > 0f) {
                drawCircle(moonColor.copy(alpha = 0.18f * moonSplash * lit), (30 + 8 * state.moonFull).dp.toPx(), moonAt)
            }
            if (discShows(state.moonPoint)) {
                val r = 22.dp.toPx()
                // the terminator: an ellipse that flattens to a line at the quarters, subtracted
                // from a crescent and added to a gibbous. Nothing is painted over, so the splash
                // carries on through the dark side
                val ex = r * abs(1f - 2f * state.moonFull)
                val terminator = Path().apply { addOval(Rect(moonAt.x - ex, moonAt.y - r, moonAt.x + ex, moonAt.y + r)) }
                val half = Path().apply {
                    arcTo(Rect(moonAt.x - r, moonAt.y - r, moonAt.x + r, moonAt.y + r), -90f, 180f, true)
                    close()
                }
                val shape = Path().apply {
                    op(half, terminator, if (state.moonFull < 0.5f) PathOperation.Difference else PathOperation.Union)
                }
                rotate(if (state.moonWaxing) MOON_TILT else MOON_TILT + 180f, moonAt) {
                    drawPath(shape, moonColor.copy(alpha = lit))
                }
            }

            val sunAt = at(state.sunPoint)
            val sunSplash = splashAt(state.sunPoint)
            if (sunSplash > 0f) {
                drawCircle(sunColor.copy(alpha = glow * 0.5f * sunSplash), 56.dp.toPx(), sunAt)
            }
            if (discShows(state.sunPoint)) {
                drawCircle(sunColor.copy(alpha = glow), 36.dp.toPx(), sunAt)
                drawCircle(sunColor, 24.dp.toPx(), sunAt)
            }

            val back = lerp(baseMtn, c2, 0.45f)
            val front = lerp(baseMtn, Color.Black, 0.35f)

            drawPath(Path().apply {
                moveTo(0f, h); lineTo(0f, horizonY + h * 0.03f)
                lineTo(w * 0.18f, horizonY - h * 0.04f); lineTo(w * 0.38f, horizonY + h * 0.04f)
                lineTo(w * 0.60f, horizonY - h * 0.05f); lineTo(w * 0.82f, horizonY + h * 0.02f)
                lineTo(w, horizonY - h * 0.02f); lineTo(w, h); close()
            }, back)

            drawPath(Path().apply {
                moveTo(0f, h); lineTo(0f, horizonY + h * 0.09f)
                lineTo(w * 0.25f, horizonY + h * 0.02f); lineTo(w * 0.50f, horizonY + h * 0.10f)
                lineTo(w * 0.72f, horizonY + h * 0.01f); lineTo(w, horizonY + h * 0.07f); lineTo(w, h); close()
            }, baseMtn)

            drawPath(Path().apply {
                moveTo(0f, h); lineTo(0f, horizonY + h * 0.16f)
                lineTo(w * 0.30f, horizonY + h * 0.08f); lineTo(w * 0.55f, horizonY + h * 0.17f)
                lineTo(w * 0.80f, horizonY + h * 0.09f); lineTo(w, horizonY + h * 0.15f); lineTo(w, h); close()
            }, front)

            if (showPoints) {
                for (i in 0 until 96) {
                    drawCircle(Color.White.copy(alpha = 0.18f), 1.2.dp.toPx(), at(i / 96f * LOOP_POINTS))
                }
                for (i in 0 until LOOP_POINTS.toInt()) {
                    val p = at(i.toFloat())
                    drawCircle(Color.White.copy(alpha = 0.8f), 2.5.dp.toPx(), p)
                    val label = measurer.measure(
                        AnnotatedString("$i"),
                        TextStyle(fontSize = 9.sp, color = Color.White),
                    )
                    drawText(label, topLeft = Offset(p.x + 5.dp.toPx(), p.y - label.size.height / 2f))
                }
            }
        }
    }
}
