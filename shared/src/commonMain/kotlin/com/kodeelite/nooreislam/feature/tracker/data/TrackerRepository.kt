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
    private val exempt: ExemptionPeriodDao,
) {
    val history: Flow<Map<LocalDate, Map<Miqat, PrayerTrackerStatus>>> =
        prayers.observeAll().map { rows ->
            rows.groupBy { it.date }.mapValues { (_, day) -> day.associate { it.prayer to it.status } }
        }

    val exemptionPeriods: Flow<List<ExemptionPeriod>> = exempt.observeAll()

    /** Null clears it, so untracked stays distinct from missed. */
    /** Dev only: empties both tables so a case can be set up from scratch. */
    suspend fun wipe() {
        prayers.clearAll()
        exempt.clearAll()
    }

    suspend fun setStatus(date: LocalDate, prayer: Miqat, status: PrayerTrackerStatus?) {
        if (status == null) prayers.clear(date, prayer)
        else prayers.upsert(TrackedPrayer(date, prayer, status))
    }

    /**
     * No-op if one is already running, so a double tap can't create two. [last] null means
     * open-ended. The guard asks about [today], not [from]: a backdated start sits behind a period
     * that has already ended, and asking on that older day would find it and refuse.
     */
    suspend fun startExemption(
        from: LocalDate,
        last: LocalDate?,
        pauseAlerts: Boolean,
        pauseFocus: Boolean,
        fromPrayer: Miqat? = null,
        today: LocalDate = from,
    ) {
        if (exempt.active(today) != null) return
        exempt.upsert(
            ExemptionPeriod(
                startDate = from,
                endDate = last,
                startPrayer = fromPrayer,
                pauseAlerts = pauseAlerts,
                pauseFocus = pauseFocus,
            )
        )
    }

    /**
     * Ending on [to]. Without a [resumeFrom] the whole of [to] is owed again, so the range closes
     * the day before; with one, [to] is its last exempt day and prayer resumes at that prayer.
     */
    suspend fun endExemption(to: LocalDate, resumeFrom: Miqat? = null) {
        val running = exempt.active(to) ?: return
        val last = if (resumeFrom != null) to else to.minus(1, DateTimeUnit.DAY)
        if (last < running.startDate) exempt.delete(running.id)
        else exempt.upsert(running.copy(endDate = last, endPrayer = resumeFrom))
    }
}
