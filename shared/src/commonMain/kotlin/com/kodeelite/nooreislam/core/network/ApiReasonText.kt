package com.kodeelite.nooreislam.core.network

import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.error_offline
import com.kodeelite.nooreislam.resources.error_service_unavailable
import com.kodeelite.nooreislam.resources.error_try_again
import com.kodeelite.nooreislam.resources.no_place_found
import org.jetbrains.compose.resources.StringResource

/** What to tell the user. The server sends keys, we own the wording. */
fun ApiReason.messageRes(): StringResource = when (this) {
    ApiReason.Offline -> Res.string.error_offline
    ApiReason.EndpointPaused, ApiReason.TokenMissing, ApiReason.DailyCapReached ->
        Res.string.error_service_unavailable
    ApiReason.NoPlaceFound -> Res.string.no_place_found
    else -> Res.string.error_try_again   // the invalid_* keys are our own bug
}
