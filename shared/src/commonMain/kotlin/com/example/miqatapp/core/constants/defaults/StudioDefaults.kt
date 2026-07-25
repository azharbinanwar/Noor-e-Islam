package com.example.miqatapp.core.constants.defaults

import androidx.compose.ui.graphics.Color

/**
 * Ship defaults for the share studio — the values a new design starts with (read by `StudioConfig`),
 * the auto initial-font-size ramp, and the undo/save limits. Mirrors [QuranDefaults].
 */
object StudioDefaults {
    // typography
    const val FONT_SIZE = 26f
    const val LINE_HEIGHT = 1.3f
    const val TEXT_OUTLINE = 0f
    const val TEXT_SHADOW_ALPHA = 0f
    val EMPHASIS_COLOR = Color(0xFFFBC02D)

    // auto initial size by ayah length
    const val SHORT_LEN = 100
    const val MEDIUM_LEN = 250
    const val FONT_SHORT = 34f
    const val FONT_MEDIUM = 26f
    const val FONT_LONG = 18f

    // background
    val BG_COLOR = Color.Black
    const val BLUR = 0f
    const val VIGNETTE = 0f
    const val VIGNETTE_SPREAD = 0.5f
    const val OVERLAY_ALPHA = 0.2f

    // card
    val CARD_COLOR = Color.Black.copy(alpha = 0.4f)
    const val CARD_RADIUS = 16f
    const val CARD_PADDING = 20f
    const val CARD_SCALE = 1f

    // translation
    const val TRANSLATION_SIZE = 16f

    // limits
    const val HISTORY_MAX = 20
    const val SAVED_MAX = 15

    // editor slider ranges
    val FONT_SIZE_RANGE = 14f..64f
    val LINE_HEIGHT_RANGE = 0.8f..2.5f
    val PERCENT_RANGE = 0f..100f       // opacity / shadow / vignette / overlay
    val CARD_RADIUS_RANGE = 0f..40f
    val BLUR_RANGE = 0f..50f
    val TRANSLATION_SIZE_RANGE = 10f..30f
}
