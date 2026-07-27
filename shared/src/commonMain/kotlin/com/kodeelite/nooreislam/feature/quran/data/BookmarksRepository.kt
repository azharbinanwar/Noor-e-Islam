package com.kodeelite.nooreislam.feature.quran.data

import com.kodeelite.nooreislam.core.datetime.Now
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// takes plain surah/ayah; the reader observes the active key set ("surah:ayah").
class BookmarksRepository(private val dao: BookmarksDao) {
    val keys: Flow<Set<String>> = dao.activeFlow().map { rows -> rows.map { "${it.surah}:${it.ayah}" }.toSet() }
    val active: Flow<List<Bookmark>> = dao.activeFlow()

    // add if new, revive if soft-deleted, else soft-delete
    suspend fun toggle(surah: Int, ayah: Int) {
        val now = Now.epochMillis()
        val cur = dao.get(surah, ayah)
        dao.upsert(
            when {
                cur == null -> Bookmark(surah = surah, ayah = ayah, createdAt = now, updatedAt = now)
                cur.deletedAt == null -> cur.copy(deletedAt = now, updatedAt = now, synced = false)
                else -> cur.copy(deletedAt = null, updatedAt = now, synced = false)
            }
        )
    }

    suspend fun isBookmarked(surah: Int, ayah: Int): Boolean = dao.get(surah, ayah)?.deletedAt == null

    // ponytail: server sync deferred — fields + these hooks are here; the pusher/merge lands with login.
    suspend fun pending(): List<Bookmark> = dao.pending()
    suspend fun markSynced(surah: Int, ayah: Int) = dao.markSynced(surah, ayah)
}
