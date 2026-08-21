package com.kodeelite.nooreislam.core.focus

import com.kodeelite.nooreislam.core.store.PrayerFocusStore
import com.kodeelite.nooreislam.feature.miqat.store.MiqatTimesStore
import com.kodeelite.nooreislam.feature.tracker.data.ExemptionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform

// Re-arms the prayer alarms whenever the times (date/method/location) or the focus settings change.
// Reuses MiqatTimesStore.today — the same reactive source the home screen already recomputes from.
object FocusScheduling {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var started = false

    // Call once at app start, after AppCtx is set.
    fun start() {
        if (started) return
        started = true
        scope.launch {
            val exemption = KoinPlatform.getKoin().get<ExemptionStore>()
            combine(
                MiqatTimesStore.today, PrayerFocusStore.configs, PrayerFocusStore.allFocus, exemption.running,
            ) { _, _, all, running -> all to (running == null) }
                .distinctUntilChanged()
                .drop(1) // the initial value; app start already armed once
                // going quiet mid-window would otherwise leave the phone silent until the end
                // alarm, so give the ringer back before re-arming
                .collect { (all, noExemption) ->
                    if (!all || !noExemption) PhoneSilencer.unmuteNow()
                    PhoneSilencer.rescheduleAll()
                }
        }
    }
}
