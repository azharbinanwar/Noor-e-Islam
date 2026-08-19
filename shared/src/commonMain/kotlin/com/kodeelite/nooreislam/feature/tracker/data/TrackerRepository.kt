package com.kodeelite.nooreislam.feature.tracker.data

import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.PrayerTrackerStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

// one repo over both tables — streak math always needs them together
class TrackerRepository(
    private val prayers: TrackedPrayerDao,
    private val excused: ExcusedPeriodDao,
) {
    val history: Flow<Map<LocalDate, Map<Miqat, PrayerTrackerStatus>>> =
        prayers.observeAll().map { rows ->
            rows.groupBy { it.date }.mapValues { (_, day) -> day.associate { it.prayer to it.status } }
        }

    val excusedPeriods: Flow<List<ExcusedPeriod>> = excused.observeAll()

    /** Null clears it, so untracked stays distinct from missed. */
    suspend fun setStatus(date: LocalDate, prayer: Miqat, status: PrayerTrackerStatus?) {
        if (status == null) prayers.clear(date, prayer)
        else prayers.upsert(TrackedPrayer(date, prayer, status))
    }

    /** No-op if one is already open, so a double tap can't create two. */
    suspend fun startExcused(from: LocalDate) {
        if (excused.open() == null) excused.upsert(ExcusedPeriod(startDate = from))
    }

    /** Ending on [to] means [to] itself is back to normal, so the range closes the day before. */
    suspend fun endExcused(to: LocalDate) {
        val open = excused.open() ?: return
        val last = to.minus(1, DateTimeUnit.DAY)
        if (last < open.startDate) excused.delete(open.id) else excused.upsert(open.copy(endDate = last))
    }

    suspend fun deleteExcused(id: Long) = excused.delete(id)
}
