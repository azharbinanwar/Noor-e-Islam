package com.kodeelite.nooreislam.feature.quran.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesStore(
    private val scope: CoroutineScope,
    private val repo: NotesRepository
) {
    val notes: StateFlow<List<Note>?> = repo.active
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    // reactive view of all notes (key -> text) for O(1) lookup during scroll
    val noteMap: StateFlow<Map<String, String>> = repo.active
        .map { list -> list.associate { "${it.surah}:${it.ayah}" to it.text } }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun set(surah: Int, ayah: Int, text: String) {
        scope.launch {
            if (text.isBlank()) repo.remove(surah, ayah)
            else repo.set(surah, ayah, text)
        }
    }
}
