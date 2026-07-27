package com.kodeelite.nooreislam.feature.quran.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookmarksStore(
    private val scope: CoroutineScope,
    private val repo: BookmarksRepository
) {
    val bookmarks: StateFlow<List<Bookmark>> = repo.active
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val keys: StateFlow<Set<String>> = repo.keys
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun toggle(surah: Int, ayah: Int) {
        scope.launch { repo.toggle(surah, ayah) }
    }
}
