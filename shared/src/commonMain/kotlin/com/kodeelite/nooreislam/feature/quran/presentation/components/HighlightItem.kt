package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppTile
import com.kodeelite.nooreislam.core.components.TilePosition
import com.kodeelite.nooreislam.core.constants.defaults.QuranDefaults
import com.kodeelite.nooreislam.core.util.toSurahKey
import com.kodeelite.nooreislam.feature.quran.data.Highlight
import com.kodeelite.nooreislam.feature.quran.data.hue
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.quran_surah_name
import org.jetbrains.compose.resources.Font

// a saved highlight item — leading dot = highlight color; ayah in the tanzil font, right-aligned, max 3 lines; surah glyph trailing
@Composable
fun HighlightItem(highlight: Highlight, text: String, position: TilePosition, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val hue = highlight.color.hue
    AppTile(
        title = "Surah ${highlight.surah} · Ayah ${highlight.ayah}",
        subtitle = text,
        subtitleFont = FontFamily(Font(QuranDefaults.FONT.res)),
        subtitleAlign = TextAlign.Right,
        subtitleMaxLines = 3,
        leading = {
            Box(Modifier.size(38.dp).clip(CircleShape).background(hue.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Box(Modifier.size(14.dp).clip(CircleShape).background(hue))
            }
        },
        trailing = {
            Text(
                highlight.surah.toSurahKey(),
                fontFamily = FontFamily(Font(Res.font.quran_surah_name)),
                color = colors.primary,
                fontSize = 28.sp
            )
        },
        position = position,
        onClick = onClick,
    )
}
