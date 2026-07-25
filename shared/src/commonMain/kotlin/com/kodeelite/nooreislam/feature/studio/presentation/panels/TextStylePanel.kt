package com.kodeelite.nooreislam.feature.studio.presentation.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kodeelite.nooreislam.core.constants.defaults.StudioDefaults
import com.kodeelite.nooreislam.feature.studio.data.StudioConfig
import com.kodeelite.nooreislam.feature.studio.presentation.components.ArtSlider
import com.kodeelite.nooreislam.feature.studio.presentation.components.ColorStripPicker
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.opacity
import com.kodeelite.nooreislam.resources.shadow
import org.jetbrains.compose.resources.stringResource

// Text opacity + shadow + color (suggested colors come from the current bg image's palette).
@Composable
fun TextStylePanel(config: StudioConfig, swatches: List<Color>, onChange: (StudioConfig) -> Unit) {
    Column {
        ArtSlider(
            stringResource(Res.string.opacity),
            config.textColor.alpha * 100f,
            { onChange(config.copy(textColor = config.textColor.copy(alpha = it / 100f))) },
            StudioDefaults.PERCENT_RANGE,
            "${(config.textColor.alpha * 100).toInt()}%"
        )
        ArtSlider(
            stringResource(Res.string.shadow),
            config.textShadowAlpha * 100f,
            { onChange(config.copy(textShadowAlpha = it / 100f)) },
            StudioDefaults.PERCENT_RANGE,
            "${(config.textShadowAlpha * 100).toInt()}%"
        )
        Spacer(Modifier.size(8.dp))
        ColorStripPicker(config.textColor, { onChange(config.copy(textColor = it.copy(alpha = config.textColor.alpha))) }, swatches)
    }
}
