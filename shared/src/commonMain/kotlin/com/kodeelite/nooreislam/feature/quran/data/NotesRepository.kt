package com.kodeelite.nooreislam.feature.quran.data

import com.kodeelite.nooreislam.core.datetime.Now
import kotlinx.coroutines.flow.Flow

class NotesRepository(private val dao: NotesDao) {
    val active: Flow<List<Note>> = dao.activeFlow()

    suspend fun get(surah: Int, ayah: Int): Note? = dao.get(surah, ayah)

    suspend fun set(surah: Int, ayah: Int, text: String) {
        val now = Now.epochMillis()
        val cur = dao.get(surah, ayah)
        dao.upsert(
            (cur ?: Note(surah = surah, ayah = ayah, text = text, createdAt = now, updatedAt = now))
                .copy(text = text, deletedAt = null, updatedAt = now, synced = false)
        )
    }

    suspend fun remove(surah: Int, ayah: Int) {
        val cur = dao.get(surah, ayah) ?: return
        dao.upsert(cur.copy(deletedAt = Now.epochMillis(), updatedAt = Now.epochMillis(), synced = false))
    }

    suspend fun pending(): List<Note> = dao.pending()
    suspend fun markSynced(surah: Int, ayah: Int) = dao.markSynced(surah, ayah)
}
