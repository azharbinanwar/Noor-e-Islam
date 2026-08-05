package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.X
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppTextField
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.core.components.TilePosition
import com.kodeelite.nooreislam.core.components.shapeFor
import com.kodeelite.nooreislam.core.constants.defaults.QuranDefaults
import com.kodeelite.nooreislam.core.util.toSurahKey
import com.kodeelite.nooreislam.feature.quran.data.Ayah
import com.kodeelite.nooreislam.feature.quran.data.QuranRepository
import com.kodeelite.nooreislam.feature.quran.data.QuranSearchRepository
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.clear_search
import com.kodeelite.nooreislam.resources.no_ayahs_found
import com.kodeelite.nooreislam.resources.quran_surah_name
import com.kodeelite.nooreislam.resources.search_quran
import com.kodeelite.nooreislam.resources.search_quran_hint_message
import com.kodeelite.nooreislam.resources.search_quran_hint_title
import com.kodeelite.nooreislam.resources.search_quran_placeholder
import com.kodeelite.nooreislam.resources.surah_number_ayah_number
import com.kodeelite.nooreislam.resources.try_a_different_search
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource

// separate from Jump To on purpose — different intent (I don't know where, but I remember a phrase)
// and a different result shape (ayah previews, not a single navigation target). Matches with or
// without harakat: the DB stores full tashkeel, so both the query and every ayah get normalized
// (diacritics + tatweel stripped, alef variants unified) before comparing — no DB changes needed.
@Composable
fun QuranSearchSheet(onOpen: (surah: Int, ayah: Int) -> Unit, onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    var query by remember { mutableStateOf("") }
    // matching runs against quran_search.db's pre-normalized text (no per-keystroke regex over 6236
    // ayahs); display uses the real ayah (with full tashkeel) from quran.db, looked up by id
    val realAyahs by produceState(emptyList()) { value = QuranRepository.all() }
    val searchAyahs by produceState(emptyList()) { value = QuranSearchRepository.all() }
    val realById = remember(realAyahs) { realAyahs.associateBy { it.id } }
    val results = remember(query, searchAyahs, realById) {
        val q = normalizeArabic(query.trim())
        if (q.isEmpty()) emptyList()
        else searchAyahs.filter { it.text.contains(q) }
            .take(QuranDefaults.QURAN_SEARCH_RESULT_LIMIT)
            .mapNotNull { realById[it.id] }
    }

    AppBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(Res.string.search_quran),
        fillHeight = true,
        header = {
            // the query is always Arabic (or Urdu) — lock the field RTL rather than follow the app locale;
            // pinned above the scrollable results instead of scrolling away with them
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                AppTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = stringResource(Res.string.search_quran_placeholder),
                    leading = { Icon(Lucide.Search, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                    trailing = if (query.isNotEmpty()) {
                        { IconButton(onClick = { query = "" }) { Icon(Lucide.X, stringResource(Res.string.clear_search), tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp)) } }
                    } else null,
                )
            }
        },
    ) {
        when {
            query.isBlank() -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                StateView(
                    title = stringResource(Res.string.search_quran_hint_title),
                    message = stringResource(Res.string.search_quran_hint_message),
                    icon = { Icon(Lucide.Search, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(40.dp)) },
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
            results.isEmpty() -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                StateView(
                    title = stringResource(Res.string.no_ayahs_found),
                    message = stringResource(Res.string.try_a_different_search),
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
            else -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                results.forEachIndexed { i, a ->
                    QuranSearchResultItem(
                        ayah = a,
                        position = TilePosition.at(i, results.size),
                        onClick = { onOpen(a.surah, a.ayah); onDismiss() },
                    )
                }
            }
        }
    }
}

// citation line ("Al-Baqarah · Surah 2 · Ayah 29") on top, then the ayah — full text, no line clamp —
// next to a small trailing calligraphic surah glyph (not a big background watermark). Takes the model
// (Ayah) + a precomputed TilePosition, same shape as SurahItem/CollectionAyahItem — the surah name
// isn't on Ayah, so it's resolved in here (produceState + the already-cached QuranRepository.surahs()),
// not passed in by the caller.
@Composable
private fun QuranSearchResultItem(ayah: Ayah, position: TilePosition, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val surahName by produceState(ayah.surah.toString(), ayah.surah) {
        value = QuranRepository.surahs().firstOrNull { it.number == ayah.surah }?.nameTransliterated ?: ayah.surah.toString()
    }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            Modifier.fillMaxWidth()
                .clip(shapeFor(position))
                .background(colors.cardColor)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                "$surahName · " + stringResource(Res.string.surah_number_ayah_number, ayah.surah, ayah.ayah),
                style = MaterialTheme.typography.labelSmall,
                color = colors.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                        text = ayah.text,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily(Font(QuranDefaults.FONT.res)),
                        color = colors.onSurface,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f),
                        lineHeight = 32.sp,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = ayah.surah.toSurahKey(),
                    fontFamily = FontFamily(Font(Res.font.quran_surah_name)),
                    color = colors.primary,
                    fontSize = 28.sp,
                )
            }
        }
    }
}

// Arabic diacritics (tashkeel) + tatweel (elongation) — combining marks the Uthmani script text is
// stored with; stripping them and unifying alef variants lets a bare-typed query match regardless.
// Pure \u codepoint escapes, never literal Arabic glyphs typed into the ranges — pasting them
// directly is exactly how this matched the wrong characters the first few times this was tried.
// 0610-061A small high marks, 064B-065F general tashkeel, 0670 superscript alef, 06D6-06ED
// Quranic pause/annotation marks, 0640 tatweel. Alef variants 0622/0623/0625/0671 -> plain alef.
private val ARABIC_DIACRITICS_AND_TATWEEL = Regex("[\u0610-\u061A\u064B-\u065F\u0670\u06D6-\u06ED\u0640]")
private val ARABIC_ALEF_VARIANTS = Regex("[\u0622\u0623\u0625\u0671]")

private fun normalizeArabic(text: String): String =
    ARABIC_ALEF_VARIANTS.replace(ARABIC_DIACRITICS_AND_TATWEEL.replace(text, ""), "\u0627")
