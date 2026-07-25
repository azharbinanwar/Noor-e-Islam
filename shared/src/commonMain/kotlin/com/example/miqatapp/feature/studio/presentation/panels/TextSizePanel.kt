package com.example.miqatapp.feature.studio.presentation.panels
import com.example.miqatapp.core.constants.defaults.StudioDefaults

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.example.miqatapp.feature.studio.data.StudioConfig
import com.example.miqatapp.feature.studio.presentation.components.ArtSlider

// Text size + line height.
@Composable
fun TextSizePanel(config: StudioConfig, onChange: (StudioConfig) -> Unit) {
    Column {
        ArtSlider("Size", config.fontSize, { onChange(config.copy(fontSize = it)) }, StudioDefaults.FONT_SIZE_RANGE, "${config.fontSize.toInt()}px")
        ArtSlider("Line", config.lineHeight, { onChange(config.copy(lineHeight = it)) }, StudioDefaults.LINE_HEIGHT_RANGE, "${(config.lineHeight * 100).toInt()}%")
    }
}
