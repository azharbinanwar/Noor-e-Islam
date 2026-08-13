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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
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
import com.kodeelite.nooreislam.core.components.AppChip
import com.kodeelite.nooreislam.core.components.AppTextField
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.core.components.TilePosition
import com.kodeelite.nooreislam.core.components.shapeFor
import com.kodeelite.nooreislam.core.constants.defaults.QuranDefaults
import com.kodeelite.nooreislam.core.util.NameMatch
import com.kodeelite.nooreislam.core.util.fromArabicIndicDigits
import com.kodeelite.nooreislam.core.util.latinKeys
import com.kodeelite.nooreislam.core.util.nameMatch
import com.kodeelite.nooreislam.core.util.normalizeArabic
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
    val surahs by produceState(emptyList()) { value = QuranRepository.surahs() }
    val realById = remember(realAyahs) { realAyahs.associateBy { it.id } }
    val bySurahAyah = remember(realAyahs) { realAyahs.associateBy { it.surah to it.ayah } }

    // any digits in the query are treated as a surah/ayah reference (in either order — whichever
    // ordering is a real surah:ayah combo wins), on top of, not instead of, the text match below.
    // A lone number is ambiguous (surah N, or ayah N of every surah that has one) so both are offered.
    // `matches` is every hit; `results` is what the list renders. Counting the full set costs nothing
    // extra (the filter already walks all ayahs), so the header can show the true total past the cap.
    val matches = remember(query, searchAyahs, realById, bySurahAyah, surahs) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@remember emptyList()

        // Arabic-Indic/Urdu digits normalize to ASCII first, so "٢:٢٥٥" or "۲:۲۵۵" match the same way "2:255" does
        val normalized = trimmed.fromArabicIndicDigits()
        val numbers = NUMBER.findAll(normalized).map { it.value.toInt() }.toList()
        val refResults = when (numbers.size) {
            2 -> listOfNotNull(bySurahAyah[numbers[0] to numbers[1]], bySurahAyah[numbers[1] to numbers[0]])
            1 -> listOfNotNull(bySurahAyah[numbers[0] to 1]) + realAyahs.filter { it.ayah == numbers[0] }
            else -> emptyList()
        }

        val textQuery = NUMBER.replace(normalized, "").trim().normalizeArabic()
        val textResults = if (textQuery.isEmpty()) emptyList() else {
            // surah names rank by match tier — exact spelling, then variant/prefix, and only when
            // neither hit anything does the typo tier speak (so "bakara" works, "baqar" never fuzzes)
            val queryKeys = latinKeys(textQuery)
            val noMatch = NameMatch.entries.size
            val byTier = surahs.mapNotNull { s ->
                val latin = minOf(
                    nameMatch(queryKeys, s.nameTransliterated)?.ordinal ?: noMatch,
                    nameMatch(queryKeys, s.nameEnglish)?.ordinal ?: noMatch,
                )
                val tier = when {
                    latin < noMatch -> latin
                    s.nameArabic.normalizeArabic().contains(textQuery) -> NameMatch.PARTIAL.ordinal
                    else -> return@mapNotNull null
                }
                s to tier
            }.sortedBy { it.second }
            val bestTier = byTier.firstOrNull()?.second
            val surahJumps = byTier
                .filter { it.second < NameMatch.FUZZY.ordinal || bestTier == NameMatch.FUZZY.ordinal }
                .mapNotNull { bySurahAyah[it.first.number to 1] }
            val bodyMatches = searchAyahs.filter { it.text.contains(textQuery) }
                .mapNotNull { realById[it.id] }
            surahJumps + bodyMatches
        }

        (refResults + textResults).distinctBy { it.id }
    }

    AppBottomSheet(
        onDismiss = onDismiss,
        // no title at all — the search field is the header and gives the sheet its context
        fillHeight = true,
        // the results LazyColumn below scrolls itself — every match renders, composed on demand
        scrollBody = false,
        header = {
            // no forced direction — query can be Arabic, English (surah name), or a numeric reference,
            // so the field just follows the ambient app locale like any other input; pinned above the
            // scrollable results instead of scrolling away with them
            AppTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = stringResource(Res.string.search_quran_placeholder),
                leading = { Icon(Lucide.Search, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                trailing = if (query.isNotEmpty()) {
                    { IconButton(onClick = { query = "" }) { Icon(Lucide.X, stringResource(Res.string.clear_search), tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp)) } }
                } else null,
            )
            if (matches.isNotEmpty()) Text(
                "${matches.size} results", // locale pass pending
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        },
    ) {
        when {
            query.isBlank() -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                StateView(
                    title = stringResource(Res.string.search_quran_hint_title),
                    message = stringResource(Res.string.search_quran_hint_message),
                    icon = { Icon(Lucide.Search, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(40.dp)) },
                    modifier = Modifier.padding(top = 24.dp),
                    action = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppChip(label = "2:255", selected = false, onClick = { query = "2:255" })
                            AppChip(label = "Al-Baqarah", selected = false, onClick = { query = "Al-Baqarah" })
                            AppChip(label = "الرحمن", selected = false, onClick = { query = "الرحمن" })
                        }
                    },
                )
            }
            matches.isEmpty() -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                StateView(
                    title = stringResource(Res.string.no_ayahs_found),
                    message = stringResource(Res.string.try_a_different_search),
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
            else -> {
                // the text part of the query, normalized the same way the match ran — what the rows highlight
                val tokens = remember(query) {
                    NUMBER.replace(query.trim().fromArabicIndicDigits(), "").trim().normalizeArabic()
                        .split(' ').filter { it.isNotEmpty() }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(matches.size, key = { matches[it].id }) { i ->
                        val a = matches[i]
                        QuranSearchResultItem(
                            ayah = a,
                            highlight = tokens,
                            position = TilePosition.at(i, matches.size),
                            onClick = { onOpen(a.surah, a.ayah); onDismiss() },
                        )
                    }
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
private fun QuranSearchResultItem(ayah: Ayah, highlight: List<String>, position: TilePosition, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val surahName by produceState(ayah.surah.toString(), ayah.surah) {
        value = QuranRepository.surahs().firstOrNull { it.number == ayah.surah }?.nameTransliterated ?: ayah.surah.toString()
    }
    // matched words get a tinted background. Whole words, not character offsets: the match ran on
    // normalized text and the row shows full tashkeel, so positions don't line up — words do.
    val tint = colors.primary.copy(alpha = 0.15f)
    val ayahText = remember(ayah.text, highlight) {
        if (highlight.isEmpty()) AnnotatedString(ayah.text) else buildAnnotatedString {
            ayah.text.split(' ').forEachIndexed { i, word ->
                if (i > 0) append(' ')
                val hit = highlight.any { word.normalizeArabic().contains(it) }
                if (hit) withStyle(SpanStyle(background = tint)) { append(word) } else append(word)
            }
        }
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
                        text = ayahText,
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

private val NUMBER = Regex("\\d+")
