package com.kodeelite.nooreislam.feature.studio.presentation.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppSwitch
import com.kodeelite.nooreislam.feature.studio.data.StudioConfig

// Date toggles: Hijri and Gregorian.
@Composable
fun DatesPanel(config: StudioConfig, onChange: (StudioConfig) -> Unit) {
    val colors = AppTheme.colors
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Hijri Date", color = colors.onSurface, fontSize = 12.sp, modifier = Modifier.weight(1f))
            AppSwitch(config.showHijri, { onChange(config.copy(showHijri = it)) })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Gregorian", color = colors.onSurface, fontSize = 12.sp, modifier = Modifier.weight(1f))
            AppSwitch(config.showGregorian, { onChange(config.copy(showGregorian = it)) })
        }
    }
}
