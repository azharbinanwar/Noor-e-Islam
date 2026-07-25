package com.example.miqatapp.feature.studio.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.miqatapp.core.datetime.Now

// A persisted design. config stored as JSON (StudioConfig.toJson); isDraft = the single resume-later row.
@Entity(tableName = "studio_creation")
data class StudioCreationEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val configJson: String,
    val createdAt: Long,
    val isDraft: Boolean = false,
)

@Dao
interface StudioCreationDao {
    @Query("SELECT * FROM studio_creation WHERE isDraft = 0 ORDER BY createdAt DESC")
    suspend fun listCreations(): List<StudioCreationEntity>

    @Query("SELECT * FROM studio_creation WHERE isDraft = 1 LIMIT 1")
    suspend fun getDraft(): StudioCreationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(row: StudioCreationEntity): Long

    @Query("DELETE FROM studio_creation WHERE rowId = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM studio_creation WHERE isDraft = 1")
    suspend fun clearDraft()
}

// UI-facing model (config already parsed).
data class StudioCreation(val id: Long, val config: StudioConfig, val createdAt: Long)

/** Persists saved designs + the resume-later draft. Repo in, repo out: objects both ways. */
class StudioCreationRepository(private val dao: StudioCreationDao) {
    // skip rows that no longer parse (config schema drifted across app versions) instead of crashing
    suspend fun list(): List<StudioCreation> =
        dao.listCreations().mapNotNull { row ->
            runCatching { StudioCreation(row.rowId, StudioConfig.fromJson(row.configJson), row.createdAt) }.getOrNull()
        }

    suspend fun save(config: StudioConfig) {
        dao.insert(StudioCreationEntity(configJson = config.toJson(), createdAt = Now.epochMillis()))
    }

    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun saveDraft(config: StudioConfig) {
        dao.clearDraft()
        dao.insert(StudioCreationEntity(configJson = config.toJson(), createdAt = Now.epochMillis(), isDraft = true))
    }

    // a stale/unparseable draft is purged rather than crashing the studio on open
    suspend fun loadDraft(): StudioConfig? =
        dao.getDraft()?.let { row -> runCatching { StudioConfig.fromJson(row.configJson) }.getOrElse { clearDraft(); null } }

    suspend fun clearDraft() = dao.clearDraft()
}
