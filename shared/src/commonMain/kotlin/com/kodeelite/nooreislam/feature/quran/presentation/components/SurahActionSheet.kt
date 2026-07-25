package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.runtime.Composable
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Star
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.feature.quran.data.Surah
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.add_to_favorites
import com.kodeelite.nooreislam.resources.open_surah
import com.kodeelite.nooreislam.resources.remove_from_favorites
import com.kodeelite.nooreislam.resources.surah_number_place_ayah_count
import org.jetbrains.compose.resources.stringResource

// long-press a surah → its name/details up top + quick actions (favorite, open)
@Composable
fun SurahActionSheet(
    surah: Surah,
    favorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(
        onDismiss = onDismiss,
        title = surah.nameTransliterated,
        subtitle = stringResource(Res.string.surah_number_place_ayah_count, surah.number, surah.revelation.label, surah.ayahCount),
    ) {
        AppTileGroup(
            items = listOf(
                AppTileItem(
                    title = if (favorite) stringResource(Res.string.remove_from_favorites) else stringResource(Res.string.add_to_favorites),
                    leadingIcon = Lucide.Star,
                    leadingColor = if (favorite) AppTheme.colors.primary else AppTheme.colors.onSurfaceVariant,
                    onClick = { onToggleFavorite(); onDismiss() },
                ),
                AppTileItem(
                    title = stringResource(Res.string.open_surah),
                    leadingIcon = Lucide.BookOpen,
                    onClick = { onOpen(); onDismiss() },
                ),
            ),
        )
    }
}
