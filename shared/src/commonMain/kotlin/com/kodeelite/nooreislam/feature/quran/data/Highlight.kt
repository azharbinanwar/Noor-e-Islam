package com.kodeelite.nooreislam.feature.quran.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kodeelite.nooreislam.core.constants.defaults.QuranDefaults

// a colored highlight on an ayah. Auto-id PK; surah+ayah are plain columns, unique together.
// sync fields: synced flips true once the server has the row; deletedAt is a soft delete so removals sync too.
@Entity(tableName = "highlight", indices = [Index(value = ["surah", "ayah"], unique = true)])
data class Highlight(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val surah: Int,
    val ayah: Int,
    val color: HighlightColor = QuranDefaults.HIGHLIGHT_COLOR,
    val createdAt: Long,
    val updatedAt: Long,
    val synced: Boolean = false,
    val deletedAt: Long? = null,
) {
    companion object {
        // placeholder row for loading lists — feed it to the real HighlightItem with toSkeleton()
        fun skeleton() = Highlight(surah = 1, ayah = 1, createdAt = 0L, updatedAt = 0L)
    }
}
