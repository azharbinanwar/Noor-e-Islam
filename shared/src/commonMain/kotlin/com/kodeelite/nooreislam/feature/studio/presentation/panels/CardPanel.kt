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

// Card opacity + corner radius + color (suggested colors from the bg image's palette).
@Composable
fun CardPanel(config: StudioConfig, swatches: List<Color>, onChange: (StudioConfig) -> Unit) {
    Column {
        ArtSlider(
            "Opacity",
            config.cardColor.alpha * 100f,
            { onChange(config.copy(cardColor = config.cardColor.copy(alpha = it / 100f))) },
            StudioDefaults.PERCENT_RANGE,
            "${(config.cardColor.alpha * 100).toInt()}%"
        )
        ArtSlider(
            "Radius",
            config.cardCornerRadius,
            { onChange(config.copy(cardCornerRadius = it)) },
            StudioDefaults.CARD_RADIUS_RANGE,
            "${config.cardCornerRadius.toInt()}px"
        )
        Spacer(Modifier.size(8.dp))
        ColorStripPicker(config.cardColor.copy(alpha = 1f), { onChange(config.copy(cardColor = it.copy(alpha = config.cardColor.alpha))) }, swatches)
    }
}
