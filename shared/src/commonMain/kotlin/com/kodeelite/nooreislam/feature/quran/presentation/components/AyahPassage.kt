package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
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
import com.kodeelite.nooreislam.feature.quran.data.QuranScript
import com.kodeelite.nooreislam.feature.quran.data.QuranStore
import com.kodeelite.nooreislam.feature.quran.data.QuranSymbols
import com.kodeelite.nooreislam.feature.quran.data.tint
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.tanzil_hafs
import org.jetbrains.compose.resources.Font
import org.koin.compose.koinInject

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
    onNoteTap: (Ayah) -> Unit = {},
    flashTarget: Ayah? = null,
    // an ayah to precisely locate once this passage's layout is known — a ruku is one continuously-
    // flowing Text (deliberately, see file header), so LazyColumn's own scrollToItem can only land on
    // the ruku's top. [onTargetLocated] reports the target line's exact window-space Y so the caller
    // (RukuBlock) can work out its exact pixel offset within the ruku item and scroll there directly —
    // this is measured live off the real TextLayoutResult, so it's correct at any font size.
    targetAyah: Ayah? = null,
    onTargetLocated: (Float) -> Unit = {},
) {
    val colors = AppTheme.colors
    val store = koinInject<QuranStore>()
    val highlightStore = koinInject<HighlightsStore>()
    val bookmarksStore = koinInject<BookmarksStore>()
    val notesStore = koinInject<NotesStore>()

    val fontSize by store.fontSize.collectAsState()
    val autoScrolling by store.autoScrollEnabled.collectAsState()
    val lineHeightRatio by store.lineHeightRatio.collectAsState()
    val font by store.font.collectAsState()
    val script by store.script.collectAsState()
    val justify by store.justifyText.collectAsState()
    val bodyFont = FontFamily(Font(font.res))
    val markerFont = FontFamily(Font(Res.font.tanzil_hafs))

    val highlights by highlightStore.colors.collectAsState()
    val bookmarks by bookmarksStore.keys.collectAsState()
    val noteMap by notesStore.noteMap.collectAsState()

    val tints = remember(colors.background) {
        HighlightColor.entries.associateWith { it.tint(colors.background) }
    }

    // selection and the "just jumped here" flash are the same visual, driven by the same animated
    // alpha — appears fast (near-instant, blink-like), fades out smoothly on either kind of clear
    val highlightedAyah = selected ?: flashTarget
    val highlightAlpha = remember { Animatable(0f) }
    LaunchedEffect(highlightedAyah) {
        val active = highlightedAyah != null && highlightedAyah in ayahs
        highlightAlpha.animateTo(if (active) SELECTION_ALPHA else 0f, tween(500))
    }

    // Build the text and its ranges atomically so a tap always matches what's on screen
    val passageData = remember(ayahs, script, highlights, bookmarks, noteMap, bodyFont, markerFont, colors, highlightedAyah, fontSize, highlightAlpha.value) {
        val ranges = mutableListOf<Pair<Ayah, IntRange>>()
        val noteIconRanges = mutableMapOf<String, IntRange>()
        val annotatedString = buildAnnotatedString {
            ayahs.forEach { ayah ->
                val start = length
                val key = "${ayah.surah}:${ayah.ayah}"

                val hlColor = highlights[key]?.let { tints[it] }
                val hit = when {
                    hlColor != null -> hlColor
                    ayah == highlightedAyah && highlightAlpha.value > 0f -> colors.primary.copy(alpha = highlightAlpha.value)
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
                    append(ayah.textIn(script))
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
                append(" ")

                ranges.add(ayah to (start until length))
            }
        }
        AyahPassageData(annotatedString, ranges, noteIconRanges)
    }

    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    var textCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    // window-space Y is a common frame both this Text and RukuBlock's own container can convert to
    // independently — no manual height bookkeeping, and correct at any font size since it's all real
    // measured layout, not an assumed line height or padding constant
    LaunchedEffect(layout, textCoords, targetAyah, passageData) {
        val currentLayout = layout ?: return@LaunchedEffect
        val coords = textCoords ?: return@LaunchedEffect
        val range = targetAyah?.let { t -> passageData.ranges.firstOrNull { it.first == t } } ?: return@LaunchedEffect
        val lineTop = currentLayout.getBoundingBox(range.second.first).top
        onTargetLocated(coords.localToWindow(Offset(0f, lineTop)).y)
    }

    Text(
        text = passageData.text,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)
            .onGloballyPositioned { textCoords = it }
            .pointerInput(passageData, autoScrolling) {
                detectTapGestures(
                    // While auto-scrolling, holding the page is "wait here" — pause for as long as the
                    // finger is down. Pressing pauses immediately rather than after the long-press delay,
                    // so the text stops where the reader meant to stop it.
                    onPress = {
                        if (autoScrolling) {
                            store.setAutoScrollPaused(true)
                            tryAwaitRelease()
                            store.setAutoScrollPaused(false)
                        }
                    },
                    onTap = { pos ->
                        layout?.getOffsetForPosition(pos)?.let { idx ->
                            passageData.ranges.firstOrNull { idx in it.second }?.let { (ayah, _) ->
                                val noteRange = passageData.noteIconRanges["${ayah.surah}:${ayah.ayah}"]
                                if (noteRange != null && idx in noteRange) onNoteTap(ayah) else onSelect(ayah)
                            }
                        }
                    },
                    // the hold means "pause" while auto-scrolling, so the highlight stays out of the way
                    onLongPress = { pos ->
                        if (!autoScrolling) layout?.getOffsetForPosition(pos)?.let { idx ->
                            passageData.ranges.firstOrNull { idx in it.second }?.let { onLongSelect(it.first) }
                        }
                    }
                )
            },
        fontSize = fontSize.sp,
        fontFamily = bodyFont,
        // a bare style on purpose: left to default, the theme's typography leaks into the paragraph,
        // and on iOS that flips which lam-alif form Skia draws — the studio passes its own style and
        // never had the problem
        style = TextStyle(),
        lineHeight = (fontSize * lineHeightRatio).sp,
        textAlign = if (justify) TextAlign.Justify else TextAlign.Center,
        onTextLayout = { layout = it }
    )
}

