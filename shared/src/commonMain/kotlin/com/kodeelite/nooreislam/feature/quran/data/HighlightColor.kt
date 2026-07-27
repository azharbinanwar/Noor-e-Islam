package com.kodeelite.nooreislam.feature.quran.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.kodeelite.nooreislam.config.theme.AppTheme

// preset highlight tints. Persisted by name via the Room Converters.
enum class HighlightColor {
    Green, Amber, Rose, Blue, Purple, Teal
}

// solid identity hue — used for the picker swatches
val HighlightColor.hue: Color
    get() = when (this) {
        HighlightColor.Green -> Color(0xFF22C55E)
        HighlightColor.Amber -> Color(0xFFF59E0B)
        HighlightColor.Rose -> Color(0xFFF43F5E)
        HighlightColor.Blue -> Color(0xFF3B82F6)
        HighlightColor.Purple -> Color(0xFFA855F7)
        HighlightColor.Teal -> Color(0xFF14B8A6)
    }

// theme-aware wash drawn behind the ayah — stronger on dark so it still reads
@Composable
fun HighlightColor.tint(): Color =
    hue.copy(alpha = if (AppTheme.colors.background.luminance() < 0.5f) 0.30f else 0.20f)
