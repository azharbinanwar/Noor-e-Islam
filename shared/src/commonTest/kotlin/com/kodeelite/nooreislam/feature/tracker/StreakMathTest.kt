package com.kodeelite.nooreislam.feature.tracker

import com.kodeelite.nooreislam.core.enums.DayProgress
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.PrayerTrackerStatus
import com.kodeelite.nooreislam.feature.tracker.data.ExemptionPeriod
import com.kodeelite.nooreislam.feature.tracker.domain.PrayerHistory
import com.kodeelite.nooreislam.feature.tracker.domain.dayProgress
import com.kodeelite.nooreislam.feature.tracker.domain.streakStats
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals

private val today = LocalDate(2026, 8, 19)
private fun day(offset: Int) = today.plus(offset, DateTimeUnit.DAY)

private fun allPrayed(status: PrayerTrackerStatus = PrayerTrackerStatus.PrayedOnTime) =
    Miqat.PRAYERS.associateWith { status }

private fun history(vararg days: Pair<Int, Map<Miqat, PrayerTrackerStatus>>): PrayerHistory =
    days.associate { (offset, statuses) -> day(offset) to statuses }

class StreakMathTest {

    @Test
    fun kazaKeepsTheStreakButNotThePercentage() {
        val h = history(0 to allPrayed(PrayerTrackerStatus.PrayedKaza))
        assertEquals(DayProgress.Complete, dayProgress(h[today], today, emptyList(), today))
        val stats = streakStats(h, emptyList(), today)
        assertEquals(1, stats.current)
        assertEquals(0, stats.onTimePercent)
    }

    @Test
    fun aMissedPrayerBreaksTheDay() {
        val partial = allPrayed() + (Miqat.Isha to PrayerTrackerStatus.Missed)
        assertEquals(DayProgress.Partial, dayProgress(partial, today, emptyList(), today))
    }

    @Test
    fun unfinishedTodayDoesNotBreakTheStreak() {
        // yesterday and the day before complete, today not logged at all
        val h = history(-1 to allPrayed(), -2 to allPrayed())
        assertEquals(2, streakStats(h, emptyList(), today).current)
    }

    @Test
    fun exemptDaysSkipRatherThanBreak() {
        val h = history(-8 to allPrayed(), -9 to allPrayed(), -10 to allPrayed(), 0 to allPrayed())
        val exempt = listOf(ExemptionPeriod(startDate = day(-7), endDate = day(-1)))
        // 3 before the gap + today, the exempt stretch neither breaks nor counts
        assertEquals(4, streakStats(h, exempt, today).current)
    }

    @Test
    fun runsEitherSideOfAPauseAddUp() {
        // 5 complete, 10 exempt, then 5 complete ending today
        val days = ((-19..-15) + (-4..0)).map { it to allPrayed() }
        val h = history(*days.toTypedArray())
        val exempt = listOf(ExemptionPeriod(startDate = day(-14), endDate = day(-5)))
        val stats = streakStats(h, exempt, today)
        assertEquals(10, stats.current)
        assertEquals(10, stats.best)
    }

    @Test
    fun exemptionWinsOverLoggedPrayers() {
        val exempt = listOf(ExemptionPeriod(startDate = today, endDate = null))
        assertEquals(DayProgress.Exempt, dayProgress(allPrayed(), today, exempt, today))
    }

    @Test
    fun openPeriodCoversUpToToday() {
        val exempt = listOf(ExemptionPeriod(startDate = day(-3), endDate = null))
        assertEquals(DayProgress.Exempt, dayProgress(null, day(-1), exempt, today))
        assertEquals(DayProgress.None, dayProgress(null, day(-4), exempt, today))
    }

    @Test
    fun bestSurvivesABrokenStreak() {
        val h = history(-5 to allPrayed(), -4 to allPrayed(), -3 to allPrayed(), 0 to allPrayed())
        val stats = streakStats(h, emptyList(), today)
        assertEquals(1, stats.current)
        assertEquals(3, stats.best)
    }

    @Test
    fun onTimePercentIgnoresExemptDays() {
        val h = history(0 to allPrayed(), -1 to allPrayed(PrayerTrackerStatus.PrayedKaza))
        val exempt = listOf(ExemptionPeriod(startDate = day(-1), endDate = day(-1)))
        assertEquals(100, streakStats(h, exempt, today).onTimePercent)
    }

    @Test
    fun emptyHistoryIsAllZeros() {
        val stats = streakStats(emptyMap(), emptyList(), today)
        assertEquals(0, stats.current)
        assertEquals(0, stats.best)
        assertEquals(0, stats.onTimePercent)
    }
}
