package com.kodeelite.nooreislam.feature.quran.data

import com.kodeelite.nooreislam.core.datetime.Now
import kotlinx.coroutines.flow.Flow

class CollectionRepository(
    private val collectionDao: CollectionDao,
    private val ayahDao: CollectionAyahDao,
) {
    val collections: Flow<List<Collection>> = collectionDao.activeFlow()

    fun ayahsIn(collectionId: Long): Flow<List<CollectionAyah>> = ayahDao.activeFlowFor(collectionId)

    // creates the collection if a same-name one (case-insensitive) doesn't already exist, then adds the ayah to it
    suspend fun addToCollectionByName(name: String, surah: Int, ayah: Int) {
        val trimmed = name.trim()
        val now = Now.epochMillis()
        val existing = collectionDao.findByName(trimmed)
        val collectionId = existing?.id ?: collectionDao.upsert(Collection(name = trimmed, createdAt = now, updatedAt = now))
        addToCollection(collectionId, surah, ayah)
    }

    suspend fun addToCollection(collectionId: Long, surah: Int, ayah: Int) {
        val now = Now.epochMillis()
        val cur = ayahDao.get(collectionId, surah, ayah)
        ayahDao.upsert(
            (cur ?: CollectionAyah(collectionId = collectionId, surah = surah, ayah = ayah, createdAt = now, updatedAt = now))
                .copy(deletedAt = null, updatedAt = now, synced = false)
        )
    }

    // no-op if the trimmed name is blank or already taken (case-insensitive) by a different collection
    suspend fun renameCollection(id: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        val cur = collectionDao.findById(id) ?: return
        val clash = collectionDao.findByName(trimmed)
        if (clash != null && clash.id != id) return
        collectionDao.upsert(cur.copy(name = trimmed, updatedAt = Now.epochMillis(), synced = false))
    }

    suspend fun removeFromCollection(collectionId: Long, surah: Int, ayah: Int) {
        val cur = ayahDao.get(collectionId, surah, ayah) ?: return
        ayahDao.upsert(cur.copy(deletedAt = Now.epochMillis(), updatedAt = Now.epochMillis(), synced = false))
    }

    // removes the collection itself and every ayah membership in it
    suspend fun removeCollection(id: Long) {
        val now = Now.epochMillis()
        val cur = collectionDao.findById(id) ?: return
        ayahDao.removeAllIn(id, now)
        collectionDao.upsert(cur.copy(deletedAt = now, updatedAt = now, synced = false))
    }
}
