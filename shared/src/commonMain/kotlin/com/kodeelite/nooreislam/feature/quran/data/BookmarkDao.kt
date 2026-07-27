package com.kodeelite.nooreislam.feature.quran.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmark WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    fun activeFlow(): Flow<List<Bookmark>>

    @Query("SELECT * FROM bookmark WHERE surah = :surah AND ayah = :ayah")
    suspend fun get(surah: Int, ayah: Int): Bookmark?

    @Upsert
    suspend fun upsert(row: Bookmark)

    @Query("SELECT * FROM bookmark WHERE synced = 0")
    suspend fun pending(): List<Bookmark>

    @Query("UPDATE bookmark SET synced = 1 WHERE surah = :surah AND ayah = :ayah")
    suspend fun markSynced(surah: Int, ayah: Int)
}
