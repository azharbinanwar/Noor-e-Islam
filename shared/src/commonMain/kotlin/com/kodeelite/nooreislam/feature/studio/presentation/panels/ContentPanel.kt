package com.kodeelite.nooreislam.feature.studio.presentation.panels

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
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppSwitch
import com.kodeelite.nooreislam.core.constants.defaults.StudioDefaults
import com.kodeelite.nooreislam.feature.studio.data.StudioConfig
import com.kodeelite.nooreislam.feature.studio.data.SurahPlacement
import com.kodeelite.nooreislam.feature.studio.presentation.components.ArtSlider
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.bismillah
import com.kodeelite.nooreislam.resources.surah_name
import com.kodeelite.nooreislam.resources.trans_size
import com.kodeelite.nooreislam.resources.translation
import com.kodeelite.nooreislam.resources.transliteration_coming_soon
import org.jetbrains.compose.resources.stringResource

// Content toggles: bismillah, translation (+ size), surah name top/bottom, transliteration (placeholder).
@Composable
fun ContentPanel(config: StudioConfig, onChange: (StudioConfig) -> Unit) {
    val colors = AppTheme.colors
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.bismillah), color = colors.onSurface, fontSize = 12.sp, modifier = Modifier.weight(1f))
            AppSwitch(config.showBismillah, { onChange(config.copy(showBismillah = it)) })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.translation), color = colors.onSurface, fontSize = 12.sp, modifier = Modifier.weight(1f))
            AppSwitch(config.showTranslation, { onChange(config.copy(showTranslation = it)) })
        }
        if (config.showTranslation) ArtSlider(
            stringResource(Res.string.trans_size),
            config.translationSize,
            { onChange(config.copy(translationSize = it)) },
            StudioDefaults.TRANSLATION_SIZE_RANGE,
            "${config.translationSize.toInt()}px",
        )
        Text(stringResource(Res.string.surah_name), color = colors.onSurface, fontSize = 12.sp)
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
            Text(
                stringResource(Res.string.transliteration_coming_soon),
                color = colors.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            AppSwitch(false, {})
        }
    }
}
