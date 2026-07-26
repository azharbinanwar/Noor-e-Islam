package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.feature.quran.data.QuranSymbols
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.quran_juz
import org.jetbrains.compose.resources.Font

// the juz font's decorative Quran Kareem glyph ("quran" ligature), centered — shown while the reader loads
@Composable
fun QuranCalligraphy() {
    val colors = AppTheme.colors
    val juz = FontFamily(Font(Res.font.quran_juz))
    Box(Modifier.fillMaxSize().background(colors.background), Alignment.Center) {
        // absoluteOffset (not offset): the glyph nudge is a fixed screen direction, so it doesn't flip in RTL/Arabic
        Text(
            QuranSymbols.QURAN,
            fontFamily = juz,
            fontSize = 120.sp,
            color = colors.primary,
            modifier = Modifier.absoluteOffset(x = (-20).dp, y = 0.dp)
        )
    }
}
