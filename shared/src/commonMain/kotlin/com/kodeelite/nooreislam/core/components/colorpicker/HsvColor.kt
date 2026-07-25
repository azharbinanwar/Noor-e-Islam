package com.kodeelite.nooreislam.core.components.colorpicker

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

/**
 * HSV is kept as the source of truth while a picker is open: Color -> HSV is lossy at the edges
 * (drag value to 0 and the hue would be lost), so we hold HSV and only emit [toColor] outward.
 */
data class HsvColor(
    val hue: Float,          // 0..360
    val saturation: Float,   // 0..1
    val value: Float,        // 0..1
    val alpha: Float = 1f,   // 0..1
) {
    fun toColor(): Color = Color.hsv(
        hue.coerceIn(0f, 360f),
        saturation.coerceIn(0f, 1f),
        value.coerceIn(0f, 1f),
        alpha.coerceIn(0f, 1f),
    )

    companion object {
        fun from(color: Color): HsvColor {
            val r = color.red;
            val g = color.green;
            val b = color.blue
            val max = maxOf(r, g, b);
            val min = minOf(r, g, b)
            val d = max - min
            val h = when {
                d == 0f -> 0f
                max == r -> 60f * (((g - b) / d) % 6f)
                max == g -> 60f * (((b - r) / d) + 2f)
                else -> 60f * (((r - g) / d) + 4f)
            }.let { if (it < 0f) it + 360f else it }
            val s = if (max == 0f) 0f else d / max
            return HsvColor(h, s, max, color.alpha)
        }
    }
}

fun Color.toHex(): String {
    fun ch(f: Float) = (f * 255).roundToInt().coerceIn(0, 255).toString(16).padStart(2, '0').uppercase()
    return "#" + ch(red) + ch(green) + ch(blue)
}
