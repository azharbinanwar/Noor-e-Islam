package com.kodeelite.nooreislam.core.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.coroutines.resume
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLPlacemark
import platform.Foundation.NSError

@Composable
actual fun rememberGeoCoder(): GeoCoder = remember { IosGeoCoder() }

// CLGeocoder is Apple's own, and rate limited: one request at a time, roughly one a minute.
@OptIn(ExperimentalForeignApi::class)
private class IosGeoCoder : GeoCoder {
    // built on first use: composing one is enough to wedge the simulator
    private val geocoder by lazy { CLGeocoder() }

    override fun available(): Boolean = true

    override suspend fun reverse(latitude: Double, longitude: Double): GeoResult {
        val location = CLLocation(latitude, longitude)
        val (marks, error) = suspendCancellableCoroutine { cont ->
            geocoder.reverseGeocodeLocation(location) { placemarks, error ->
                cont.resume(placemarks.orEmpty().filterIsInstance<CLPlacemark>() to error)
            }
        }
        val first = marks.firstOrNull()
        return when {
            first != null -> GeoResult.Ok(first.toGeoAddress())
            error != null -> GeoResult.Fail(error.kind(), error.localizedDescription)
            else -> GeoResult.Fail(GeoCodeError.NoResult, "nothing mapped at this point")
        }
    }

    override suspend fun search(query: String, limit: Int): List<GeoAddress> {
        if (query.isBlank()) return emptyList()
        val marks: List<CLPlacemark> = suspendCancellableCoroutine { cont ->
            geocoder.geocodeAddressString(query) { placemarks, _ ->
                cont.resume(placemarks.orEmpty().filterIsInstance<CLPlacemark>())
            }
        }
        return marks.take(limit).map { it.toGeoAddress() }
    }
}

// kCLErrorNetwork is 2, kCLErrorGeocodeFoundNoResult is 8
private fun NSError.kind(): GeoCodeError = when (code) {
    2L -> GeoCodeError.NoNetwork
    8L, 9L -> GeoCodeError.NoResult
    else -> GeoCodeError.Failed
}

@OptIn(ExperimentalForeignApi::class)
private fun CLPlacemark.toGeoAddress(): GeoAddress {
    val coords = location?.coordinate?.useContents { latitude to longitude }
    return GeoAddress(
        name = name,
        street = listOfNotNull(subThoroughfare, thoroughfare).joinToString(" ").ifBlank { null },
        subLocality = subLocality,
        locality = locality,
        subAdminArea = subAdministrativeArea,
        adminArea = administrativeArea,
        postalCode = postalCode,
        country = country,
        countryCode = ISOcountryCode,
        timeZone = timeZone?.name,
        latitude = coords?.first,
        longitude = coords?.second,
        // no formatted lines without pulling in Contacts, so build the usual four
        lines = listOfNotNull(
            listOfNotNull(subThoroughfare, thoroughfare).joinToString(" ").ifBlank { null },
            subLocality,
            listOfNotNull(locality, postalCode).joinToString(" ").ifBlank { null },
            listOfNotNull(administrativeArea, country).joinToString(", ").ifBlank { null },
        ),
    )
}
