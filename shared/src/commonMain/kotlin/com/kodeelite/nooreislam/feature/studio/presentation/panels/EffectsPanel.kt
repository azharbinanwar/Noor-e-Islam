package com.kodeelite.nooreislam.feature.studio.presentation.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kodeelite.nooreislam.core.constants.defaults.StudioDefaults
import com.kodeelite.nooreislam.feature.studio.data.StudioConfig
import com.kodeelite.nooreislam.feature.studio.presentation.components.ArtSlider
import com.kodeelite.nooreislam.feature.studio.presentation.components.PatternPicker

// Pattern + blur / vignette / shadow-spread / overlay.
@Composable
fun EffectsPanel(config: StudioConfig, onChange: (StudioConfig) -> Unit) {
    Column {
        PatternPicker(config.pattern) { onChange(config.copy(pattern = it)) }
        Spacer(Modifier.size(8.dp))
        ArtSlider("Blur", config.blurRadius, { onChange(config.copy(blurRadius = it)) }, StudioDefaults.BLUR_RANGE, "${config.blurRadius.toInt()}px")
        ArtSlider(
            "Vignette",
            config.vignetteIntensity * 100f,
            { onChange(config.copy(vignetteIntensity = it / 100f)) },
            StudioDefaults.PERCENT_RANGE,
            "${(config.vignetteIntensity * 100).toInt()}%"
        )
        ArtSlider(
            "Shadow Spread",
            config.vignetteSpread * 100f,
            { onChange(config.copy(vignetteSpread = it / 100f)) },
            StudioDefaults.PERCENT_RANGE,
            "${(config.vignetteSpread * 100).toInt()}%"
        )
        ArtSlider(
            "Overlay",
            config.overlayAlpha * 100f,
            { onChange(config.copy(overlayAlpha = it / 100f)) },
            StudioDefaults.PERCENT_RANGE,
            "${(config.overlayAlpha * 100).toInt()}%"
        )
    }
}
