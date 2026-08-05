package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.History
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppTextField
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.core.components.TilePosition
import com.kodeelite.nooreislam.core.constants.defaults.QuranDefaults
import com.kodeelite.nooreislam.feature.quran.data.QuranRepository
import com.kodeelite.nooreislam.feature.quran.data.QuranStore
import com.kodeelite.nooreislam.feature.quran.data.Surah
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.favorites_and_common
import com.kodeelite.nooreislam.resources.jump_to
import com.kodeelite.nooreislam.resources.jump_to_ayah_field
import com.kodeelite.nooreislam.resources.jump_to_choose_surah
import com.kodeelite.nooreislam.resources.jump_to_surah_field
import com.kodeelite.nooreislam.resources.no_surahs_found
import com.kodeelite.nooreislam.resources.open_ayah
import com.kodeelite.nooreislam.resources.recent
import com.kodeelite.nooreislam.resources.search
import com.kodeelite.nooreislam.resources.surahs_label
import com.kodeelite.nooreislam.resources.try_a_different_search
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

// pure pick-and-choose, no typing required: a permanent [Surah][Ayah #][Go] picker, then Recent jumps,
// then Favorites/Common as one shortcut list. Free-text search lives in its own separate feature
// (search-the-Quran-text) instead of being merged in here — different intent, different result shape.
@Composable
fun SurahPickerSheet(onOpen: (surah: Int, ayah: Int) -> Unit, onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    val store = koinInject<QuranStore>()
    val favorites by store.favorites.collectAsState()
    val recentJumps by store.recentJumps.collectAsState()
    val surahs by produceState(emptyList()) { value = QuranRepository.surahs() }

    var pickedSurah by remember { mutableStateOf<Surah?>(null) }
    var ayahText by remember(pickedSurah) { mutableStateOf("") }
    val pickedAyah = ayahText.toIntOrNull()?.coerceIn(1, pickedSurah?.ayahCount ?: 1) ?: 1
    var showSurahListPicker by remember { mutableStateOf(false) }

    fun openAndDismiss(surah: Int, ayah: Int) {
        store.recordJump(surah, ayah)
        onOpen(surah, ayah)
        onDismiss()
    }

    AppBottomSheet(onDismiss = onDismiss, title = stringResource(Res.string.jump_to), fillHeight = true) {
        // one row, not two — tap the tile to change the surah, type the ayah right there; the arrow
        // sits beside it, a fixed square matching the tile's natural height (not intrinsic-measured —
        // GroupShell's own padding/clip chain doesn't propagate IntrinsicSize reliably)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppTileGroup(
                modifier = Modifier.weight(1f),
                items = listOf(
                    AppTileItem(
                        title = pickedSurah?.nameTransliterated ?: stringResource(Res.string.jump_to_choose_surah),
                        onClick = { showSurahListPicker = true },
                        trailing = {
                            val maxAyah = pickedSurah?.ayahCount ?: 1
                            BasicTextField(
                                value = ayahText,
                                onValueChange = { v -> ayahText = v.filter { it.isDigit() }.take(3) },
                                enabled = pickedSurah != null,
                                singleLine = true,
                                textStyle = TextStyle(color = colors.onSurface, fontSize = 14.sp, textAlign = TextAlign.Center),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                cursorBrush = SolidColor(colors.primary),
                                modifier = Modifier.width(76.dp).height(36.dp).clip(RoundedCornerShape(10.dp)).background(colors.background),
                                decorationBox = { inner ->
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        if (ayahText.isEmpty()) Text("1–$maxAyah", color = colors.onSurfaceVariant, fontSize = 12.sp, textAlign = TextAlign.Center)
                                        inner()
                                    }
                                },
                            )
                        },
                    ),
                ),
            )
            val canGo = pickedSurah != null
            Box(
                Modifier.size(56.dp).clip(RoundedCornerShape(16.dp))
                    .background(if (canGo) colors.primary else colors.cardColor)
                    .clickable(enabled = canGo) { pickedSurah?.let { openAndDismiss(it.number, pickedAyah) } },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Lucide.ArrowRight,
                    stringResource(Res.string.open_ayah),
                    tint = if (canGo) colors.onPrimary else colors.onSurfaceVariant.copy(alpha = 0.4f),
                )
            }
        }

        if (recentJumps.isNotEmpty()) {
            AppTileGroup(
                title = stringResource(Res.string.recent),
                modifier = Modifier.padding(top = 16.dp),
                items = recentJumps.map { (n, ayah) ->
                    val s = surahs.firstOrNull { it.number == n }
                    AppTileItem(
                        title = s?.nameTransliterated ?: "${stringResource(Res.string.jump_to_surah_field)} $n",
                        subtitle = "${stringResource(Res.string.jump_to_ayah_field)} $ayah",
                        leadingIcon = Lucide.History,
                        onClick = { openAndDismiss(n, ayah) },
                    )
                },
            )
        }
        val shortcuts = remember(surahs, favorites) {
            surahs.filter { it.number in favorites || it.number in QuranDefaults.COMMON_SURAHS }
        }
        if (shortcuts.isNotEmpty()) {
            JumpToSectionLabel(stringResource(Res.string.favorites_and_common))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                shortcuts.forEachIndexed { i, s ->
                    SurahItem(s, TilePosition.at(i, shortcuts.size), onClick = { pickedSurah = s })
                }
            }
        }
    }

    if (showSurahListPicker) {
        SurahListPickerSheet(
            surahs = surahs,
            onPick = { pickedSurah = it; showSurahListPicker = false },
            onDismiss = { showSurahListPicker = false },
        )
    }
}

// same label style as SurahsTab's SectionLabel — kept local since that one's private to its own file
@Composable
private fun JumpToSectionLabel(text: String) {
    Text(
        text,
        color = AppTheme.colors.primary,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp),
    )
}

// searchable full surah list — same SurahItem tile as the Surahs tab, so it reads as the same screen
@Composable
private fun SurahListPickerSheet(surahs: List<Surah>, onPick: (Surah) -> Unit, onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    var query by remember { mutableStateOf("") }
    val shown = if (query.isBlank()) surahs else surahs.filter {
        it.nameTransliterated.contains(query, ignoreCase = true) ||
            it.nameEnglish.contains(query, ignoreCase = true) ||
            it.nameArabic.contains(query) ||
            it.number.toString() == query.trim()
    }

    AppBottomSheet(onDismiss = onDismiss, title = stringResource(Res.string.surahs_label), fillHeight = true) {
        AppTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = stringResource(Res.string.search),
            leading = { Icon(Lucide.Search, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(),
        )
        if (shown.isEmpty()) {
            StateView(
                title = stringResource(Res.string.no_surahs_found),
                message = stringResource(Res.string.try_a_different_search),
                modifier = Modifier.padding(top = 32.dp),
            )
        } else {
            Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                shown.forEachIndexed { i, s ->
                    SurahItem(s, TilePosition.at(i, shown.size), onClick = { onPick(s) })
                }
            }
        }
    }
}
