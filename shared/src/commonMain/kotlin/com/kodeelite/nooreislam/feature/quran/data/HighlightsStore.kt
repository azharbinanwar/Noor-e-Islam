package com.kodeelite.nooreislam.feature.quran.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HighlightsStore(
    private val scope: CoroutineScope,
    private val repo: HighlightsRepository
) {
    val highlights: StateFlow<List<Highlight>?> = repo.active
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    val colors: StateFlow<Map<String, HighlightColor>> = repo.colors
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun set(surah: Int, ayah: Int, color: HighlightColor? = null) {
        scope.launch {
            if (color == null) repo.remove(surah, ayah)
            else repo.set(surah, ayah, color)
        }
    }

    fun applyDefault(surah: Int, ayah: Int) {
        scope.launch { repo.set(surah, ayah) }
    }
}
