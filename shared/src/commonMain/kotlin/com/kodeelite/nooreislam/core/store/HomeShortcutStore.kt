package com.kodeelite.nooreislam.core.store

import com.kodeelite.nooreislam.core.constants.PrefConst
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.core.prefs.PrefsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * The four routes pinned to the home action row. Stored as serialized routes, so a route
 * that no longer exists simply fails to decode and the defaults come back.
 */
object HomeShortcutStore {

    private val defaults: List<AppRoute> = listOf(AppRoute.Qibla, AppRoute.Tracker, AppRoute.Quran, AppRoute.PrayerTimes)
    private val serializer = ListSerializer(AppRoute.serializer())

    private val _pinned = MutableStateFlow(
        PrefsService.getStringOrNull(PrefConst.HOME_SHORTCUTS)
            ?.let { runCatching { Json.decodeFromString(serializer, it) }.getOrNull() }
            ?.takeIf { it.isNotEmpty() }
            ?: defaults,
    )
    val pinned: StateFlow<List<AppRoute>> = _pinned.asStateFlow()

    fun replace(index: Int, route: AppRoute) {
        val next = _pinned.value.toMutableList()
        if (index !in next.indices) return
        next[index] = route
        PrefsService.putString(PrefConst.HOME_SHORTCUTS, Json.encodeToString(serializer, next))
        _pinned.value = next
    }
}
