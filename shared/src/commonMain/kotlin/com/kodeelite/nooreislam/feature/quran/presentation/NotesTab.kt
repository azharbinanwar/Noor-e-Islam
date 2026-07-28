package com.kodeelite.nooreislam.feature.quran.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kodeelite.nooreislam.core.components.TilePosition
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.feature.quran.data.NotesStore
import com.kodeelite.nooreislam.feature.quran.presentation.components.NoteItem
import org.koin.compose.koinInject

@Composable
fun NotesTab() {
    val nav = LocalAppNavigator.current
    val store = koinInject<NotesStore>()
    val notes by store.notes.collectAsState()
    val noteList = notes.toList()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(notes.size) { i ->
            val note = notes[i]
            NoteItem(note.surah, note.ayah, note.text, TilePosition.at(i, notes.size)) {
                nav.navigate(AppRoute.QuranReader(note.surah, note.ayah))
            }
        }
    }
}
