package com.example.miqatapp.feature.studio.presentation.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppSwitch
import com.example.miqatapp.core.constants.defaults.StudioDefaults
import com.example.miqatapp.feature.studio.data.StudioConfig
import com.example.miqatapp.feature.studio.data.SurahPlacement
import com.example.miqatapp.feature.studio.presentation.components.ArtSlider

// Content toggles: bismillah, translation (+ size), surah name top/bottom, transliteration (placeholder).
@Composable
fun ContentPanel(config: StudioConfig, onChange: (StudioConfig) -> Unit) {
    val colors = AppTheme.colors
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Bismillah", color = colors.onSurface, fontSize = 12.sp, modifier = Modifier.weight(1f))
            AppSwitch(config.showBismillah, { onChange(config.copy(showBismillah = it)) })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Translation", color = colors.onSurface, fontSize = 12.sp, modifier = Modifier.weight(1f))
            AppSwitch(config.showTranslation, { onChange(config.copy(showTranslation = it)) })
        }
        if (config.showTranslation) ArtSlider(
            "Trans Size",
            config.translationSize,
            { onChange(config.copy(translationSize = it)) },
            StudioDefaults.TRANSLATION_SIZE_RANGE,
            "${config.translationSize.toInt()}px",
        )
        Text("Surah name", color = colors.onSurface, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SurahPlacement.entries.forEach { p ->
                val active = config.surahPlacement == p
                Box(
                    Modifier.height(36.dp).weight(1f).clip(RoundedCornerShape(10.dp))
                        .background(if (active) colors.primary else colors.onSurface.copy(alpha = 0.05f))
                        .clickable { onChange(config.copy(surahPlacement = p)) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(p.label, color = if (active) colors.onPrimary else colors.onSurface, fontSize = 11.sp)
                }
            }
        }
        // placeholder — needs a translation/transliteration source
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Transliteration (coming soon)", color = colors.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.weight(1f))
            AppSwitch(false, {})
        }
    }
}
