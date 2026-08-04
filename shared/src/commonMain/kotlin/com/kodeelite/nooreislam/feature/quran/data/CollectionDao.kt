package com.kodeelite.nooreislam.feature.quran.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collection WHERE deletedAt IS NULL ORDER BY name ASC")
    fun activeFlow(): Flow<List<Collection>>

    @Query("SELECT * FROM collection WHERE name = :name AND deletedAt IS NULL")
    suspend fun findByName(name: String): Collection?

    @Query("SELECT * FROM collection WHERE id = :id")
    suspend fun findById(id: Long): Collection?

    // returns the row's id (new or existing) — CollectionAyah needs it as a FK
    @Upsert
    suspend fun upsert(row: Collection): Long

    @Query("SELECT * FROM collection WHERE synced = 0")
    suspend fun pending(): List<Collection>

    @Query("UPDATE collection SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)
}
