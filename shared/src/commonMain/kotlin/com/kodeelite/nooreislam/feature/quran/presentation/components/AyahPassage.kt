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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

// one ruku as a single flowing paragraph; tap a verse to select it, highlight follows only that verse's glyphs
@Composable
fun AyahPassage(ayahs: List<Ayah>, selected: Ayah?, pressed: Ayah?, onSelect: (Ayah) -> Unit, onLongSelect: (Ayah) -> Unit) {
    val colors = AppTheme.colors
    val store = koinInject<QuranStore>()
    val highlightStore = koinInject<HighlightsStore>()
    val bookmarksStore = koinInject<BookmarksStore>()
    val notesStore = koinInject<NotesStore>()

    val fontSize by store.fontSize.collectAsState()
    val script by store.font.collectAsState()
    val bodyFont = FontFamily(Font(script.res))
    val markerFont = FontFamily(Font(Res.font.tanzil_hafs)) // ornate number + ruku/sajda glyphs

    val highlightColors by highlightStore.colors.collectAsState()
    val bookmarkKeys by bookmarksStore.keys.collectAsState()
    val notes by notesStore.notes.collectAsState()
    val tints = remember(colors.background) { HighlightColor.entries.associateWith { it.tint(colors.background) } } // resolve theme-aware tints once

    // char range each verse occupies, so a tap can map back to its ayah
    val ranges = remember(ayahs) { ArrayList<Pair<Ayah, IntRange>>() }
    val text = buildAnnotatedString {
        ranges.clear()
        ayahs.forEach { ayah ->
            val start = length
            val key = "${ayah.surah}:${ayah.ayah}"
            val isBookmarked = key in bookmarkKeys
            val hasNote = notes.any { it.surah == ayah.surah && it.ayah == ayah.ayah }

            // Highlight: Visuals moved to drawBehind (Background removed from SpanStyle)
            withStyle(SpanStyle(fontFamily = bodyFont, color = colors.onBackground)) {
                append(ayahText(ayah))
            }
            append(" ")

            withStyle(SpanStyle(fontFamily = markerFont, color = colors.primary)) {
                append(QuranSymbols.ayahNumber(ayah.ayah.toArabicIndic()))
            }

            // Inline Markers: Bookmark 🔖 and Note 📝 symbols
            if (isBookmarked) {
                withStyle(SpanStyle(color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)) { append(" 🔖") }
            }
            if (hasNote) {
                withStyle(SpanStyle(color = colors.primary, fontWeight = FontWeight.Bold)) { append(" 📝") }
            }

            if (ayah.sajda != null) withStyle(SpanStyle(fontFamily = markerFont, color = colors.primary)) {
                append(" " + QuranSymbols.SAJDA)
            }
            ranges.add(ayah to (start until length))
            append("  ")
        }
    }

    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val density = LocalDensity.current
    val padPx = with(density) { 4.dp.toPx() } // Padding around text
    val cornerPx = with(density) { 6.dp.toPx() } // Corner radius

    // Build a single continuous contour path that "hugs" the RTL text blocks
    fun buildHuggingPath(ayah: Ayah?): Path? {
        val l = layout ?: return null
        val target = ayah ?: return null
        val range = ranges.firstOrNull { it.first == target }?.second ?: return null

        val startLine = l.getLineForOffset(range.first)
        val endLine = l.getLineForOffset(range.last)

        return Path().apply {
            val startX = l.getHorizontalPosition(range.first, true)
            val endX = l.getHorizontalPosition(range.last, false)

            // 1. TOP-START JUNCTION (Right-Top in LTR card logic)
            // Move to vertical start to allow corner radius to draw the top-right corner
            moveTo(startX + padPx, l.getLineTop(startLine) + cornerPx)

            // 2. RIGHT SIDE (Descending Trace)
            for (i in startLine..endLine) {
                val lineRight = if (i == startLine) startX else l.getLineRight(i)
                lineTo(lineRight + padPx, l.getLineTop(i))
                lineTo(lineRight + padPx, l.getLineBottom(i))
            }

            // 3. BOTTOM-END JUNCTION (Left-Bottom in LTR card logic)
            // Ensure no duplicate points: move straight to the vertical start of the left side
            lineTo(endX - padPx, l.getLineBottom(endLine))
            lineTo(endX - padPx, l.getLineBottom(endLine) - cornerPx)

            // 4. LEFT SIDE (Ascending Trace)
            for (i in endLine downTo startLine) {
                val lineLeft = if (i == endLine) endX else l.getLineLeft(i)
                // Skip drawing to the exact bottom corner again if we just did it in step 3
                if (i != endLine) lineTo(lineLeft - padPx, l.getLineBottom(i))
                lineTo(lineLeft - padPx, l.getLineTop(i))
            }

            // 5. CLOSE TOP
            lineTo(startX + padPx, l.getLineTop(startLine))
            close()
        }
    }

    val selectionPath = remember(selected, layout) { buildHuggingPath(selected) }
    val pressPath = remember(pressed, layout) { buildHuggingPath(pressed) }
    // Pre-calculate paths for all highlighted ayahs in this passage
    val highlightPaths = remember(ayahs, highlightColors, layout) {
        ayahs.mapNotNull { ayah ->
            val color = highlightColors["${ayah.surah}:${ayah.ayah}"] ?: return@mapNotNull null
            val path = buildHuggingPath(ayah) ?: return@mapNotNull null
            path to (tints[color] ?: Color.Transparent)
        }
    }

    Text(
        text,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)
            .drawBehind {
                val cornerEffect = PathEffect.cornerPathEffect(cornerPx)
                val selectionStroke = Stroke(width = 1.5.dp.toPx(), pathEffect = cornerEffect)
                val selectionFillPaint = Paint().apply {
                    color = colors.primary.copy(alpha = 0.04f)
                    pathEffect = cornerEffect
                }

                // 1. Draw Highlights (Fill only, no border)
                highlightPaths.forEach { (path, tint) ->
                    val paint = Paint().apply {
                        color = tint
                        pathEffect = cornerEffect
                    }
                    drawIntoCanvas { canvas -> canvas.drawPath(path, paint) }
                }

                // 2. Draw Pressed Feedback
                pressPath?.let { path ->
                    val paint = Paint().apply {
                        color = colors.primary.copy(alpha = 0.12f)
                        pathEffect = cornerEffect
                    }
                    drawIntoCanvas { canvas -> canvas.drawPath(path, paint) }
                }

                // 3. Draw Selection "Hugging" Border (Fill + Stroke)
                selectionPath?.let { path ->
                    drawIntoCanvas { canvas -> canvas.drawPath(path, selectionFillPaint) }
                    drawPath(path, color = colors.primary.copy(alpha = 0.5f), style = selectionStroke)
                }
            }
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
