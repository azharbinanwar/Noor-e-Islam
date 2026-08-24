package com.kodeelite.nooreislam.core.location

import com.kodeelite.nooreislam.core.constants.Place
import com.kodeelite.nooreislam.core.network.ApiReason
import com.kodeelite.nooreislam.core.network.ApiResult
import com.kodeelite.nooreislam.core.network.map
import com.kodeelite.nooreislam.core.platform.deviceCountryCode
import kotlinx.datetime.TimeZone

/**
 * Names a coordinate and finds cities. Our API first; the OS geocoder only when it
 * cannot answer. Timezone comes from the device — see dev/LOCATION.md.
 */
class LocationRepository(
    private val api: GeoApi,
    private val deviceZone: () -> String = { TimeZone.currentSystemDefault().id },
    private val deviceCountry: () -> String? = { deviceCountryCode() },
) {

    suspend fun resolve(at: Coordinates, fallback: GeoCoder? = null): ApiResult<Place> {
        val zone = deviceZone()
        val result = api.reverse(at.latitude, at.longitude).map { it.toPlace(zone) }
        if (result is ApiResult.Ok || !shouldFallBack(result.reasonOrNull())) return result

        // the OS geocoder can throw on either platform; a failed fallback keeps the API's reason
        val address = runCatching { fallback?.reverse(at.latitude, at.longitude) }
            .getOrNull()
            .let { it as? GeoResult.Ok }
            ?.address
            ?: return result
        return ApiResult.Ok(address.toPlace(at, zone))
    }

    /** [country] defaults to the device's, so a first run still ranks the user's own country first. */
    suspend fun search(
        query: String,
        near: Coordinates? = null,
        country: String? = null,
        fallback: GeoCoder? = null,
    ): ApiResult<List<Place>> {
        val zone = deviceZone()
        val boost = country ?: deviceCountry()
        val result = api.search(query, near, boost).map { list -> list.map { it.toPlace(zone) } }
        if (result is ApiResult.Ok || !shouldFallBack(result.reasonOrNull())) return result

        val found = runCatching { fallback?.search(query) }.getOrNull().orEmpty()
        if (found.isEmpty()) return result
        return ApiResult.Ok(found.map { it.toPlace(null, zone) })
    }

    private fun shouldFallBack(reason: ApiReason?) =
        reason != null && (reason.isUnavailable || reason == ApiReason.Offline)
}

private fun ApiResult<*>.reasonOrNull() = (this as? ApiResult.Fail)?.reason

// name can be a road or a landmark, so prefer a settlement
private fun GeoPlace.toPlace(timeZone: String) = Place(
    name = locality ?: region ?: name,
    countryCode = countryCode.orEmpty(),
    latitude = latitude,
    longitude = longitude,
    timeZone = timeZone,
)

// the OS may place the pin elsewhere, so keep the coordinate we asked about
private fun GeoAddress.toPlace(at: Coordinates?, timeZone: String) = Place(
    name = locality ?: subAdminArea ?: adminArea ?: name.orEmpty(),
    countryCode = countryCode.orEmpty(),
    latitude = at?.latitude ?: latitude ?: 0.0,
    longitude = at?.longitude ?: longitude ?: 0.0,
    timeZone = timeZone,
)
