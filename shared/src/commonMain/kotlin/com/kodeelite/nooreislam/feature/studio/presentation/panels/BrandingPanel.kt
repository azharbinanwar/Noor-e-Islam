package com.kodeelite.nooreislam.feature.studio.presentation.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.BoxSelect
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppSwitch
import com.kodeelite.nooreislam.feature.studio.data.LogoCorner
import com.kodeelite.nooreislam.feature.studio.data.StudioConfig

// Watermark toggle + position (corner) picker.
@Composable
fun BrandingPanel(config: StudioConfig, onChange: (StudioConfig) -> Unit) {
    val colors = AppTheme.colors
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Watermark", color = colors.onSurface, fontSize = 12.sp, modifier = Modifier.weight(1f))
            AppSwitch(config.showWatermark, { onChange(config.copy(showWatermark = it)) })
        }
        if (config.showWatermark) {
            Text("Position", color = colors.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LogoCorner.entries.forEach { c ->
                    val isSel = config.watermarkCorner == c
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) colors.primary else colors.onSurface.copy(0.05f))
                            .clickable { onChange(config.copy(watermarkCorner = c)) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Lucide.BoxSelect, null, tint = if (isSel) colors.onPrimary else colors.primary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
