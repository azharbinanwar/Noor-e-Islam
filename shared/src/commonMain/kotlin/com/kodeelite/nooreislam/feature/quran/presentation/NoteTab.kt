package com.kodeelite.nooreislam.feature.quran.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kodeelite.nooreislam.core.components.TilePosition
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.feature.quran.presentation.components.NoteRow

// Notes — placeholder rows until the notes store lands.
private class DummyNote(val surah: Int, val ayah: Int, val text: String)

private val DUMMY_NOTES = listOf(
    DummyNote(2, 286, "A dua to memorize for daily use"),
    DummyNote(1, 5, "The heart of Al-Fatiha"),
)

@Composable
fun NoteTab() {
    val nav = LocalAppNavigator.current
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(DUMMY_NOTES.size) { i ->
            val n = DUMMY_NOTES[i]
            NoteRow(n.surah, n.ayah, n.text, TilePosition.at(i, DUMMY_NOTES.size)) {
                nav.navigate(AppRoute.QuranReader(n.surah, n.ayah))
            }
        }
    }
}
