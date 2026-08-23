package com.kodeelite.nooreislam.core.location

import androidx.compose.runtime.Composable

/**
 * A resolved address. Every field is optional because the two platforms fill different ones, and
 * both leave most of them empty at sea or in open desert.
 *
 * [timeZone] is an IANA id and iOS only — Android's geocoder has no timezone at all.
 */
data class GeoAddress(
    val name: String? = null,           // the placemark's own label: a building, a landmark, a road
    val street: String? = null,
    val subLocality: String? = null,    // neighbourhood, sector, union council
    val locality: String? = null,       // the city, which is the field this all exists for
    val subAdminArea: String? = null,   // district or county
    val adminArea: String? = null,      // province or state
    val postalCode: String? = null,
    val country: String? = null,
    val countryCode: String? = null,
    val timeZone: String? = null,
    val latitude: Double? = null,       // where the placemark itself sits, not what was asked for
    val longitude: Double? = null,
    val lines: List<String> = emptyList(), // the platform's own formatted address, one entry per line
) {
    /** City and country, the only two lines worth showing a user. Null when neither came back. */
    val short: String?
        get() = listOfNotNull(locality ?: subAdminArea ?: adminArea, country)
            .takeIf { it.isNotEmpty() }?.joinToString(", ")
}

/** Why a lookup came back with nothing, so a screen can say something better than "failed". */
enum class GeoCodeError { NoNetwork, NoResult, Unavailable, Failed }

sealed interface GeoResult {
    data class Ok(val address: GeoAddress) : GeoResult
    data class Fail(val error: GeoCodeError, val message: String? = null) : GeoResult
}

/**
 * The OS address book: coordinates in, place name out, and back the other way for a search box.
 * Apple and Google resolve these from their own map data, so the names are the ones people use.
 */
interface GeoCoder {
    suspend fun reverse(latitude: Double, longitude: Double): GeoResult

    /** Text in, places out — "Lahore" gives back somewhere to put on the map. Best match first. */
    suspend fun search(query: String, limit: Int = 10): List<GeoAddress>

    /** False on a device with no geocoding backend at all, which is rare but real on Android. */
    fun available(): Boolean
}

@Composable
expect fun rememberGeoCoder(): GeoCoder
