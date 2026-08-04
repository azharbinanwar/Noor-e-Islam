package com.kodeelite.nooreislam.feature.quran.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// one ayah's membership in one collection. Auto-id PK; an ayah appears at most once per collection.
@Entity(
    tableName = "collection_ayah",
    indices = [Index(value = ["collectionId", "surah", "ayah"], unique = true)],
)
data class CollectionAyah(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val collectionId: Long,
    val surah: Int,
    val ayah: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val synced: Boolean = false,
    val deletedAt: Long? = null,
)
