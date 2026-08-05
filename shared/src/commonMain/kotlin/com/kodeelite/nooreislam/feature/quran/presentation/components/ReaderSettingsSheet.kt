package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.runtime.Composable
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.ChevronsDown
import com.composables.icons.lucide.Languages
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Navigation
import com.composables.icons.lucide.Sun
import com.composables.icons.lucide.WholeWord
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.auto_scroll
import com.kodeelite.nooreislam.resources.content
import com.kodeelite.nooreislam.resources.jump_to
import com.kodeelite.nooreislam.resources.keep_screen_on
import com.kodeelite.nooreislam.resources.reading
import com.kodeelite.nooreislam.resources.reading_settings
import com.kodeelite.nooreislam.resources.tafsir_source
import com.kodeelite.nooreislam.resources.translation
import com.kodeelite.nooreislam.resources.word_by_word
import org.jetbrains.compose.resources.stringResource

// content + reading behavior — opened only from the reader's app bar (not ayah-specific). Appearance
// (theme, text size, line spacing, script) lives in QuranThemePickerSheet instead, opened from both
// the reader and the index screen.
// ponytail: all rows listed as reminders; the ones without a feature yet are placeholders (onClick = {}).
@Composable
fun ReaderSettingsSheet(onDismiss: () -> Unit) {
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
                    subtitle = "play/pause + speed — placeholder",
                    leadingIcon = Lucide.ChevronsDown,
                    onClick = {}),
                AppTileItem(title = stringResource(Res.string.keep_screen_on), subtitle = "placeholder", leadingIcon = Lucide.Sun, onClick = {}),
                AppTileItem(title = stringResource(Res.string.jump_to), subtitle = "placeholder", leadingIcon = Lucide.Navigation, onClick = {}),
            ),
        )
    }
}
