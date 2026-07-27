package com.kodeelite.nooreislam.feature.quran.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// a saved ayah. Auto-id PK; surah+ayah are plain columns, unique together (one bookmark per ayah).
// sync fields: synced flips true once the server has the row; deletedAt is a soft delete so removals sync too.
@Entity(tableName = "bookmark", indices = [Index(value = ["surah", "ayah"], unique = true)])
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val surah: Int,
    val ayah: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val synced: Boolean = false,
    val deletedAt: Long? = null,
)
