package com.kodeelite.nooreislam.feature.tracker.domain

import com.kodeelite.nooreislam.core.enums.DayProgress
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.PrayerTrackerStatus
import com.kodeelite.nooreislam.feature.tracker.data.ExemptionPeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

private const val ON_TIME_WINDOW_DAYS = 30

typealias PrayerHistory = Map<LocalDate, Map<Miqat, PrayerTrackerStatus>>

data class StreakStats(val current: Int, val best: Int, val onTimePercent: Int)

/** Exempt wins: a day off stays a day off even if something got logged on it. */
fun dayProgress(
    statuses: Map<Miqat, PrayerTrackerStatus>?,
    date: LocalDate,
    exempt: List<ExemptionPeriod>,
    today: LocalDate,
): DayProgress = when {
    exempt.any { it.covers(date, today) } -> DayProgress.Exempt
    statuses.isNullOrEmpty() -> DayProgress.None
    Miqat.PRAYERS.all { statuses[it]?.isPrayed == true } -> DayProgress.Complete
    else -> DayProgress.Partial
}

/** Exempt days skip: they neither extend nor break a run, and leave the on-time denominator. */
fun streakStats(history: PrayerHistory, exempt: List<ExemptionPeriod>, today: LocalDate): StreakStats {
    val progress = { date: LocalDate -> dayProgress(history[date], date, exempt, today) }

    var current = 0
    // an unfinished today is still in progress, not a broken streak
    var date = if (progress(today) == DayProgress.Complete) today else today.minusDays(1)
    while (true) {
        when (progress(date)) {
            DayProgress.Complete -> current++
            DayProgress.Exempt -> Unit
            else -> break
        }
        date = date.minusDays(1)
    }

    var best = 0
    var run = 0
    var cursor = history.keys.minOrNull() ?: today
    while (cursor <= today) {
        when (progress(cursor)) {
            DayProgress.Complete -> { run++; if (run > best) best = run }
            DayProgress.Exempt -> Unit
            else -> run = 0
        }
        cursor = cursor.plus(1, DateTimeUnit.DAY)
    }
    if (current > best) best = current

    val windowStart = today.minusDays(ON_TIME_WINDOW_DAYS - 1)
    val logged = history
        .filterKeys { it in windowStart..today && exempt.none { p -> p.covers(it, today) } }
        .values.flatMap { it.values }
    val onTimePercent = if (logged.isEmpty()) 0 else logged.count { it.isOnTime } * 100 / logged.size

    return StreakStats(current, best, onTimePercent)
}

private fun LocalDate.minusDays(days: Int) = plus(-days, DateTimeUnit.DAY)
