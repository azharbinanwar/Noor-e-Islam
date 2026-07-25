package com.kodeelite.nooreislam.feature.studio.data

import androidx.compose.ui.graphics.Color
import com.kodeelite.nooreislam.feature.studio.data.GradientStore.generate
import com.kodeelite.nooreislam.feature.studio.data.GradientStore.presets
import kotlin.random.Random

/**
 * Owns the curated gradient catalog + its color combinations. Each gradient carries a palette
 * (accent + tones as [StudioGradient.colors], a readable [StudioGradient.onColors]) so the text/card
 * pickers show good combinations — exactly like [ImageStore]. The studio store just asks for [presets]
 * or a fresh [generate] batch; generation samples the same curated catalog (never random/strange).
 */
object GradientStore {
    // stops + accent + on-text (the readable color that sits on this gradient)
    private fun preset(stops: List<Color>, accent: Color, onText: Color) =
        StudioGradient(stops = stops, colors = listOf(accent) + stops, onColors = listOf(onText))

    val presets: List<StudioGradient> = listOf(
        preset(listOf(Color(0xFF8A5A3C), Color(0xFF3E2A1E), Color(0xFF1A120C)), accent = Color(0xFFC9A24B), onText = Color(0xFFF3EBD8)),
        preset(listOf(Color(0xFF2B3A4A), Color(0xFF0B111A)), accent = Color(0xFF5C7A8A), onText = Color(0xFFE7ECEF)),
        preset(listOf(Color(0xFF1F6F5C), Color(0xFF123B31), Color(0xFF0A1F1A)), accent = Color(0xFF4E7C63), onText = Color(0xFFEAF1EC)),
        preset(listOf(Color(0xFFD98E5A), Color(0xFF7A3E52), Color(0xFF241827)), accent = Color(0xFFC9A24B), onText = Color(0xFFF5E9D6)),
        preset(listOf(Color(0xFFF4ECD8), Color(0xFFD8CBA8)), accent = Color(0xFF8B5E34), onText = Color(0xFF4A3826)),
        preset(listOf(Color(0xFF22262E), Color(0xFF12151A)), accent = Color(0xFFC9A24B), onText = Color(0xFFE0E0E0)),
        preset(listOf(Color(0xFFB77B6B), Color(0xFF5C3A3A), Color(0xFF231616)), accent = Color(0xFFE0C687), onText = Color(0xFFF3EBD8)),
        preset(listOf(Color(0xFF2C7A6B), Color(0xFF0D211D)), accent = Color(0xFF8FA88C), onText = Color(0xFFC9C5BC)),
        preset(listOf(Color(0xFF7C5A9B), Color(0xFF3A2450), Color(0xFF160C22)), accent = Color(0xFFC9A24B), onText = Color(0xFFEAE3F0)),
        preset(listOf(Color(0xFF3B4C7A), Color(0xFF141A2E)), accent = Color(0xFFE0C687), onText = Color(0xFFDCE2EF)),
        preset(listOf(Color(0xFF2E8B6F), Color(0xFF154234), Color(0xFF081712)), accent = Color(0xFFC9A24B), onText = Color(0xFFE9F3EE)),
        preset(listOf(Color(0xFFB14A3A), Color(0xFF5C1F1A), Color(0xFF210A08)), accent = Color(0xFFE0C687), onText = Color(0xFFF5E6E0)),
        preset(listOf(Color(0xFFE8B860), Color(0xFFB5722E), Color(0xFF4A2C10)), accent = Color(0xFFE8B860), onText = Color(0xFFFFF6E4)),
        preset(listOf(Color(0xFFC9A87A), Color(0xFF8C6B45), Color(0xFF3C2A17)), accent = Color(0xFFC9A87A), onText = Color(0xFFF4ECDC)),
        preset(listOf(Color(0xFF5A3A5C), Color(0xFF160E1A)), accent = Color(0xFFC9A24B), onText = Color(0xFFEDE3EA)),
        preset(listOf(Color(0xFFB7E4D3), Color(0xFF5FA98C), Color(0xFF1F4536)), accent = Color(0xFF5FA98C), onText = Color(0xFF1F4536)),
        preset(listOf(Color(0xFFD68F6E), Color(0xFF8A4A3C), Color(0xFF341714)), accent = Color(0xFFD68F6E), onText = Color(0xFFFBEBE0)),
        preset(listOf(Color(0xFFA8B896), Color(0xFF5E6E4E), Color(0xFF26301E)), accent = Color(0xFFA8B896), onText = Color(0xFFF1F4EA)),
        preset(listOf(Color(0xFF3A3D42), Color(0xFF17181B)), accent = Color(0xFFC9A24B), onText = Color(0xFFEDEDEF)),
        preset(listOf(Color(0xFFF0876A), Color(0xFFC2456B), Color(0xFF4A1730)), accent = Color(0xFFF0876A), onText = Color(0xFFFFF0EA)),
    )

    /**
     * Generates tasteful gradients across the hue wheel (5 per batch by default):
     *  - saturation damped in the acid yellow/green zone, fuller for blue/purple
     *  - hues walk by the golden angle (~137°) so a batch spreads out instead of clustering
     *  - lightness floor/gap so the top never washes out and text always has a dark base
     * Each carries a derived palette (accent from the hue + a light readable on-color, since these are dark).
     */
    fun generate(count: Int = 5, seed: Int): List<StudioGradient> {
        val rng = Random(seed)
        var hueCursor = rng.nextInt(360)
        return List(count) {
            hueCursor = (hueCursor + 137 + rng.nextInt(-15, 16)).mod(360)
            val h = hueCursor
            val h2 = (h + rng.nextInt(-25, 26)).mod(360)

            val baseSat = 0.34f + rng.nextFloat() * 0.24f          // 0.34 - 0.58
            val sat = (baseSat * satMultiplier(h)).coerceIn(0.22f, 0.62f)
            val topL = 0.40f + rng.nextFloat() * 0.12f             // 0.40 - 0.52
            val bottomL = 0.11f + rng.nextFloat() * 0.07f          // 0.11 - 0.18

            val top = Color.hsl(h.toFloat(), sat, topL)
            val bottom = Color.hsl(h2.toFloat(), sat * 0.85f, bottomL)
            val accent = Color.hsl(h.toFloat(), sat, 0.62f)        // brighter tone of the hue for emphasis

            StudioGradient(
                stops = listOf(top, bottom),
                colors = listOf(accent, top, bottom),
                onColors = listOf(Color(0xFFF3EBD8)),              // warm off-white, readable on these dark gradients
            )
        }
    }

    /** Dampens the acid hue zones (yellow/green), lets blue/purple stay richer. */
    private fun satMultiplier(h: Int): Float = when (h) {
        in 45 until 95 -> 0.50f    // yellow / yellow-green
        in 95 until 150 -> 0.65f   // green
        in 150 until 200 -> 0.80f  // teal / cyan
        in 200 until 265 -> 1.00f  // blue
        in 265 until 320 -> 0.88f  // purple / magenta
        else -> 0.82f              // red zone
    }
}
