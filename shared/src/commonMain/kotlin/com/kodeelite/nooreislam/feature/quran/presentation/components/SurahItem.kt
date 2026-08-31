package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppTile
import com.kodeelite.nooreislam.core.components.TilePosition
import com.kodeelite.nooreislam.core.locale.tr
import com.kodeelite.nooreislam.core.util.toArabicIndic
import com.kodeelite.nooreislam.core.util.toSurahKey
import com.kodeelite.nooreislam.core.util.toSurahMeaning
import com.kodeelite.nooreislam.feature.quran.data.Surah
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.quran_surah_name
import com.kodeelite.nooreislam.resources.surah_ayah_count
import com.kodeelite.nooreislam.resources.surah_ruku_count
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource

// one surah tile — the calligraphic name (trailing) is the identity; meaning is the title, transliteration demoted below
@Composable
fun SurahItem(surah: Surah, modifier: Modifier = Modifier, position: TilePosition = TilePosition.Single, onLongClick: (() -> Unit)? = null, onClick: () -> Unit) {
    val nameFont = FontFamily(Font(Res.font.quran_surah_name))
    AppTile(
        modifier = modifier,
        // localized meaning from the surah_meanings array — add a language = drop a values-xx array, no code change
        title = surah.number.toSurahMeaning(),
        // each unit localized on its own so the order is set here; Arabic-Indic digits keep the line RTL-safe
        subtitle = listOf(
            stringResource(Res.string.surah_ruku_count, tr(surah.rukuCount.toString(), surah.rukuCount.toArabicIndic())),
            stringResource(Res.string.surah_ayah_count, tr(surah.ayahCount.toString(), surah.ayahCount.toArabicIndic())),
            surah.revelation.label,
        ).joinToString(" · "),
        leading = { NumberBadge(surah.number) },
        trailing = { Text(surah.number.toSurahKey(), fontFamily = nameFont, color = AppTheme.colors.primary, fontSize = 28.sp) },
        position = position,
        onClick = onClick,
        onLongClick = onLongClick,
    )
}
