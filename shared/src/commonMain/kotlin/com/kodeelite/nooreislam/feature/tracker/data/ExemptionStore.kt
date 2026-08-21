package com.kodeelite.nooreislam.feature.tracker.data

import com.kodeelite.nooreislam.core.constants.PrefConst
import com.kodeelite.nooreislam.core.constants.defaults.ExemptionDefaults
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.prefs.PrefsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * The one place that answers "is prayer owed on this day". The schedulers read [blocks] while they
 * build, so nothing here touches an alarm or a notification switch — a day simply produces no events.
 *
 * Eagerly shared, because the schedulers read [running] synchronously from a cold process.
 */
class ExemptionStore(
    private val scope: CoroutineScope,
    private val repo: TrackerRepository,
) {
    private val today = Now.now.map { it.date }.distinctUntilChanged()

    /** The period covering today, or null. Lapses on its own when its last day passes. */
    val running: StateFlow<ExemptionPeriod?> =
        combine(repo.exemptionPeriods, today) { periods, d -> periods.firstOrNull { it.activeOn(d) } }
            .onEach(::mirror)
            .stateIn(scope, SharingStarted.Eagerly, null)

    val on: StateFlow<Boolean> =
        running.map { it != null }.stateIn(scope, SharingStarted.Eagerly, false)

    private val _lastDays = MutableStateFlow(
        PrefsService.getInt(PrefConst.EXEMPTION_DAYS, ExemptionDefaults.DAYS),
    )

    /** What the sheet opens on: her last length, or the default until she picks one. */
    val lastDays: StateFlow<Int> = _lastDays.asStateFlow()

    /** [days] null starts an open-ended one. A [days] of 1 means today only. */
    fun start(days: Int?, pauseAlerts: Boolean, pauseFocus: Boolean) {
        // only a real length is worth remembering; open-ended says nothing about how long she needs
        days?.let {
            PrefsService.putInt(PrefConst.EXEMPTION_DAYS, it)
            _lastDays.value = it
        }
        val from = Now.date()
        val last = days?.let { from.plus(it - 1, DateTimeUnit.DAY) }
        scope.launch { repo.startExemption(from, last, pauseAlerts, pauseFocus) }
    }

    fun end() {
        scope.launch { repo.endExemption(Now.date()) }
    }

    /**
     * Whether a scheduler should skip [date]. Read from the prefs cache, not [running]: a boot or
     * nightly receiver arms alarms in a cold process, where the Room flow has not emitted yet and
     * [running] would still say null — which would arm a window on an exempt day.
     */
    fun blocksAlerts(date: LocalDate): Boolean = blocks(date, PrefConst.EXEMPTION_PAUSE_ALERTS)

    fun blocksFocus(date: LocalDate): Boolean = blocks(date, PrefConst.EXEMPTION_PAUSE_FOCUS)

    private fun blocks(date: LocalDate, pauseKey: String): Boolean {
        if (!PrefsService.getBoolean(pauseKey, false)) return false
        val from = PrefsService.getStringOrNull(PrefConst.EXEMPTION_FROM)?.let(LocalDate::parse) ?: return false
        val to = PrefsService.getStringOrNull(PrefConst.EXEMPTION_TO)?.let(LocalDate::parse)
        return date >= from && (to == null || date <= to)
    }

    private fun mirror(period: ExemptionPeriod?) {
        if (period == null) {
            listOf(
                PrefConst.EXEMPTION_FROM, PrefConst.EXEMPTION_TO,
                PrefConst.EXEMPTION_PAUSE_ALERTS, PrefConst.EXEMPTION_PAUSE_FOCUS,
            ).forEach(PrefsService::remove)
            return
        }
        PrefsService.putString(PrefConst.EXEMPTION_FROM, period.startDate.toString())
        period.endDate?.let { PrefsService.putString(PrefConst.EXEMPTION_TO, it.toString()) }
            ?: PrefsService.remove(PrefConst.EXEMPTION_TO)
        PrefsService.putBoolean(PrefConst.EXEMPTION_PAUSE_ALERTS, period.pauseAlerts)
        PrefsService.putBoolean(PrefConst.EXEMPTION_PAUSE_FOCUS, period.pauseFocus)
    }
}
