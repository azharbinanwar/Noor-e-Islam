package com.kodeelite.nooreislam.core.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Composable
actual fun rememberGeoCoder(): GeoCoder {
    val context = LocalContext.current
    return remember(context) { AndroidGeoCoder(context) }
}

/**
 * Backed by Google's geocoder through the platform. Needs network: there is no offline table behind
 * it, and a device without Play Services may have no backend at all — hence [available].
 */
private class AndroidGeoCoder(context: Context) : GeoCoder {
    // Locale.getDefault() so the names come back in the user's language where Google has them
    private val geocoder = Geocoder(context, Locale.getDefault())

    override fun available(): Boolean = Geocoder.isPresent()

    override suspend fun reverse(latitude: Double, longitude: Double): GeoResult {
        if (!available()) return GeoResult.Fail(GeoCodeError.Unavailable, "no geocoder on this device")
        return runLookup { cb -> geocoder.reverseCompat(latitude, longitude, 1, cb) }.fold(
            onSuccess = { list ->
                list.firstOrNull()?.let { GeoResult.Ok(it.toGeoAddress()) }
                    ?: GeoResult.Fail(GeoCodeError.NoResult, "nothing mapped at this point")
            },
            onFailure = { it.asFailure() },
        )
    }

    override suspend fun search(query: String, limit: Int): List<GeoAddress> {
        if (!available() || query.isBlank()) return emptyList()
        return runLookup { cb -> geocoder.forwardCompat(query, limit, cb) }
            .getOrDefault(emptyList())
            .map { it.toGeoAddress() }
    }

    // API 33 replaced the blocking calls with a callback. Below it, the old ones still work but must
    // be kept off the main thread, and they throw on a network failure rather than returning empty.
    private suspend fun runLookup(
        call: ((Result<List<Address>>) -> Unit) -> Unit,
    ): Result<List<Address>> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { cont ->
            call { result -> if (cont.isActive) cont.resume(result) }
        }
    }
}

// the platform reports a dead connection as a plain IOException with no cause
private fun Throwable.asFailure() = GeoResult.Fail(
    if (this is IOException) GeoCodeError.NoNetwork else GeoCodeError.Failed,
    message ?: this::class.simpleName,
)

private fun Geocoder.reverseCompat(lat: Double, lng: Double, max: Int, cb: (Result<List<Address>>) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getFromLocation(lat, lng, max, listener(cb))
    } else {
        cb(runCatching { @Suppress("DEPRECATION") getFromLocation(lat, lng, max).orEmpty() })
    }
}

private fun Geocoder.forwardCompat(query: String, max: Int, cb: (Result<List<Address>>) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getFromLocationName(query, max, listener(cb))
    } else {
        cb(runCatching { @Suppress("DEPRECATION") getFromLocationName(query, max).orEmpty() })
    }
}

private fun listener(cb: (Result<List<Address>>) -> Unit) = object : Geocoder.GeocodeListener {
    override fun onGeocode(addresses: MutableList<Address>) = cb(Result.success(addresses))
    override fun onError(message: String?) = cb(Result.failure(IOException(message ?: "geocoder error")))
}

private fun Address.toGeoAddress() = GeoAddress(
    name = featureName,
    street = listOfNotNull(subThoroughfare, thoroughfare).joinToString(" ").ifBlank { null },
    subLocality = subLocality,
    locality = locality,
    subAdminArea = subAdminArea,
    adminArea = adminArea,
    postalCode = postalCode,
    country = countryName,
    countryCode = countryCode,
    timeZone = null, // Android's Address carries no timezone
    latitude = latitude,
    longitude = longitude,
    lines = (0..maxAddressLineIndex).mapNotNull { getAddressLine(it) },
)
