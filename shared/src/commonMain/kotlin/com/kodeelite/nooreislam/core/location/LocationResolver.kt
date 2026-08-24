package com.kodeelite.nooreislam.core.location

import com.kodeelite.nooreislam.core.constants.Place
import com.kodeelite.nooreislam.core.network.dataOrNull

/**
 * Was a fresh fix a real move, and if so, which city? The distance gate runs first,
 * so a fix that has not moved costs nothing.
 */
class LocationResolver(private val repository: LocationRepository) {

    /** A candidate [Place] when [fix] is a real move from [current], else null. */
    suspend fun detectMove(current: Place, fix: Coordinates, fallback: GeoCoder? = null): Place? {
        if (distanceKm(fix, current) < MOVE_THRESHOLD_KM) return null
        val candidate = repository.resolve(fix, fallback).dataOrNull() ?: return null
        return candidate.takeIf { it.name != current.name || it.countryCode != current.countryCode }
    }

    companion object {
        const val MOVE_THRESHOLD_KM = 32.0   // ~20 miles — a change of city, not drift
    }
}
