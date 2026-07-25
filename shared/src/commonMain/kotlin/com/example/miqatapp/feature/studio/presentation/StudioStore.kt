package com.example.miqatapp.feature.studio.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.miqatapp.core.constants.defaults.StudioDefaults
import com.example.miqatapp.feature.studio.data.StudioConfig
import com.example.miqatapp.feature.studio.data.StudioCreation
import com.example.miqatapp.feature.studio.data.StudioCreationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Studio store (cubit-style state holder), scoped to the screen: current config, undo/redo history,
 * editing mode, gallery, and saved creations. All studio mutation + history lives here; the UI reads
 * `config` and calls `update/undo/redo/saveCurrent`. Saved creations persist via [repo] (Room + JSON);
 * the `*State` fields are exposed so the screen can `by`-delegate to them without churn.
 */
class StudioStore(
    initial: StudioConfig,
    private val repo: StudioCreationRepository,
    private val scope: CoroutineScope,
) {
    val configState = mutableStateOf(initial)
    val isEditingState = mutableStateOf(true)
    val studioModeState = mutableStateOf(StudioMode.DEFAULT)
    val galleryOpenState = mutableStateOf(false)
    val savedCreationsState = mutableStateOf<List<StudioCreation>>(emptyList())
    val draftState = mutableStateOf<StudioConfig?>(null)   // resume-later, in-progress design
    val showSavedHintState = mutableStateOf(false)

    private var config by configState
    private var savedCreations by savedCreationsState
    private var draft by draftState
    private var showSavedHint by showSavedHintState

    private var history = listOf(initial)
    private var historyIndex = 0
    private var commitJob: Job? = null   // debounces continuous gestures into one history entry

    init { refresh() }

    private fun refresh() = scope.launch { savedCreations = repo.list(); draft = repo.loadDraft() }.let {}

    // discrete edit (panel toggle, template, reset): commit to history immediately
    fun update(newConfig: StudioConfig) {
        commitJob?.cancel()
        config = newConfig
        commitToHistory()
    }

    // continuous edit (drag / pinch / slider): render live, commit one entry once it settles
    fun updateLive(newConfig: StudioConfig) {
        config = newConfig
        commitJob?.cancel()
        commitJob = scope.launch { delay(400); commitToHistory() }
    }

    private fun commitToHistory() {
        if (config == history.getOrNull(historyIndex)) return   // nothing changed
        val newHistory = history.subList(0, historyIndex + 1).toMutableList()
        newHistory.add(config)
        if (newHistory.size > StudioDefaults.HISTORY_MAX) newHistory.removeAt(0)
        history = newHistory
        historyIndex = history.size - 1
    }

    fun undo() {
        if (historyIndex > 0) {
            historyIndex--; config = history[historyIndex]
        }
    }

    fun redo() {
        if (historyIndex < history.size - 1) {
            historyIndex++; config = history[historyIndex]
        }
    }

    fun saveCurrent() = scope.launch {
        repo.save(config)
        savedCreations = repo.list()
        showSavedHint = true
    }.let {}

    fun delete(id: Long) = scope.launch {
        repo.delete(id)
        savedCreations = repo.list()
    }.let {}

    // resume-later draft (single row); auto-saved (debounced) while editing
    fun saveDraft() = scope.launch { repo.saveDraft(config); draft = config }.let {}
}
