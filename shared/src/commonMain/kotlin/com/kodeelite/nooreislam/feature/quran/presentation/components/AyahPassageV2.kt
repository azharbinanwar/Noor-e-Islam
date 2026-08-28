package com.kodeelite.nooreislam.feature.quran.presentation.components

// PARKED — not wired into RukuBlock/the reader. Reference only, not compiled into the app flow.
//
// This was the custom drawBehind + per-line-rect "hugging path" rewrite of AyahPassage,
// built to fix a bookmark-icon-vs-BiDi-placeholder selection bug (that part worked — the
// bookmark/note glyphs became plain text instead of InlineTextContent placeholders, and
// that change carried over into the live AyahPassage.kt regardless of what happened here).
//
// What's still broken: on real Android devices, TextAlign.Justify + RTL multi-line ayahs
// produce selection/highlight rects that drift on the left edge of wrapped lines. Three fix
// attempts failed here, including one with zero dependency on any Android layout-query API
// (using the paragraph's own box width for fully-justified lines) — which means the root
// cause isn't understood yet, not just mis-modeled. AyahPassage.kt was reverted to the
// older, simpler SpanStyle(background = ...) highlighting approach, which lets Android's
// own renderer paint the highlight in the same pass as the justification stretch, instead
// of reconstructing it after the fact from measurement queries that don't seem to agree
// with what Android actually painted.
//
// Future plan, if this gets revisited: get real on-device diagnostics first (logged rect
// values next to a screenshot of where the highlight actually lands) before trying a fourth
// fix blind. Don't re-wire this into RukuBlock until that bug is actually understood — the
// payoff over the SpanStyle approach is a continuous "hugging" outline across wrapped lines
// instead of per-line boxes, which is a visual nicety, not a correctness requirement.

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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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
import com.kodeelite.nooreislam.feature.quran.data.QuranScript
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

private const val BISMALAH_WORD_COUNT_V2 = 4

// Text plus each ayah's character range within it, and the note glyph's own tiny
// range (a separate tap target from the ayah's full selection range)
private data class AyahPassageDataV2(
    val text: AnnotatedString,
    val visualRanges: Map<String, IntRange>,
    val noteIconRanges: Map<String, IntRange>
)

/**
 * AyahPassageV2: parked rewrite — see file header. Not used by RukuBlock.
 */
