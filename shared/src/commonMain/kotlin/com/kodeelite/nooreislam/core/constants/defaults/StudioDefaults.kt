package com.kodeelite.nooreislam.core.constants.defaults

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

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
    const val AUTO_CONTRAST = false
    val EMPHASIS_COLOR = Color(0xFFFBC02D)
    val TEXT_COLOR = Color.White
    const val MAX_AYAHS = 10   // a share image, not a page; past this the text is unreadably small

    val TEXT_ALIGN = TextAlign.Center

    // auto initial size by ayah length
    const val SHORT_LEN = 100
    const val MEDIUM_LEN = 250
    const val FONT_SHORT = 34f
    const val FONT_MEDIUM = 26f
    const val FONT_LONG = 18f

    // background
    val BG_COLOR = Color.Black
    const val IS_DUOTONE = false
    const val BLUR = 0f
    const val VIGNETTE = 0f
    const val VIGNETTE_SPREAD = 0.5f
    const val OVERLAY_ALPHA = 0.2f
    const val IMAGE_SCALE = 1f          // no zoom (manual-crop identity)
    const val OFFSET = 0f               // image + card pan: no offset

    // card
    val CARD_COLOR = Color.Black.copy(alpha = 0.4f)
    const val CARD_RADIUS = 16f
    const val CARD_PADDING = 20f
    const val CARD_SCALE = 1f

    // content toggles + translation
    const val SHOW_BISMILLAH = false
    const val SHOW_TRANSLATION = false
    const val TRANSLATION_SIZE = 16f
    const val TRANSLATION_TEXT = "This is a placeholder for the Ayah translation..."

    // dates & watermark
    const val SHOW_HIJRI = false
    const val SHOW_GREGORIAN = false
    const val SHOW_WATERMARK = true

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
