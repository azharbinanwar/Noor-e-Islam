package com.kodeelite.nooreislam.core.focus

import com.kodeelite.nooreislam.core.constants.defaults.FocusDefaults
import com.kodeelite.nooreislam.core.datetime.currentDate
import com.kodeelite.nooreislam.core.datetime.currentTime
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.store.PrayerFocusStore
import com.kodeelite.nooreislam.feature.miqat.store.MiqatTimesStore
import com.kodeelite.nooreislam.feature.tracker.data.ExemptionStore
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import org.koin.mp.KoinPlatform

// Saved focus settings -> concrete silent windows. Uses the real clock, not Now, so the debug/fast
// clock can't leak into real timing.
object FocusWindows {
    private val exemption: ExemptionStore by lazy { KoinPlatform.getKoin().get<ExemptionStore>() }


    /** Enabled windows whose end is still ahead of now (today), plus all of tomorrow. */
    fun upcoming(): List<FocusWindow> {
        // the master gates the whole feature: with nothing to arm, rescheduling cancels what was armed
        if (!PrayerFocusStore.allFocus.value) return emptyList()
        val tz = TimeZone.currentSystemDefault()
        val today = currentDate()
        val nowMillis = LocalDateTime(today, currentTime()).toInstant(tz).toEpochMilliseconds()
        return windowsFor(today, tz).filter { it.endMillis > nowMillis } +
                windowsFor(today.plus(1, DateTimeUnit.DAY), tz)
    }

    private fun windowsFor(date: LocalDate, tz: TimeZone): List<FocusWindow> {
        // nothing to silence for on a day no prayer is owed
        if (exemption.blocksFocus(date)) return emptyList()
        val friday = date.dayOfWeek == DayOfWeek.FRIDAY
        val times = MiqatTimesStore.timesFor(date)
        val configs = PrayerFocusStore.configs.value
        return FocusDefaults.rows.mapNotNull { row ->
            val cfg = configs[row.key] ?: return@mapNotNull null
            if (!cfg.enabled) return@mapNotNull null
            // Jumu'ah row only on Fridays; on Friday the plain Dhuhr row yields to it (same time, no double-silence).
            if (row.friday && !friday) return@mapNotNull null
            if (!row.friday && friday && row.miqat == Miqat.Dhuhr) return@mapNotNull null
            val at = times.firstOrNull { it.miqat == row.miqat }?.at ?: return@mapNotNull null
            val start = at.toInstant(tz).plus(cfg.startAfter.minutes)
            val end = start.plus(cfg.duration.minutes)
            val label = if (row.friday) "Jumu'ah" else row.miqat.name // English name; the notification localizes later
            FocusWindow(label, cfg.mode, start.toEpochMilliseconds(), end.toEpochMilliseconds())
        }
    }
}
