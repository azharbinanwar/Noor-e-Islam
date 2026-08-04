package com.kodeelite.nooreislam.feature.quran.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionAyahDao {
    @Query("SELECT * FROM collection_ayah WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    fun activeFlow(): Flow<List<CollectionAyah>>

    @Query("SELECT * FROM collection_ayah WHERE collectionId = :collectionId AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun activeFlowFor(collectionId: Long): Flow<List<CollectionAyah>>

    @Query("SELECT * FROM collection_ayah WHERE collectionId = :collectionId AND surah = :surah AND ayah = :ayah AND deletedAt IS NULL")
    suspend fun get(collectionId: Long, surah: Int, ayah: Int): CollectionAyah?

    @Upsert
    suspend fun upsert(row: CollectionAyah)

    @Query("UPDATE collection_ayah SET deletedAt = :now, updatedAt = :now, synced = 0 WHERE collectionId = :collectionId AND deletedAt IS NULL")
    suspend fun removeAllIn(collectionId: Long, now: Long)

    @Query("SELECT * FROM collection_ayah WHERE synced = 0")
    suspend fun pending(): List<CollectionAyah>

    @Query("UPDATE collection_ayah SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)
}
