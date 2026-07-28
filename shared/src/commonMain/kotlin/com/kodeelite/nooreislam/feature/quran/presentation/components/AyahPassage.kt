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
import androidx.compose.ui.text.AnnotatedString
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
import com.kodeelite.nooreislam.feature.quran.data.BookmarksStore
import com.kodeelite.nooreislam.feature.quran.data.HighlightColor
import com.kodeelite.nooreislam.feature.quran.data.HighlightsStore
import com.kodeelite.nooreislam.feature.quran.data.NotesStore
import com.kodeelite.nooreislam.feature.quran.data.QuranStore
import com.kodeelite.nooreislam.feature.quran.data.QuranSymbols
import com.kodeelite.nooreislam.feature.quran.data.tint
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.tanzil_hafs
import org.jetbrains.compose.resources.Font
import org.koin.compose.koinInject

private const val BISMALAH_WORD_COUNT = 4
private const val RTL_MARKER = "‏"

private const val SELECTION_ALPHA = 0.10f

// Text plus each ayah's selection range and the note glyph's own tiny range (a
// separate tap target from the ayah's full selection range)
private data class AyahPassageData(
    val text: AnnotatedString,
    val ranges: List<Pair<Ayah, IntRange>>,
    val noteIconRanges: Map<String, IntRange>
)

/**
 * AyahPassage: renders a group of verses (ruku) as a single flowing paragraph.
 *
 * Highlights/selection are drawn via SpanStyle(background = ...), not a custom drawBehind
 * path — that's deliberate. Android paints justified (TextAlign.Justify) text and its
 * SpanStyle backgrounds in the same pass, so the highlight always lands exactly where the
 * justified glyphs actually render. A reconstructed-after-the-fact rect (built from
 * TextLayoutResult queries) doesn't have that guarantee on Android and drifted on wrapped,
 * justified lines — see AyahPassageV2.kt for that attempt and why it's parked.
 */
@Composable
fun AyahPassage(
    ayahs: List<Ayah>,
    selected: Ayah?,
    onSelect: (Ayah) -> Unit,
    onLongSelect: (Ayah) -> Unit,
    onNoteTap: (Ayah) -> Unit = {}
) {
    val colors = AppTheme.colors
    val store = koinInject<QuranStore>()
    val highlightStore = koinInject<HighlightsStore>()
    val bookmarksStore = koinInject<BookmarksStore>()
    val notesStore = koinInject<NotesStore>()

    val fontSize by store.fontSize.collectAsState()
    val script by store.font.collectAsState()
    val bodyFont = FontFamily(Font(script.res))
    val markerFont = FontFamily(Font(Res.font.tanzil_hafs))

    val highlights by highlightStore.colors.collectAsState()
    val bookmarks by bookmarksStore.keys.collectAsState()
    val noteMap by notesStore.noteMap.collectAsState()

    val tints = remember(colors.background) {
        HighlightColor.entries.associateWith { it.tint(colors.background) }
    }

    // Build the text and its ranges atomically so a tap always matches what's on screen
    val passageData = remember(ayahs, highlights, bookmarks, noteMap, bodyFont, markerFont, colors, selected, fontSize) {
        val ranges = mutableListOf<Pair<Ayah, IntRange>>()
        val noteIconRanges = mutableMapOf<String, IntRange>()
        val annotatedString = buildAnnotatedString {
            ayahs.forEach { ayah ->
                val start = length
                val key = "${ayah.surah}:${ayah.ayah}"

                val hlColor = highlights[key]?.let { tints[it] }
                val hit = when {
                    selected == ayah -> colors.primary.copy(alpha = SELECTION_ALPHA)
                    hlColor != null -> hlColor
                    else -> Color.Transparent
                }

                append(RTL_MARKER)

                if (key in bookmarks) {
                    withStyle(SpanStyle(fontFamily = bodyFont, color = colors.primary, background = hit)) { append(QuranSymbols.BOOKMARK + " ") }
                }
                if (key in noteMap) {
                    val noteStart = length
                    withStyle(SpanStyle(fontFamily = bodyFont, color = colors.primary, background = hit)) { append(QuranSymbols.NOTE) }
                    noteIconRanges[key] = noteStart until length
                    withStyle(SpanStyle(background = hit)) { append(" ") }
                }

                withStyle(SpanStyle(fontFamily = bodyFont, color = colors.onBackground, background = hit)) {
                    append(ayahText(ayah))
                }

                withStyle(SpanStyle(fontFamily = markerFont, color = colors.primary, background = hit)) {
                    append(" " + QuranSymbols.ayahNumber(ayah.ayah.toArabicIndic()))
                }

                if (ayah.sajda != null) {
                    withStyle(SpanStyle(fontFamily = markerFont, color = colors.primary, background = hit)) {
                        append(" " + QuranSymbols.SAJDA)
                    }
                }

                append(RTL_MARKER)
                append("   ")

                ranges.add(ayah to (start until length))
            }
        }
        AyahPassageData(annotatedString, ranges, noteIconRanges)
    }

    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = passageData.text,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)
            .pointerInput(passageData) {
                detectTapGestures(
                    onTap = { pos ->
                        layout?.getOffsetForPosition(pos)?.let { idx ->
                            passageData.ranges.firstOrNull { idx in it.second }?.let { (ayah, _) ->
                                val noteRange = passageData.noteIconRanges["${ayah.surah}:${ayah.ayah}"]
                                if (noteRange != null && idx in noteRange) onNoteTap(ayah) else onSelect(ayah)
                            }
                        }
                    },
                    onLongPress = { pos ->
                        layout?.getOffsetForPosition(pos)?.let { idx ->
                            passageData.ranges.firstOrNull { idx in it.second }?.let { onLongSelect(it.first) }
                        }
                    }
                )
            },
        fontSize = fontSize.sp,
        lineHeight = (fontSize * 1.9f).sp,
        textAlign = TextAlign.Justify,
        onTextLayout = { layout = it }
    )
}

// Drops embedded basmalah on non-Fatiha surah starts
private fun ayahText(ayah: Ayah): String {
    val words = ayah.text.split(' ').filter { it.isNotBlank() }
    val body = if (ayah.ayah == 1 && ayah.surah != 1 && words.size > BISMALAH_WORD_COUNT && words.first().startsWith("بِسْم"))
        words.drop(BISMALAH_WORD_COUNT) else words
    return body.joinToString(" ")
}
