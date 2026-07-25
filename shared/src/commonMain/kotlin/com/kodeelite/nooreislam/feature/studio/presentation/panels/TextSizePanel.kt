package com.kodeelite.nooreislam.feature.studio.presentation.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.kodeelite.nooreislam.core.constants.defaults.StudioDefaults
import com.kodeelite.nooreislam.feature.studio.data.StudioConfig
import com.kodeelite.nooreislam.feature.studio.presentation.components.ArtSlider
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.line_height
import com.kodeelite.nooreislam.resources.size
import org.jetbrains.compose.resources.stringResource

// Text size + line height.
@Composable
fun TextSizePanel(config: StudioConfig, onChange: (StudioConfig) -> Unit) {
    Column {
        ArtSlider(
            stringResource(Res.string.size),
            config.fontSize,
            { onChange(config.copy(fontSize = it)) },
            StudioDefaults.FONT_SIZE_RANGE,
            "${config.fontSize.toInt()}px"
        )
        ArtSlider(
            stringResource(Res.string.line_height),
            config.lineHeight,
            { onChange(config.copy(lineHeight = it)) },
            StudioDefaults.LINE_HEIGHT_RANGE,
            "${(config.lineHeight * 100).toInt()}%"
        )
    }
}
