package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppTile
import com.kodeelite.nooreislam.core.components.TilePosition
import com.kodeelite.nooreislam.core.util.toSurahKey
import com.kodeelite.nooreislam.feature.quran.data.Surah
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.quran_surah_name
import org.jetbrains.compose.resources.Font

// one surah tile — pass position to place it in a group, or omit it for a standalone (single) tile
@Composable
fun SurahRow(surah: Surah, position: TilePosition = TilePosition.Single, onLongClick: (() -> Unit)? = null, onClick: () -> Unit) {
    val nameFont = FontFamily(Font(Res.font.quran_surah_name))
    AppTile(
        title = surah.nameTransliterated,
        subtitle = "${surah.nameEnglish} · ${surah.ayahCount} ayahs · ${surah.revelation.label}",
        leading = { NumberBadge(surah.number) },
        trailing = { Text(surah.number.toSurahKey(), fontFamily = nameFont, color = AppTheme.colors.primary, fontSize = 28.sp) },
        position = position,
        onClick = onClick,
        onLongClick = onLongClick,
    )
}
