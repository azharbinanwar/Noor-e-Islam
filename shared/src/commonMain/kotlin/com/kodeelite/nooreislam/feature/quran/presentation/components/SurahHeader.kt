package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.util.toSurahKey
import com.kodeelite.nooreislam.feature.quran.data.QuranStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.quran_surah_name
import org.jetbrains.compose.resources.Font

import org.koin.compose.koinInject

private const val SURAH_HEADER_MAX_SP = 60f // cap so the ornate name never overflows at large reading sizes

// ornate surah name at a surah start
@Composable
fun SurahHeader(surah: Int) {
    val store = koinInject<QuranStore>()
    val fontSize by store.fontSize.collectAsState()
    Text(
        surah.toSurahKey(), Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 6.dp),
        fontFamily = FontFamily(Font(Res.font.quran_surah_name)), fontSize = (fontSize * 2.5f).coerceAtMost(SURAH_HEADER_MAX_SP).sp,
        color = AppTheme.colors.primary, textAlign = TextAlign.Center, maxLines = 1,
    )
}
