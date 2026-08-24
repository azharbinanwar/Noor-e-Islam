package com.kodeelite.nooreislam.core.network

import kotlinx.serialization.Serializable

/** Every response, success or failure, so one type parses both. */
@Serializable
data class ApiEnvelope<T>(
    val success: Boolean,
    val code: Int,
    val data: T? = null,
    val reason: ApiReason? = null,
    val message: String? = null,   // English, logs only — the app translates [reason]
) {
    fun toResult(): ApiResult<T> =
        if (success && data != null) ApiResult.Ok(data)
        else ApiResult.Fail(reason ?: ApiReason.Unknown, code)
}
