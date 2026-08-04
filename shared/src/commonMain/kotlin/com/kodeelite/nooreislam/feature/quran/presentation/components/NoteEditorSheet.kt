package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppTextField
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.feature.quran.data.NotesStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.note_placeholder
import com.kodeelite.nooreislam.resources.remove_note
import com.kodeelite.nooreislam.resources.save
import com.kodeelite.nooreislam.resources.surah_number_ayah_number
import org.jetbrains.compose.resources.stringResource

// create/edit an ayah note; blank save deletes it, matching NotesStore.set's own semantics
@Composable
fun NoteEditorSheet(surah: Int, ayah: Int, initialText: String, store: NotesStore, onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    var text by remember(surah, ayah) { mutableStateOf(initialText) }

    AppBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(Res.string.surah_number_ayah_number, surah, ayah),
        footer = {
            AppButton(
                stringResource(Res.string.save),
                onClick = { store.set(surah, ayah, text.trim()); onDismiss() },
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        AppTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = stringResource(Res.string.note_placeholder),
            singleLine = false,
            modifier = Modifier.heightIn(min = 120.dp),
        )
        if (initialText.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            AppTileGroup(
                items = listOf(
                    AppTileItem(
                        title = stringResource(Res.string.remove_note),
                        leadingIcon = Lucide.Minus,
                        leadingColor = colors.error,
                        onClick = { store.set(surah, ayah, ""); onDismiss() }
                    )
                )
            )
        }
    }
}
