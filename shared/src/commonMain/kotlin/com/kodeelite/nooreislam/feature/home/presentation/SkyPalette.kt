package com.kodeelite.nooreislam.feature.home.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.util.lerp
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.feature.miqat.domain.MiqatTime
import kotlinx.datetime.LocalTime

/** A fixed anchor look for a prayer: sky palette and night depth. */
private data class Scene(val sky: List<Color>, val night: Float)

private fun sceneFor(p: Miqat): Scene = when (p) {
    Miqat.Fajr -> Scene(listOf(Color(0xFF1A1740), Color(0xFF3B2E63), Color(0xFF7A5A86)), night = 0.70f)
    Miqat.Sunrise -> Scene(listOf(Color(0xFF2A4A8A), Color(0xFFB85C73), Color(0xFFF0A85C)), night = 0.18f)
    Miqat.Dhuhr -> Scene(listOf(Color(0xFF2E83C9), Color(0xFF6FB6E8), Color(0xFFC6E6FB)), night = 0f)
    Miqat.Asr -> Scene(listOf(Color(0xFF3E78B0), Color(0xFF8FB4D8), Color(0xFFE6D6A8)), night = 0.08f)
    Miqat.Maghrib -> Scene(listOf(Color(0xFF2B1E55), Color(0xFF8E3A63), Color(0xFFE8843C)), night = 0.42f)
    Miqat.Isha -> Scene(listOf(Color(0xFF050912), Color(0xFF0E1430), Color(0xFF221A45)), night = 0.95f)
    Miqat.Imsak -> sceneFor(Miqat.Fajr)
    Miqat.Ishraq, Miqat.Zawal -> sceneFor(Miqat.Dhuhr)
    Miqat.Sunset -> sceneFor(Miqat.Maghrib)
    Miqat.Midnight, Miqat.LastThird -> sceneFor(Miqat.Isha)
}

private val DAY_ORDER = listOf(Miqat.Fajr, Miqat.Sunrise, Miqat.Dhuhr, Miqat.Asr, Miqat.Maghrib, Miqat.Isha)

private class AnchorPt(val min: Int, val scene: Scene)
private class Slot(val a: Scene, val aMin: Int, val b: Scene, val bMin: Int)

/** The three gradient stops and how deep the night is, both blended between the surrounding prayers. */
data class SkyPalette(val sky: List<Color>, val night: Float)

fun skyPalette(now: LocalTime, times: List<MiqatTime>): SkyPalette {
    fun minuteOf(m: Miqat): Int? =
        times.firstOrNull { it.miqat == m }?.at?.time?.let { it.hour * 60 + it.minute }

    val n = now.hour * 60 + now.minute
    val anchors = DAY_ORDER.mapNotNull { m -> minuteOf(m)?.let { AnchorPt(it, sceneFor(m)) } }.sortedBy { it.min }
    if (anchors.size < 2) return sceneFor(Miqat.Dhuhr).let { SkyPalette(it.sky, it.night) }

    val first = anchors.first()
    val last = anchors.last()
    val slot = when {
        n < first.min -> Slot(last.scene, last.min - 1440, first.scene, first.min)
        n >= last.min -> Slot(last.scene, last.min, first.scene, first.min + 1440)
        else -> anchors.indexOfLast { it.min <= n }
            .let { Slot(anchors[it].scene, anchors[it].min, anchors[it + 1].scene, anchors[it + 1].min) }
    }
    val p = ((n - slot.aMin).toFloat() / (slot.bMin - slot.aMin)).coerceIn(0f, 1f)
    return SkyPalette(
        listOf(
            lerp(slot.a.sky[0], slot.b.sky[0], p),
            lerp(slot.a.sky[1], slot.b.sky[1], p),
            lerp(slot.a.sky[2], slot.b.sky[2], p),
        ),
        lerp(slot.a.night, slot.b.night, p),
    )
}
