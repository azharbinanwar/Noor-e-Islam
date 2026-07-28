package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.feature.quran.data.Bookmark
import com.kodeelite.nooreislam.feature.quran.data.BookmarksStore
import com.kodeelite.nooreislam.feature.quran.data.QuranRepository
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.copy_ayah
import com.kodeelite.nooreislam.resources.remove_bookmark
import com.kodeelite.nooreislam.resources.share_to_studio
import com.kodeelite.nooreislam.resources.surah_number_ayah_number
import org.jetbrains.compose.resources.stringResource

@Composable
fun BookmarkActionSheet(
    bookmark: Bookmark,
    store: BookmarksStore,
    onShareToStudio: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val clipboard = LocalClipboardManager.current
    val text by produceState("") {
        value = QuranRepository.ayah(bookmark.surah, bookmark.ayah)?.text ?: ""
    }

    AppBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(Res.string.surah_number_ayah_number, bookmark.surah, bookmark.ayah),
    ) {
        AppTileGroup(
            items = listOf(
                AppTileItem(
                    title = stringResource(Res.string.share_to_studio),
                    leadingIcon = Lucide.Image,
                    onClick = { onShareToStudio(); onDismiss() }
                ),
                AppTileItem(
                    title = stringResource(Res.string.copy_ayah),
                    leadingIcon = Lucide.Copy,
                    onClick = {
                        clipboard.setText(AnnotatedString(text))
                        onDismiss()
                    }
                ),
                AppTileItem(
                    title = stringResource(Res.string.remove_bookmark),
                    leadingIcon = Lucide.Minus,
                    leadingColor = colors.error,
                    onClick = { store.toggle(bookmark.surah, bookmark.ayah); onDismiss() }
                )
            )
        )
    }
}