@Composable
fun AyahPassageV2(
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

    // 1. Build the stylized text and record each ayah's character range
    val passageData = remember(ayahs, bookmarks, noteMap, bodyFont, markerFont, colors) {
        val visualRanges = mutableMapOf<String, IntRange>()
        val noteIconRanges = mutableMapOf<String, IntRange>()
        val annotatedString = buildAnnotatedString {
            ayahs.forEach { ayah ->
                val key = "${ayah.surah}:${ayah.ayah}"

                // Wrap whole segment in RTL markers to lock the character offsets in the BiDi engine
                append("‏")
                val visualStart = length

                if (key in bookmarks) {
                    withStyle(SpanStyle(color = colors.primary, fontSize = fontSize.sp * 0.9f)) { append(QuranSymbols.BOOKMARK) }
                    append(" ")
                }
                if (key in noteMap) {
                    val noteStart = length
                    withStyle(SpanStyle(color = colors.primary, fontSize = fontSize.sp * 0.9f)) { append(QuranSymbols.NOTE) }
                    noteIconRanges[key] = noteStart until length
                    append(" ")
                }

                withStyle(SpanStyle(fontFamily = bodyFont, color = colors.onBackground)) {
                    append(ayahTextV2(ayah))
                }
                append(" ")

                withStyle(SpanStyle(fontFamily = markerFont, color = colors.primary)) {
                    append(QuranSymbols.ayahNumber(ayah.ayah.toArabicIndic()))
                }

                if (ayah.sajda != null) {
                    withStyle(SpanStyle(fontFamily = markerFont, color = colors.primary)) {
                        append(" " + QuranSymbols.SAJDA)
                    }
                }

                visualRanges[key] = (visualStart until length)

                append("‏")
                append("   ") // 3 spaces between verses
            }
        }
        AyahPassageDataV2(annotatedString, visualRanges, noteIconRanges)
    }

    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val density = LocalDensity.current
    val padPx = with(density) { 6.dp.toPx() } // Horizontal breathing room
    val cornerPx = with(density) { 8.dp.toPx() } // Softness radius

    // 2. Hit-test geometry: character range -> per-line screen rects.
    // A line the range fully owns (nothing from a neighboring ayah on it) is, by definition
    // of justification, stretched edge-to-edge — so its true rendered bounds are just the
    // paragraph's own box width. No query needed, which matters because on Android neither
    // getPathForRange nor getLineLeft/getLineRight reliably reflects the justified (stretched)
    // position — Android's inter-word justify is a draw-time-only effect that doesn't
    // propagate into any of the layout's measurement APIs. A boundary line the range only
    // partially shares with a neighbor still needs a query, so getHorizontalPosition covers that.
    // The paragraph's own last line is never stretched (ragged, like any justified paragraph),
    // so it's excluded from the box-width shortcut even when fully owned.
    // STATUS: still wrong on real Android devices as of the last test — see file header.
    fun rectsForRange(range: IntRange): List<Rect> {
        val l = layout ?: return emptyList()
        val textLength = l.layoutInput.text.length
        if (range.first < 0 || range.last >= textLength) return emptyList()

        val boxWidth = l.size.width.toFloat()
        val rects = mutableListOf<Rect>()
        val startLine = l.getLineForOffset(range.first)
        val endLine = l.getLineForOffset(range.last)

        for (line in startLine..endLine) {
            val lineStart = l.getLineStart(line)
            val lineEnd = l.getLineEnd(line, visibleEnd = true)
            val s = maxOf(range.first, lineStart)
            val e = minOf(range.last + 1, lineEnd)
            if (s >= e) continue

            val fullyOwned = s == lineStart && e == lineEnd
            val isJustified = line != l.lineCount - 1

            val (left, right) = if (fullyOwned && isJustified) {
                0f to boxWidth
            } else {
                val a = l.getHorizontalPosition(s, usePrimaryDirection = true)
                val b = l.getHorizontalPosition(e, usePrimaryDirection = true)
                minOf(a, b) to maxOf(a, b)
            }

            if (right - left > 0f) {
                rects += Rect(left - padPx, l.getLineTop(line), right + padPx, l.getLineBottom(line))
            }
        }
        return rects
    }

    fun ayahRects(ayah: Ayah?): List<Rect> {
        val range = ayah?.let { passageData.visualRanges["${it.surah}:${it.ayah}"] } ?: return emptyList()
        return rectsForRange(range)
    }

    // Note glyph's own (larger, touch-friendly) hit box, separate from the ayah's full select rect
    fun noteIconRect(ayah: Ayah): Rect? {
        val range = passageData.noteIconRanges["${ayah.surah}:${ayah.ayah}"] ?: return null
        val r = rectsForRange(range).firstOrNull() ?: return null
        return Rect(r.left - padPx, r.top - padPx, r.right + padPx, r.bottom + padPx)
    }

    // Merge an ayah's per-line rects into one hugging outline (right edge down, left edge
    // up), rounded uniformly by cornerPathEffect so wrapped lines join with no seam.
    fun huggingPath(rects: List<Rect>): Path? {
        if (rects.isEmpty()) return null
        return Path().apply {
            rects.forEachIndexed { i, r ->
                if (i == 0) moveTo(r.right, r.top) else lineTo(r.right, r.top)
                lineTo(r.right, r.bottom)
            }
            rects.asReversed().forEach { r ->
                lineTo(r.left, r.bottom)
                lineTo(r.left, r.top)
            }
            close()
        }
    }

    val selectionPath = remember(selected, layout, passageData) { huggingPath(ayahRects(selected)) }
    val highlightPaths = remember(highlights, layout, passageData, tints) {
        ayahs.mapNotNull { ayah ->
            val color = highlights["${ayah.surah}:${ayah.ayah}"] ?: return@mapNotNull null
            val path = huggingPath(ayahRects(ayah)) ?: return@mapNotNull null
            path to (tints[color] ?: Color.Transparent)
        }
    }

    // 3. Render the passage with custom drawing logic
    Text(
        text = passageData.text,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)
            .drawBehind {
                val cornerEffect = PathEffect.cornerPathEffect(cornerPx)

                // Draw persistent highlights
                highlightPaths.forEach { (path, tint) ->
                    val p = Paint().apply { color = tint; pathEffect = cornerEffect }
                    drawIntoCanvas { it.drawPath(path, p) }
                }

                // Draw active selection (outline + focus wash)
                selectionPath?.let { path ->
                    val fill = Paint().apply { color = colors.primary.copy(alpha = 0.05f); pathEffect = cornerEffect }
                    drawIntoCanvas { it.drawPath(path, fill) }
                    drawPath(path, color = colors.primary.copy(alpha = 0.5f), style = Stroke(1.5.dp.toPx(), pathEffect = cornerEffect))
                }
            }
            .pointerInput(passageData) {
                // Hit-test the same rects that get drawn: what you see is what you tap.
                // Note glyph checked first — it's a separate, smaller target inside the ayah's own rect.
                detectTapGestures(
                    onTap = { pos ->
                        val notedAyah = ayahs.firstOrNull { noteIconRect(it)?.contains(pos) == true }
                        if (notedAyah != null) onNoteTap(notedAyah)
                        else ayahs.firstOrNull { a -> ayahRects(a).any { it.contains(pos) } }?.let(onSelect)
                    },
                    onLongPress = { pos ->
                        ayahs.firstOrNull { a -> ayahRects(a).any { it.contains(pos) } }?.let(onLongSelect)
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
private fun ayahTextV2(ayah: Ayah): String {
    val words = ayah.textIn(QuranScript.saved()).split(' ').filter { it.isNotBlank() }
    val body = if (ayah.ayah == 1 && ayah.surah != 1 && words.size > BISMALAH_WORD_COUNT_V2 && words.first().startsWith("بِسْم"))
        words.drop(BISMALAH_WORD_COUNT_V2) else words
    return body.joinToString(" ")
}
