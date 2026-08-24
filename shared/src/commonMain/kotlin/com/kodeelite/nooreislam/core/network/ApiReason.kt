package com.kodeelite.nooreislam.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Why a call failed, as the server names it. [Unknown] absorbs keys added after this build. */
@Serializable
enum class ApiReason {
    @SerialName("endpoint_paused") EndpointPaused,
    @SerialName("token_missing") TokenMissing,
    @SerialName("daily_cap_reached") DailyCapReached,
    @SerialName("invalid_lat") InvalidLat,
    @SerialName("invalid_lng") InvalidLng,
    @SerialName("invalid_query") InvalidQuery,
    @SerialName("invalid_near") InvalidNear,
    @SerialName("invalid_country") InvalidCountry,
    @SerialName("no_place_found") NoPlaceFound,
    @SerialName("upstream_failed") UpstreamFailed,

    /** Never reached the server. */
    Offline,
    Unknown;

    val isUnavailable: Boolean
        get() = this == EndpointPaused || this == TokenMissing || this == DailyCapReached

    val isRetryable: Boolean
        get() = this == UpstreamFailed || this == Offline
}
