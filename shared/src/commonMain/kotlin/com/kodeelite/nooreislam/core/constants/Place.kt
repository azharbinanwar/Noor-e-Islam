package com.kodeelite.nooreislam.core.constants

import com.kodeelite.nooreislam.core.enums.countryLabel
import kotlinx.serialization.Serializable

/**
 * A location prayer times are computed for. The single model used everywhere — search results, GPS,
 * and the saved/active selection. [latitude]/[longitude]/[timeZone] feed the calc engine; [countryCode]
 * (ISO alpha-2) drives the country-derived method and the display label.
 */
@Serializable
data class Place(
    val name: String,
    val countryCode: String,
    val latitude: Double,
    val longitude: Double,
    val timeZone: String,
)

/** Display country label (e.g. "Pakistan, PK"), derived from countryCode — not stored, re-localizes. */
val Place.countryLabel: String get() = countryLabel(countryCode)
