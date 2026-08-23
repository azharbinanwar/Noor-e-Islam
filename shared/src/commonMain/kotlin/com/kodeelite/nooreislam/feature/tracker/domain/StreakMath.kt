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

/**
 * The prayer right after the last one she logged — the earliest an exemption could have begun,
 * because a prayer she prayed was never exempt. Null when nothing is logged at all.
 *
 * Gaps behind that prayer stay missed rather than becoming exempt: prayed Asr but not Fajr means
 * the exemption starts at Maghrib, not at Fajr. Read whatever the streak switch says — someone who
 * turned it off thinking that was how to pause is exactly who this has to be right for.
 */
fun resumePoint(history: PrayerHistory): Pair<LocalDate, Miqat>? {
    val lastDay = history.entries.filter { it.value.isNotEmpty() }.maxByOrNull { it.key } ?: return null
    val lastPrayer = Miqat.PRAYERS.filter { it in lastDay.value }.maxByOrNull { it.ordinal } ?: return null
    val next = Miqat.PRAYERS.firstOrNull { it.ordinal > lastPrayer.ordinal }
    // past Isha the day is spent, so the floor is the next morning
    return if (next != null) lastDay.key to next else lastDay.key.plus(1, DateTimeUnit.DAY) to Miqat.PRAYERS.first()
}

/**
 * What [date] still owes. The edges of a period are half days — one that began at Asr leaves Fajr
 * and Dhuhr hers to pray — so the whole app counts prayers here rather than assuming five.
 */
fun owedPrayers(date: LocalDate, exempt: List<ExemptionPeriod>, today: LocalDate): List<Miqat> =
    Miqat.PRAYERS.filterNot { p -> exempt.any { it.covers(date, p, today) } }

/** A day is off only when it owes nothing; otherwise it is judged on what it owed, not on five. */
fun dayProgress(
    statuses: Map<Miqat, PrayerTrackerStatus>?,
    date: LocalDate,
    exempt: List<ExemptionPeriod>,
    today: LocalDate,
): DayProgress {
    val owed = owedPrayers(date, exempt, today)
    return when {
        owed.isEmpty() -> DayProgress.Exempt
        statuses.isNullOrEmpty() -> DayProgress.None
        owed.all { statuses[it]?.isPrayed == true } -> DayProgress.Complete
        else -> DayProgress.Partial
    }
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
    // an exempt prayer leaves the denominator, not the whole day it sat in
    val logged = history
        .filterKeys { it in windowStart..today }
        .flatMap { (date, statuses) ->
            val owed = owedPrayers(date, exempt, today)
            statuses.filterKeys { it in owed }.values
        }
    val onTimePercent = if (logged.isEmpty()) 0 else logged.count { it.isOnTime } * 100 / logged.size

    return StreakStats(current, best, onTimePercent)
}

private fun LocalDate.minusDays(days: Int) = plus(-days, DateTimeUnit.DAY)
