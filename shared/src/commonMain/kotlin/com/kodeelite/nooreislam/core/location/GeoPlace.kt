package com.kodeelite.nooreislam.core.location

import kotlinx.serialization.Serializable

/** A place as the API returns it. [GeoAddress] stays the app's own shape. */
@Serializable
data class GeoPlace(
    val id: String,
    val name: String,
    val locality: String? = null,
    val region: String? = null,
    val country: String? = null,
    val countryCode: String? = null,
    val latitude: Double,
    val longitude: Double,
) {
    fun toAddress(): GeoAddress = GeoAddress(
        name = name,
        locality = locality,
        adminArea = region,
        country = country,
        countryCode = countryCode,
        latitude = latitude,
        longitude = longitude,
    )
}
