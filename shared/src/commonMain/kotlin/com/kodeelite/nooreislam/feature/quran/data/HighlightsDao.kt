package com.kodeelite.nooreislam.feature.quran.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HighlightsDao {
    @Query("SELECT * FROM highlight WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    fun activeFlow(): Flow<List<Highlight>>

    @Query("SELECT * FROM highlight WHERE surah = :surah AND ayah = :ayah")
    suspend fun get(surah: Int, ayah: Int): Highlight?

    @Upsert
    suspend fun upsert(row: Highlight)

    @Query("SELECT * FROM highlight WHERE synced = 0")
    suspend fun pending(): List<Highlight>

    @Query("UPDATE highlight SET synced = 1 WHERE surah = :surah AND ayah = :ayah")
    suspend fun markSynced(surah: Int, ayah: Int)
}
