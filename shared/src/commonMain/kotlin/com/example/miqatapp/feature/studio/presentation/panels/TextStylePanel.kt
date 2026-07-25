package com.example.miqatapp.feature.studio.presentation.panels
import com.example.miqatapp.core.constants.defaults.StudioDefaults

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.miqatapp.feature.studio.data.StudioConfig
import com.example.miqatapp.feature.studio.presentation.components.ArtSlider
import com.example.miqatapp.feature.studio.presentation.components.ColorStripPicker

// Text opacity + shadow + color (suggested colors come from the current bg image's palette).
@Composable
fun TextStylePanel(config: StudioConfig, swatches: List<Color>, onChange: (StudioConfig) -> Unit) {
    Column {
        ArtSlider("Opacity", config.textColor.alpha * 100f, { onChange(config.copy(textColor = config.textColor.copy(alpha = it / 100f))) }, StudioDefaults.PERCENT_RANGE, "${(config.textColor.alpha * 100).toInt()}%")
        ArtSlider("Shadow", config.textShadowAlpha * 100f, { onChange(config.copy(textShadowAlpha = it / 100f)) }, StudioDefaults.PERCENT_RANGE, "${(config.textShadowAlpha * 100).toInt()}%")
        Spacer(Modifier.size(8.dp))
        ColorStripPicker(config.textColor, { onChange(config.copy(textColor = it.copy(alpha = config.textColor.alpha))) }, swatches)
    }
}
