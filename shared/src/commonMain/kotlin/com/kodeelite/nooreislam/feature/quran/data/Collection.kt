package com.kodeelite.nooreislam.feature.quran.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// a user-named group of saved ayahs. Auto-id PK; name unique (case-insensitive) so adding by an
// existing name merges into it instead of creating a duplicate.
@Entity(tableName = "collection", indices = [Index(value = ["name"], unique = true)])
data class Collection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val synced: Boolean = false,
    val deletedAt: Long? = null,
)
