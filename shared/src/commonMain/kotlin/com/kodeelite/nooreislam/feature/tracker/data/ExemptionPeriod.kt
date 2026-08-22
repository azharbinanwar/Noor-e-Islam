package com.kodeelite.nooreislam.feature.tracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kodeelite.nooreislam.core.enums.Miqat
import kotlinx.datetime.LocalDate

// a range, not a row per day, so an ongoing period needs nothing writing a row each night.
// *Date suffix because END is a reserved SQLite keyword. The table keeps its old name: renaming it
// would quarantine every install that already has one.
@Entity(tableName = "excused_period")
data class ExemptionPeriod(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,   // null = open-ended, ends only when she says so
    // the edges are half days: hayd rarely begins at Fajr. Null at either end means that whole day,
    // which is what every row written before this meant.
    val startPrayer: Miqat? = null,   // first exempt prayer on [startDate]
    val endPrayer: Miqat? = null,     // first prayer owed again on [endDate]
    val pauseAlerts: Boolean = true,  // what she chose to pause, so ending puts back exactly that
    val pauseFocus: Boolean = true,
) {
    /** Looking back: an open-ended period reaches no further than today. Caller passes the clock so this stays pure. */
    fun covers(date: LocalDate, today: LocalDate): Boolean =
        date >= startDate && date <= (endDate ?: today)

    /**
     * Whether [prayer] on [date] was exempt. Whole days in the middle; the two edges answer per
     * prayer, so a Fajr prayed before it began still counts and one owed after it ended still does.
     */
    fun covers(date: LocalDate, prayer: Miqat, today: LocalDate): Boolean {
        if (!covers(date, today)) return false
        if (date == startDate && startPrayer != null && prayer.ordinal < startPrayer.ordinal) return false
        if (date == endDate && endPrayer != null && prayer.ordinal >= endPrayer.ordinal) return false
        return true
    }

    /** Looking forward: an open-ended period has no far edge, so it blocks every day from its start. */
    fun blocks(date: LocalDate): Boolean =
        date >= startDate && (endDate == null || date <= endDate)

    /** Closing it early stamps [endPrayer] on its last day, so that day is already back to praying. */
    fun activeOn(today: LocalDate): Boolean =
        blocks(today) && !(endDate == today && endPrayer != null)
}
