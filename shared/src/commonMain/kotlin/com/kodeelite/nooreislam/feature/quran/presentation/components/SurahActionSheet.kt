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
        subtitle = "Surah ${surah.number} · ${surah.revelation.label} · ${surah.ayahCount} ayahs",
    ) {
        AppTileGroup(
            items = listOf(
                AppTileItem(
                    title = if (favorite) "Remove from Favorites" else "Add to Favorites",
                    leadingIcon = Lucide.Star,
                    leadingColor = if (favorite) AppTheme.colors.primary else AppTheme.colors.onSurfaceVariant,
                    onClick = { onToggleFavorite(); onDismiss() },
                ),
                AppTileItem(
                    title = "Open surah",
                    leadingIcon = Lucide.BookOpen,
                    onClick = { onOpen(); onDismiss() },
                ),
            ),
        )
    }
}
