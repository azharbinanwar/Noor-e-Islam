package com.kodeelite.nooreislam.feature.studio.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.kodeelite.nooreislam.core.constants.defaults.QuranDefaults
import com.kodeelite.nooreislam.core.constants.defaults.StudioDefaults
import com.kodeelite.nooreislam.feature.quran.data.Ayah
import com.kodeelite.nooreislam.feature.quran.data.QuranFont
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
    val fontFamily: QuranFont = QuranDefaults.FONT,
    val fontSize: Float = StudioDefaults.FONT_SIZE,
    @Contextual val textColor: Color = StudioDefaults.TEXT_COLOR,
    @Contextual val textAlign: TextAlign = StudioDefaults.TEXT_ALIGN,
    val lineHeight: Float = StudioDefaults.LINE_HEIGHT,
    val textOutlineSize: Float = StudioDefaults.TEXT_OUTLINE,
    val textShadowAlpha: Float = StudioDefaults.TEXT_SHADOW_ALPHA,
    val autoContrast: Boolean = StudioDefaults.AUTO_CONTRAST,

    // Content Customization
    val emphasizedWords: Set<Int> = emptySet(),
    @Contextual val emphasisColor: Color = StudioDefaults.EMPHASIS_COLOR,

    // Header & Content
    val showBismillah: Boolean = StudioDefaults.SHOW_BISMILLAH,
    val showTranslation: Boolean = StudioDefaults.SHOW_TRANSLATION,
    val translationText: String = StudioDefaults.TRANSLATION_TEXT,
    val translationSize: Float = StudioDefaults.TRANSLATION_SIZE,
    val surahPlacement: SurahPlacement = SurahPlacement.DEFAULT,   // surah name + ref block: top, bottom, or off

    // Background
    @Contextual val bgColor: Color = StudioDefaults.BG_COLOR,
    val bgImageUrl: String? = ImageStore.default?.url,
    val bgImageScale: Float = StudioDefaults.IMAGE_SCALE,   // pinch-zoom the photo (manual crop)
    val bgImageOffsetX: Float = StudioDefaults.OFFSET,      // pan the photo within the frame
    val bgImageOffsetY: Float = StudioDefaults.OFFSET,
    val bgGradient: StudioGradient? = null,
    val isDuotone: Boolean = StudioDefaults.IS_DUOTONE,
    val pattern: CanvasPattern = CanvasPattern.DEFAULT,
    val blurRadius: Float = StudioDefaults.BLUR,
    val vignetteIntensity: Float = StudioDefaults.VIGNETTE,
    val vignetteSpread: Float = StudioDefaults.VIGNETTE_SPREAD,
    val overlayAlpha: Float = StudioDefaults.OVERLAY_ALPHA,

    // Container (Card)
    @Contextual val cardColor: Color = StudioDefaults.CARD_COLOR,
    val cardCornerRadius: Float = StudioDefaults.CARD_RADIUS,
    val cardPadding: Float = StudioDefaults.CARD_PADDING,
    val cardScale: Float = StudioDefaults.CARD_SCALE,
    val cardOffsetX: Float = StudioDefaults.OFFSET,
    val cardOffsetY: Float = StudioDefaults.OFFSET,

    // Stickers
    val stickers: List<PlacedSticker> = emptyList(),

    // Branding & Dates
    val showHijri: Boolean = StudioDefaults.SHOW_HIJRI,
    val showGregorian: Boolean = StudioDefaults.SHOW_GREGORIAN,
    val showWatermark: Boolean = StudioDefaults.SHOW_WATERMARK,
    val watermarkCorner: LogoCorner = LogoCorner.DEFAULT,

    // Canvas
    val aspectRatio: StudioAspectRatio = StudioAspectRatio.DEFAULT
) {
    companion object {
        // the ship-default config (every field from StudioDefaults) for a given ayah set — reset / new design
        fun default(ayahs: List<Ayah>) = StudioConfig(ayahs = ayahs)
    }
}
