package com.kodeelite.nooreislam.core.location

import com.kodeelite.nooreislam.core.constants.defaults.LocationDefaults
import com.kodeelite.nooreislam.core.network.ApiClient
import com.kodeelite.nooreislam.core.network.ApiResult
import com.kodeelite.nooreislam.core.network.getResult

/** Our geocoding endpoints. The app never names the provider behind them. */
class GeoApi(private val client: ApiClient) {

    suspend fun reverse(latitude: Double, longitude: Double): ApiResult<GeoPlace> =
        client.getResult(
            "geocode/reverse",
            mapOf("lat" to latitude.toString(), "lng" to longitude.toString()),
        )

    /** [near] and [country] only reorder — nothing is filtered out. */
    suspend fun search(
        query: String,
        near: Coordinates? = null,
        country: String? = null,
        limit: Int = LocationDefaults.SEARCH_LIMIT,
    ): ApiResult<List<GeoPlace>> = client.getResult(
        "geocode/search",
        buildMap {
            put("q", query)
            put("limit", limit.toString())
            near?.let { put("near", "${it.longitude},${it.latitude}") }   // lng,lat
            country?.takeIf { it.isNotBlank() }?.let { put("country", it) }
        },
    )
}
