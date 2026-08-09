package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.ChevronsDown
import com.composables.icons.lucide.Gauge
import com.composables.icons.lucide.Languages
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Navigation
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Sun
import com.composables.icons.lucide.WholeWord
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppSwitch
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.MiniStepper
import com.kodeelite.nooreislam.core.constants.defaults.QuranDefaults
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.feature.quran.data.QuranStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.auto_scroll
import com.kodeelite.nooreislam.resources.auto_scroll_speed
import com.kodeelite.nooreislam.resources.content
import com.kodeelite.nooreislam.resources.jump_to
import com.kodeelite.nooreislam.resources.keep_screen_on
import com.kodeelite.nooreislam.resources.reading
import com.kodeelite.nooreislam.resources.reading_settings
import com.kodeelite.nooreislam.resources.search_quran
import com.kodeelite.nooreislam.resources.tafsir_source
import com.kodeelite.nooreislam.resources.translation
import com.kodeelite.nooreislam.resources.word_by_word
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

// content + reading behavior — opened only from the reader's app bar (not ayah-specific). Appearance
// (theme, text size, line spacing, script) lives in QuranThemePickerSheet instead, opened from both
// the reader and the index screen.
// ponytail: all rows listed as reminders; the ones without a feature yet are placeholders (onClick = {}).
@Composable
fun ReaderSettingsSheet(onDismiss: () -> Unit) {
    val nav = LocalAppNavigator.current
    val store = koinInject<QuranStore>()
    val autoScrollEnabled by store.autoScrollEnabled.collectAsState()
    val autoScrollSpeed by store.autoScrollSpeed.collectAsState()
    val keepScreenOn by store.keepScreenOn.collectAsState()
    var showJumpTo by remember { mutableStateOf(false) }
    var showSearchQuran by remember { mutableStateOf(false) }
    AppBottomSheet(onDismiss = onDismiss, title = stringResource(Res.string.reading_settings)) {
        AppTileGroup(
            title = stringResource(Res.string.content),
            items = listOf(
                AppTileItem(
                    title = stringResource(Res.string.translation),
                    subtitle = "on/off + pick — placeholder",
                    leadingIcon = Lucide.Languages,
                    onClick = {}),
                AppTileItem(title = stringResource(Res.string.tafsir_source), subtitle = "placeholder", leadingIcon = Lucide.BookOpen, onClick = {}),
                AppTileItem(
                    title = stringResource(Res.string.word_by_word),
                    subtitle = "on/off — placeholder",
                    leadingIcon = Lucide.WholeWord,
                    onClick = {}),
            ),
        )
        AppTileGroup(
            title = stringResource(Res.string.reading),
            items = listOf(
                AppTileItem(
                    title = stringResource(Res.string.auto_scroll),
                    leadingIcon = Lucide.ChevronsDown,
                    trailing = { AppSwitch(checked = autoScrollEnabled, onCheckedChange = { store.toggleAutoScroll() }) },
                ),
                AppTileItem(
                    title = stringResource(Res.string.auto_scroll_speed),
                    leadingIcon = Lucide.Gauge,
                    trailing = {
                        Box(Modifier.alpha(if (autoScrollEnabled) 1f else 0.4f)) {
                            MiniStepper(
                                value = autoScrollSpeed,
                                suffix = "x",
                                onChange = { store.setAutoScrollSpeed(it) },
                                min = QuranDefaults.MIN_AUTO_SCROLL_SPEED,
                                max = QuranDefaults.MAX_AUTO_SCROLL_SPEED,
                            )
                        }
                    },
                ),
                AppTileItem(
                    title = stringResource(Res.string.keep_screen_on),
                    leadingIcon = Lucide.Sun,
                    trailing = { AppSwitch(checked = keepScreenOn, onCheckedChange = { store.toggleKeepScreenOn() }) },
                ),
                AppTileItem(title = stringResource(Res.string.jump_to), leadingIcon = Lucide.Navigation, onClick = { showJumpTo = true }),
                AppTileItem(title = stringResource(Res.string.search_quran), leadingIcon = Lucide.Search, onClick = { showSearchQuran = true }),
            ),
        )
    }

    if (showJumpTo) {
        SurahPickerSheet(
            onOpen = { surah, ayah ->
                nav.navigate(AppRoute.QuranReader(surah, ayah))
                showJumpTo = false
                onDismiss()
            },
            onDismiss = { showJumpTo = false },
        )
    }

    if (showSearchQuran) {
        QuranSearchSheet(
            onOpen = { surah, ayah ->
                nav.navigate(AppRoute.QuranReader(surah, ayah))
                showSearchQuran = false
                onDismiss()
            },
            onDismiss = { showSearchQuran = false },
        )
    }
}
