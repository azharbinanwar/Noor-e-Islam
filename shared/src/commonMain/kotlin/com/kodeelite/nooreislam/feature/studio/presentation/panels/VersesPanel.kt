package com.kodeelite.nooreislam.feature.studio.presentation.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.constants.defaults.StudioDefaults
import com.kodeelite.nooreislam.core.util.toSurahKey
import com.kodeelite.nooreislam.feature.quran.data.Ayah
import com.kodeelite.nooreislam.feature.quran.data.QuranRepository
import com.kodeelite.nooreislam.feature.quran.data.Surah
import com.kodeelite.nooreislam.feature.studio.data.StudioConfig
import com.kodeelite.nooreislam.feature.studio.presentation.components.SectionHeader
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.ayah
import com.kodeelite.nooreislam.resources.quran_surah_name
import com.kodeelite.nooreislam.resources.search
import com.kodeelite.nooreislam.resources.show_less
import com.kodeelite.nooreislam.resources.surah
import com.kodeelite.nooreislam.resources.view_all
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource

// Which verses are on the canvas. A surah strip (calligraphic names, View all expands to a searchable
// grid) and that surah's ayah numbers (View all expands to a grid). Tap one ayah for a single, tap a
// second to close the range between — the canvas re-renders on every tap. Contiguous within one
// surah on purpose: a share image is a passage, not a scrapbook.
@Composable
fun VersesPanel(config: StudioConfig, onChange: (StudioConfig) -> Unit) {
    val colors = AppTheme.colors
    val scope = rememberCoroutineScope()
    val surahs by produceState(emptyList<Surah>()) { value = QuranRepository.surahs() }
    val nameFont = FontFamily(Font(Res.font.quran_surah_name))

    val currentSurah = config.ayahs.first().surah
    val start = config.ayahs.first().ayah
    val end = config.ayahs.last().ayah

    // the whole surah once per surah change; a tap then slices it synchronously. Fetching per tap
    // raced: a later tap's query could land first and throw the selection backwards.
    val surahAyahs by produceState(config.ayahs, currentSurah) { value = QuranRepository.surah(currentSurah) }

    // a new selection re-sizes the text to fit, same thresholds the studio opens with;
    // the size slider still overrides afterwards
    fun sized(ayahs: List<Ayah>): StudioConfig {
        val len = ayahs.sumOf { it.textIn(config.fontFamily.script).length }
        val size = when {
            len < StudioDefaults.SHORT_LEN -> StudioDefaults.FONT_SHORT
            len < StudioDefaults.MEDIUM_LEN -> StudioDefaults.FONT_MEDIUM
            else -> StudioDefaults.FONT_LONG
        }
        return config.copy(ayahs = ayahs, fontSize = size)
    }

    fun select(from: Int, to: Int) {
        val range = surahAyahs.filter { it.ayah in from..to }
        if (range.isNotEmpty()) onChange(sized(range))
    }

    fun moveToSurah(surah: Int) {
        scope.launch {
            val first = QuranRepository.surah(surah).take(1)
            if (first.isNotEmpty()) onChange(sized(first))
        }
    }

    // the first pick anchors; every tap after moves the other end — grow or shrink either way.
    // Tapping the anchor itself collapses back to a single ayah.
    fun tapAyah(n: Int) = when {
        n == start && end > start -> select(start, start)
        else -> {
            val a = minOf(start, n)
            val b = maxOf(start, n).coerceAtMost(a + StudioDefaults.MAX_AYAHS - 1)
            select(a, b)
        }
    }

    var surahsExpanded by remember { mutableStateOf(false) }
    var ayahsExpanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        SectionHeader(
            stringResource(Res.string.surah),
            actionLabel = stringResource(if (surahsExpanded) Res.string.show_less else Res.string.view_all),
            // one grid at a time, so the open one gets the panel's full height
            onAction = { surahsExpanded = !surahsExpanded; ayahsExpanded = false; query = "" },
        )
        if (!surahsExpanded) {
            val surahRow = rememberLazyListState()
            LaunchedEffect(surahs, currentSurah) {
                if (surahs.isNotEmpty()) surahRow.scrollToItem((currentSurah - 1).coerceAtLeast(0))
            }
            LazyRow(state = surahRow, contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(surahs, key = { it.number }) { s ->
                    SurahChip(s, s.number == currentSurah, nameFont, Modifier) { if (s.number != currentSurah) moveToSurah(s.number) }
                }
            }
        } else {
            // search matches the name or the number, so "kah" and "18" both land on Al-Kahf
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = TextStyle(color = colors.onSurface, fontSize = 13.sp),
                decorationBox = { field ->
                    Box(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(10.dp))
                            .background(colors.onSurface.copy(alpha = 0.08f)).padding(horizontal = 12.dp, vertical = 9.dp),
                    ) {
                        if (query.isEmpty()) Text(stringResource(Res.string.search), color = colors.onSurfaceVariant, fontSize = 13.sp)
                        field()
                    }
                },
            )
            val hits = surahs.filter {
                query.isBlank() || it.nameTransliterated.contains(query, ignoreCase = true) || it.number.toString().startsWith(query.trim())
            }
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                hits.chunked(3).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { s ->
                            SurahChip(s, s.number == currentSurah, nameFont, Modifier.weight(1f)) {
                                if (s.number != currentSurah) moveToSurah(s.number)
                                surahsExpanded = false
                            }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }

        val count = surahs.firstOrNull { it.number == currentSurah }?.ayahCount ?: 0
        SectionHeader(
            stringResource(Res.string.ayah),
            actionLabel = if (count > StudioDefaults.MAX_AYAHS) stringResource(if (ayahsExpanded) Res.string.show_less else Res.string.view_all) else null,
            onAction = if (count > StudioDefaults.MAX_AYAHS) ({ ayahsExpanded = !ayahsExpanded; surahsExpanded = false }) else null,
        )
        if (!ayahsExpanded) {
            val ayahRow = rememberLazyListState()
            LaunchedEffect(currentSurah, count) {
                if (count > 0) ayahRow.scrollToItem((start - 1).coerceAtLeast(0))
            }
            LazyRow(state = ayahRow, contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items((1..count).toList(), key = { it }) { n -> AyahDot(n, n in start..end, Modifier.size(34.dp)) { tapAyah(n) } }
            }
        } else {
            // stays open across taps — closing a range takes two of them
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                (1..count).toList().chunked(6).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { n -> AyahDot(n, n in start..end, Modifier.weight(1f).height(40.dp)) { tapAyah(n) } }
                        repeat(6 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SurahChip(s: Surah, active: Boolean, nameFont: FontFamily, modifier: Modifier, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier.clip(RoundedCornerShape(10.dp))
            .background(if (active) colors.primary else colors.onSurface.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("${s.number}", color = if (active) colors.onPrimary else colors.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        // the same calligraphic glyph the canvas draws, so the chip previews the design itself
        Text(s.number.toSurahKey(), fontFamily = nameFont, fontSize = 20.sp, color = if (active) colors.onPrimary else colors.onSurface, maxLines = 1)
    }
}

@Composable
private fun AyahDot(n: Int, inRange: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Box(
        modifier.clip(CircleShape)
            .background(if (inRange) colors.primary else colors.onSurface.copy(alpha = 0.08f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "$n",
            color = if (inRange) colors.onPrimary else colors.onSurface,
            fontSize = 12.sp,
            fontWeight = if (inRange) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
