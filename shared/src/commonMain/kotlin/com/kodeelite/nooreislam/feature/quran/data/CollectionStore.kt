package com.kodeelite.nooreislam.feature.quran.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollectionStore(
    private val scope: CoroutineScope,
    private val repo: CollectionRepository,
) {
    val collections: StateFlow<List<Collection>?> = repo.collections
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    // per-collection ayah list — callers collectAsState() this directly (count badge, expand preview, details screen)
    fun ayahsIn(collectionId: Long): Flow<List<CollectionAyah>> = repo.ayahsIn(collectionId)

    fun addToCollectionByName(name: String, surah: Int, ayah: Int) {
        scope.launch { repo.addToCollectionByName(name, surah, ayah) }
    }

    fun addToCollection(collectionId: Long, surah: Int, ayah: Int) {
        scope.launch { repo.addToCollection(collectionId, surah, ayah) }
    }

    fun renameCollection(id: Long, newName: String) {
        scope.launch { repo.renameCollection(id, newName) }
    }

    fun removeFromCollection(collectionId: Long, surah: Int, ayah: Int) {
        scope.launch { repo.removeFromCollection(collectionId, surah, ayah) }
    }

    fun removeCollection(id: Long) {
        scope.launch { repo.removeCollection(id) }
    }
}
