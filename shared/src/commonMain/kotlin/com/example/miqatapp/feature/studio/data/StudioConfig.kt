package com.example.miqatapp.feature.studio.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.example.miqatapp.core.constants.defaults.StudioDefaults
import com.example.miqatapp.feature.quran.data.Ayah
import com.example.miqatapp.feature.quran.data.QuranFont
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class StudioGradient(
    val stops: List<@Contextual Color>,                    // gradient stops (render)
    val isRadial: Boolean = false,
    val colors: List<@Contextual Color> = emptyList(),     // palette: accent + tones — good picks (like StudioImage)
    val onColors: List<@Contextual Color> = emptyList(),   // readable colors to sit on this gradient
)

@Serializable
data class PlacedSticker(
    val id: Int,
    val type: String,
    val x: Float,
    val y: Float,
    val scale: Float = 1f,
    val rotation: Float = 0f
)

/**
 * High-end configuration for the Miqat Art Studio.
 */
@Serializable
data class StudioConfig(
    val ayahs: List<Ayah>,   // the app's main model; surah name etc. added later if needed

    // Typography
    val fontFamily: QuranFont = QuranFont.DEFAULT,
    val fontSize: Float = StudioDefaults.FONT_SIZE,
    @Contextual val textColor: Color = Color.White,
    @Contextual val textAlign: TextAlign = TextAlign.Center,
    val lineHeight: Float = StudioDefaults.LINE_HEIGHT,
    val textOutlineSize: Float = StudioDefaults.TEXT_OUTLINE,
    val textShadowAlpha: Float = StudioDefaults.TEXT_SHADOW_ALPHA,
    val autoContrast: Boolean = false,

    // Content Customization
    val emphasizedWords: Set<Int> = emptySet(),
    @Contextual val emphasisColor: Color = StudioDefaults.EMPHASIS_COLOR,

    // Header & Content
    val showBismillah: Boolean = false,
    val showTranslation: Boolean = false,
    val translationText: String = "This is a placeholder for the Ayah translation...",
    val translationSize: Float = StudioDefaults.TRANSLATION_SIZE,
    val surahPlacement: SurahPlacement = SurahPlacement.Bottom,   // surah name + ref block: top, bottom, or off

    // Background
    @Contextual val bgColor: Color = StudioDefaults.BG_COLOR,
    val bgImageUrl: String? = ImageStore.default.url,
    val bgImageScale: Float = 1f,      // pinch-zoom the photo (manual crop)
    val bgImageOffsetX: Float = 0f,    // pan the photo within the frame
    val bgImageOffsetY: Float = 0f,
    val bgGradient: StudioGradient? = null,
    val isDuotone: Boolean = false,
    val pattern: CanvasPattern = CanvasPattern.None,
    val blurRadius: Float = StudioDefaults.BLUR,
    val vignetteIntensity: Float = StudioDefaults.VIGNETTE,
    val vignetteSpread: Float = StudioDefaults.VIGNETTE_SPREAD,
    val overlayAlpha: Float = StudioDefaults.OVERLAY_ALPHA,

    // Container (Card)
    @Contextual val cardColor: Color = StudioDefaults.CARD_COLOR,
    val cardCornerRadius: Float = StudioDefaults.CARD_RADIUS,
    val cardPadding: Float = StudioDefaults.CARD_PADDING,
    val cardScale: Float = StudioDefaults.CARD_SCALE,
    val cardOffsetX: Float = 0f,
    val cardOffsetY: Float = 0f,

    // Stickers
    val stickers: List<PlacedSticker> = emptyList(),

    // Branding & Dates
    val showHijri: Boolean = false,
    val showGregorian: Boolean = false,
    val showWatermark: Boolean = true,
    val watermarkCorner: LogoCorner = LogoCorner.BottomRight,

    // Canvas
    val aspectRatio: StudioAspectRatio = StudioAspectRatio.Full
) {
    companion object {
        // the ship-default config (every field from StudioDefaults) for a given ayah set — reset / new design
        fun default(ayahs: List<Ayah>) = StudioConfig(ayahs = ayahs)
    }
}
