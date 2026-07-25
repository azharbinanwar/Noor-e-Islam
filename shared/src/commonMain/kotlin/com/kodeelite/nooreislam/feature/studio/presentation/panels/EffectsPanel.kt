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
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.blur
import com.kodeelite.nooreislam.resources.overlay
import com.kodeelite.nooreislam.resources.shadow_spread
import com.kodeelite.nooreislam.resources.vignette
import org.jetbrains.compose.resources.stringResource

// Pattern + blur / vignette / shadow-spread / overlay.
@Composable
fun EffectsPanel(config: StudioConfig, onChange: (StudioConfig) -> Unit) {
    Column {
        PatternPicker(config.pattern) { onChange(config.copy(pattern = it)) }
        Spacer(Modifier.size(8.dp))
        ArtSlider(
            stringResource(Res.string.blur),
            config.blurRadius,
            { onChange(config.copy(blurRadius = it)) },
            StudioDefaults.BLUR_RANGE,
            "${config.blurRadius.toInt()}px"
        )
        ArtSlider(
            stringResource(Res.string.vignette),
            config.vignetteIntensity * 100f,
            { onChange(config.copy(vignetteIntensity = it / 100f)) },
            StudioDefaults.PERCENT_RANGE,
            "${(config.vignetteIntensity * 100).toInt()}%"
        )
        ArtSlider(
            stringResource(Res.string.shadow_spread),
            config.vignetteSpread * 100f,
            { onChange(config.copy(vignetteSpread = it / 100f)) },
            StudioDefaults.PERCENT_RANGE,
            "${(config.vignetteSpread * 100).toInt()}%"
        )
        ArtSlider(
            stringResource(Res.string.overlay),
            config.overlayAlpha * 100f,
            { onChange(config.copy(overlayAlpha = it / 100f)) },
            StudioDefaults.PERCENT_RANGE,
            "${(config.overlayAlpha * 100).toInt()}%"
        )
    }
}
