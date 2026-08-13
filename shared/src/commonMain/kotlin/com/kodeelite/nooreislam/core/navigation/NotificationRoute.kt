package com.kodeelite.nooreislam.core.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/**
 * Where a notification tap should land, carried as one JSON string under [NOTIF_ROUTE_KEY].
 *
 * The whole tap path is: platform reads that one key -> [PendingNavigation.offer] -> AppNavHost
 * navigates. No platform code inspects the payload, and nothing branches on notification type —
 * a reminder that should go somewhere sets a route, one that shouldn't leaves it null.
 */
const val NOTIF_ROUTE_KEY = "noor.route"

private val json = Json { ignoreUnknownKeys = true; classDiscriminator = "type" }

fun encodeRoute(route: AppRoute?): String? = route?.let { json.encodeToString(AppRoute.serializer(), it) }

/** Null for anything unreadable — an old payload after an update must never crash the launch. */
fun decodeRoute(payload: String?): AppRoute? =
    payload?.takeIf { it.isNotBlank() }?.let { runCatching { json.decodeFromString(AppRoute.serializer(), it) }.getOrNull() }

/**
 * Holds a tapped notification's destination until the nav host is composed and can consume it.
 * State, not an event: a cold-start tap arrives long before the graph exists.
 */
object PendingNavigation {
    private val _route = MutableStateFlow<AppRoute?>(null)
    val route: StateFlow<AppRoute?> = _route.asStateFlow()

    /** Platform entry points call this with the raw payload string. */
    fun offer(payload: String?) {
        decodeRoute(payload)?.let { _route.value = it }
    }

    fun consume() {
        _route.value = null
    }
}
