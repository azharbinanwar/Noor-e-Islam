package com.kodeelite.nooreislam.feature.quran.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// a user note on an ayah. Auto-id PK; surah+ayah unique.
@Entity(tableName = "note", indices = [Index(value = ["surah", "ayah"], unique = true)])
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val surah: Int,
    val ayah: Int,
    val text: String,
    val createdAt: Long,
    val updatedAt: Long,
    val synced: Boolean = false,
    val deletedAt: Long? = null,
)
