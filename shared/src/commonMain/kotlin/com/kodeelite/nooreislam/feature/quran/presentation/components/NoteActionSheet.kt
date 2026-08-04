package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.Pencil
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.feature.quran.data.Note
import com.kodeelite.nooreislam.feature.quran.data.NotesStore
import com.kodeelite.nooreislam.feature.quran.data.QuranRepository
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.copy_ayah
import com.kodeelite.nooreislam.resources.edit_note
import com.kodeelite.nooreislam.resources.open_ayah
import com.kodeelite.nooreislam.resources.remove_note
import com.kodeelite.nooreislam.resources.share_to_studio
import com.kodeelite.nooreislam.resources.surah_number_ayah_number
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoteActionSheet(
    note: Note,
    store: NotesStore,
    onOpen: () -> Unit,
    onShareToStudio: () -> Unit,
    onEdit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val clipboard = LocalClipboardManager.current
    val ayahText by produceState("") {
        value = QuranRepository.ayah(note.surah, note.ayah)?.text ?: ""
    }

    AppBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(Res.string.surah_number_ayah_number, note.surah, note.ayah),
    ) {
        // full text, read-only — long-press is the "view" entry point, no separate screen needed
        Text(note.text, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
        Spacer(Modifier.height(14.dp))
        AppTileGroup(
            items = listOf(
                AppTileItem(
                    title = stringResource(Res.string.open_ayah),
                    leadingIcon = Lucide.BookOpen,
                    onClick = { onOpen(); onDismiss() }
                ),
                AppTileItem(
                    title = stringResource(Res.string.share_to_studio),
                    leadingIcon = Lucide.Image,
                    onClick = { onShareToStudio(); onDismiss() }
                ),
                AppTileItem(
                    title = stringResource(Res.string.copy_ayah),
                    leadingIcon = Lucide.Copy,
                    onClick = { clipboard.setText(AnnotatedString(ayahText)); onDismiss() }
                ),
                AppTileItem(
                    title = stringResource(Res.string.edit_note),
                    leadingIcon = Lucide.Pencil,
                    onClick = { onEdit(); onDismiss() }
                ),
                AppTileItem(
                    title = stringResource(Res.string.remove_note),
                    leadingIcon = Lucide.Minus,
                    leadingColor = colors.error,
                    onClick = { store.set(note.surah, note.ayah, ""); onDismiss() }
                )
            )
        )
    }
}
