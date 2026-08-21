package com.kodeelite.nooreislam.feature.tracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

// a range, not a row per day, so an ongoing period needs nothing writing a row each night.
// *Date suffix because END is a reserved SQLite keyword. The table keeps its old name: renaming it
// would quarantine every install that already has one.
@Entity(tableName = "excused_period")
data class ExemptionPeriod(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDate: LocalDate,
    val endDate: LocalDate? = null, // null = still ongoing
) {
    /** Caller passes the clock so this stays pure. */
    fun covers(date: LocalDate, today: LocalDate): Boolean =
        date >= startDate && date <= (endDate ?: today)
}
