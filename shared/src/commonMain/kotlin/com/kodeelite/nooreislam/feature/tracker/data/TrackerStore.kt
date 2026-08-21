package com.kodeelite.nooreislam.feature.tracker.data

import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.PrayerTrackerStatus
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.tracker.domain.PrayerHistory
import com.kodeelite.nooreislam.feature.tracker.domain.StreakStats
import com.kodeelite.nooreislam.feature.tracker.domain.streakStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class TrackerStore(
    private val scope: CoroutineScope,
    private val repo: TrackerRepository,
) {
    // off Now rather than a captured value, so everything rolls over at midnight on its own
    private val today = Now.now.map { it.date }.distinctUntilChanged()

    val history: StateFlow<PrayerHistory> =
        repo.history.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val exempt: StateFlow<List<ExemptionPeriod>> =
        repo.exemptionPeriods.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tracked: StateFlow<Map<Miqat, PrayerTrackerStatus>> =
        combine(repo.history, today) { h, d -> h[d].orEmpty() }
            .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val stats: StateFlow<StreakStats> =
        combine(repo.history, repo.exemptionPeriods, today) { h, e, d -> streakStats(h, e, d) }
            .stateIn(scope, SharingStarted.WhileSubscribed(5000), StreakStats(0, 0, 0))

    fun setStatus(date: LocalDate, prayer: Miqat, status: PrayerTrackerStatus?) {
        scope.launch { repo.setStatus(date, prayer, status) }
    }

    fun setStatus(prayer: Miqat, status: PrayerTrackerStatus?) = setStatus(Now.date(), prayer, status)

    /** The streak is a score. It reads the exemption, it never ends one. */
    fun setStreakEnabled(on: Boolean) = SettingsStore.setStreakEnabled(on)
}
