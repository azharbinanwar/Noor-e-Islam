package com.kodeelite.nooreislam.feature.tracker.store

import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.PrayerTrackerStatus
import com.kodeelite.nooreislam.feature.tracker.store.PrayerTrackingStore.tracked
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Today's prayer tracking. In memory for now; Room-backed later. Screens read [tracked]. */
object PrayerTrackingStore {
    private val _tracked = MutableStateFlow<Map<Miqat, PrayerTrackerStatus>>(emptyMap())
    val tracked: StateFlow<Map<Miqat, PrayerTrackerStatus>> = _tracked.asStateFlow()

    fun setStatus(prayer: Miqat, status: PrayerTrackerStatus?) {
        _tracked.value = if (status == null) _tracked.value - prayer else _tracked.value + (prayer to status)
    }
}
