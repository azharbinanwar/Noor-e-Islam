package com.kodeelite.nooreislam.feature.quran.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.StickyNote
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.core.components.TilePosition
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.feature.quran.data.Note
import com.kodeelite.nooreislam.feature.quran.data.NotesStore
import com.kodeelite.nooreislam.feature.quran.presentation.components.NoteActionSheet
import com.kodeelite.nooreislam.feature.quran.presentation.components.NoteEditorSheet
import com.kodeelite.nooreislam.feature.quran.presentation.components.NoteItem
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.loading_notes
import com.kodeelite.nooreislam.resources.no_notes
import com.kodeelite.nooreislam.resources.notes_hint
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun NotesTab() {
    val nav = LocalAppNavigator.current
    val store = koinInject<NotesStore>()
    val notesState by store.notes.collectAsState()
    var actionNote by remember { mutableStateOf<Note?>(null) }
    var editingNote by remember { mutableStateOf<Note?>(null) }

    if (notesState == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            StateView.Loading(title = stringResource(Res.string.loading_notes))
        }
    } else if (notesState!!.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            StateView(
                title = stringResource(Res.string.no_notes),
                message = stringResource(Res.string.notes_hint),
                icon = { Icon(Lucide.StickyNote, null, tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(40.dp)) }
            )
        }
    } else {
        val notes = notesState!!
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(notes.size) { i ->
                val note = notes[i]
                NoteItem(
                    note, TilePosition.at(i, notes.size),
                    onClick = { nav.navigate(AppRoute.QuranReader(note.surah, note.ayah)) },
                    onLongClick = { actionNote = note },
                )
            }
        }
    }

    actionNote?.let { note ->
        NoteActionSheet(
            note = note,
            store = store,
            onEdit = { editingNote = note },
            onDismiss = { actionNote = null },
        )
    }

    editingNote?.let { note ->
        NoteEditorSheet(
            surah = note.surah,
            ayah = note.ayah,
            initialText = note.text,
            store = store,
            onDismiss = { editingNote = null },
        )
    }
}
