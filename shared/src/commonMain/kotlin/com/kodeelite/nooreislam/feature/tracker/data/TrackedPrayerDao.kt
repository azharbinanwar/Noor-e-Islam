package com.kodeelite.nooreislam.feature.tracker.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.kodeelite.nooreislam.core.enums.Miqat
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** DAO funcs are suspend (KMP requirement); Flow queries observe changes. */
@Dao
interface TrackedPrayerDao {
    @Upsert
    suspend fun upsert(entry: TrackedPrayer)

    @Query("DELETE FROM tracked_prayer WHERE date = :date AND prayer = :prayer")
    suspend fun clear(date: LocalDate, prayer: Miqat)

    @Query("DELETE FROM tracked_prayer")
    suspend fun clearAll()

    // ponytail: whole history in one Flow — 5 rows a day stays trivial for years.
    // Switch to a date-bounded query if a stats screen ever needs more than the calendar shows.
    @Query("SELECT * FROM tracked_prayer")
    fun observeAll(): Flow<List<TrackedPrayer>>
}
