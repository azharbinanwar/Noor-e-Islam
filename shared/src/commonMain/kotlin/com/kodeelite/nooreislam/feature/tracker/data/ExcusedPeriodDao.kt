package com.kodeelite.nooreislam.feature.tracker.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExcusedPeriodDao {
    @Upsert
    suspend fun upsert(period: ExcusedPeriod)

    @Query("DELETE FROM excused_period WHERE id = :id")
    suspend fun delete(id: Long)

    /** At most one open period at a time; the UI's "Ended" action closes this one. */
    @Query("SELECT * FROM excused_period WHERE endDate IS NULL LIMIT 1")
    suspend fun open(): ExcusedPeriod?

    @Query("SELECT * FROM excused_period ORDER BY startDate DESC")
    fun observeAll(): Flow<List<ExcusedPeriod>>
}
