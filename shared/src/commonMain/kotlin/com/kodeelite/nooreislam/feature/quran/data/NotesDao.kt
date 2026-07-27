package com.kodeelite.nooreislam.feature.quran.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {
    @Query("SELECT * FROM note WHERE deletedAt IS NULL ORDER BY updatedAt DESC")
    fun activeFlow(): Flow<List<Note>>

    @Query("SELECT * FROM note WHERE surah = :surah AND ayah = :ayah")
    suspend fun get(surah: Int, ayah: Int): Note?

    @Upsert
    suspend fun upsert(row: Note)

    @Query("SELECT * FROM note WHERE synced = 0")
    suspend fun pending(): List<Note>

    @Query("UPDATE note SET synced = 1 WHERE surah = :surah AND ayah = :ayah")
    suspend fun markSynced(surah: Int, ayah: Int)
}
