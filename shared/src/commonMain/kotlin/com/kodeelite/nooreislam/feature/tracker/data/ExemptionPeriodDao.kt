package com.kodeelite.nooreislam.feature.tracker.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface ExemptionPeriodDao {
    @Upsert
    suspend fun upsert(period: ExemptionPeriod)

    @Query("DELETE FROM excused_period WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM excused_period")
    suspend fun clearAll()

    /** At most one running at a time. A day closed with an endPrayer is done, so it does not count. */
    @Query(
        "SELECT * FROM excused_period WHERE endDate IS NULL OR endDate > :today " +
            "OR (endDate = :today AND endPrayer IS NULL) ORDER BY startDate DESC LIMIT 1"
    )
    suspend fun active(today: LocalDate): ExemptionPeriod?

    @Query("SELECT * FROM excused_period ORDER BY startDate DESC")
    fun observeAll(): Flow<List<ExemptionPeriod>>
}
