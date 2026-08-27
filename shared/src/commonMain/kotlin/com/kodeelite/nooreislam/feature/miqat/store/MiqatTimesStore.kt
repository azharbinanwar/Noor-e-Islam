package com.kodeelite.nooreislam.feature.miqat.store

import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.store.LocationStore
import com.kodeelite.nooreislam.feature.miqat.domain.MiqatEngine
import com.kodeelite.nooreislam.feature.miqat.domain.MiqatTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/**
 * Holds computed Miqat times — the only caller of the engine, so the UI reads this store, never the engine.
 * `today` recomputes whenever the settings, the active place, or the [Now] date changes — so it follows
 * a pushed or fast debug clock for free, and rolls over at midnight on its own. `timesFor` computes any other
 * day on demand (calendar).
 */
object MiqatTimesStore {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val today: StateFlow<List<MiqatTime>> = combine(
        MiqatCalculationStore.calculation,
        LocationStore.activePlace,
        Now.now.map { it.date }.distinctUntilChanged(),
    ) { calc, place, date ->
        MiqatEngine.timesFor(date, place, calc)
    }.stateIn(scope, SharingStarted.Eagerly, compute(Now.date()))

    /**
     * The day being worshipped. Isha's window runs past midnight, so until Fajr the day under way is
     * still the calendar day before — anything scored per day has to ask for this date, not [Now]'s.
     */
    val activeDate: StateFlow<LocalDate> = combine(today, Now.now) { times, now ->
        val fajr = times.firstOrNull { it.miqat == Miqat.Fajr }?.at?.time
        if (fajr != null && now.time < fajr) now.date.minus(1, DateTimeUnit.DAY) else now.date
    }.distinctUntilChanged().stateIn(scope, SharingStarted.Eagerly, Now.date())

    /** [activeDate]'s times — the list to show and score against, rather than the calendar day's. */
    val activeTimes: StateFlow<List<MiqatTime>> = combine(
        MiqatCalculationStore.calculation,
        LocationStore.activePlace,
        activeDate,
    ) { calc, place, date ->
        MiqatEngine.timesFor(date, place, calc)
    }.stateIn(scope, SharingStarted.Eagerly, compute(Now.date()))

    /** Any day on demand (calendar browsing), using the current place + settings. Holds nothing. */
    fun timesFor(date: LocalDate): List<MiqatTime> = compute(date)

    private fun compute(date: LocalDate) =
        MiqatEngine.timesFor(date, LocationStore.activePlace.value, MiqatCalculationStore.calculation.value)
}
