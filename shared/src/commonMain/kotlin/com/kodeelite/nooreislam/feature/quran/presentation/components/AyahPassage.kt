package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.util.toArabicIndic
import com.kodeelite.nooreislam.feature.quran.data.Ayah
import com.kodeelite.nooreislam.feature.quran.data.HighlightColor
import com.kodeelite.nooreislam.feature.quran.data.HighlightRepository
import com.kodeelite.nooreislam.feature.quran.data.QuranStore
import com.kodeelite.nooreislam.feature.quran.data.QuranSymbols
import com.kodeelite.nooreislam.feature.quran.data.tint
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.tanzil_hafs
import org.jetbrains.compose.resources.Font
import org.koin.compose.koinInject

private const val BISMALAH_WORD_COUNT = 4

// one ruku as a single flowing paragraph; tap a verse to select it, highlight follows only that verse's glyphs
@Composable
fun AyahPassage(ayahs: List<Ayah>, selected: Ayah?, pressed: Ayah?, onSelect: (Ayah) -> Unit, onLongSelect: (Ayah) -> Unit) {
    val colors = AppTheme.colors
    val fontSize by QuranStore.fontSize.collectAsState()
    val script by QuranStore.font.collectAsState()
    val bodyFont = FontFamily(Font(script.res))
    val markerFont = FontFamily(Font(Res.font.tanzil_hafs)) // ornate number + ruku/sajda glyphs

    val highlights = koinInject<HighlightRepository>()
    val highlightColors by highlights.colors.collectAsState(emptyMap())
    val tints = HighlightColor.entries.associateWith { it.tint() } // resolve theme-aware tints once

    // char range each verse occupies, so a tap can map back to its ayah
    val ranges = remember(ayahs) { ArrayList<Pair<Ayah, IntRange>>() }
    val text = buildAnnotatedString {
        ranges.clear()
        ayahs.forEach { ayah ->
            val start = length
            // a picked/saved highlight shows its color; otherwise a tap or long-press shows the selection tint
            val hlColor = highlightColors["${ayah.surah}:${ayah.ayah}"]?.let { tints[it] }
            val hit = when {
                hlColor != null -> hlColor
                selected == ayah || pressed == ayah -> colors.primary.copy(alpha = 0.14f)
                else -> Color.Transparent
            }
            withStyle(SpanStyle(fontFamily = bodyFont, color = colors.onBackground, background = hit)) { append(ayahText(ayah)) }
            append(" ")
            withStyle(SpanStyle(fontFamily = markerFont, color = colors.primary, background = hit)) {
                append(QuranSymbols.ayahNumber(ayah.ayah.toArabicIndic()))
            }
            if (ayah.sajda != null) withStyle(SpanStyle(fontFamily = markerFont, color = colors.primary)) { append(" " + QuranSymbols.SAJDA) }
            ranges.add(ayah to (start until length))
            append("  ")
        }
    }

    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    Text(
        text,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)
            .pointerInput(ranges) {
                detectTapGestures(
                    onTap = { pos ->
                        layout?.getOffsetForPosition(pos)?.let { off -> ranges.firstOrNull { off in it.second }?.let { onSelect(it.first) } }
                    },
                    onLongPress = { pos ->
                        layout?.getOffsetForPosition(pos)?.let { off -> ranges.firstOrNull { off in it.second }?.let { onLongSelect(it.first) } }
                    },
                )
            },
        fontSize = fontSize.sp,
        lineHeight = (fontSize * 1.9f).sp,
        textAlign = TextAlign.Justify,
        onTextLayout = { layout = it },
    )
}

// ayah display text; the embedded basmalah is dropped on non-Fatiha surah starts (shown separately)
private fun ayahText(ayah: Ayah): String {
    val words = ayah.text.split(' ').filter { it.isNotBlank() }
    val body = if (ayah.ayah == 1 && ayah.surah != 1 && words.size > BISMALAH_WORD_COUNT && words.first().startsWith("بِسْم"))
        words.drop(BISMALAH_WORD_COUNT) else words
    return body.joinToString(" ")
}
