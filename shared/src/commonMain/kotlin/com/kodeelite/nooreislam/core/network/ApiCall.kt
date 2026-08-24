package com.kodeelite.nooreislam.core.network

import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException

/** A GET that answers with [ApiResult]. It never throws, so the network cannot crash a screen. */
internal suspend inline fun <reified T> ApiClient.getResult(
    path: String,
    params: Map<String, String> = emptyMap(),
): ApiResult<T> = try {
    val response: HttpResponse = http.get("$base/${path.trimStart('/')}") {
        params.forEach { (key, value) -> parameter(key, value) }
    }
    response.body<ApiEnvelope<T>>().toResult()
} catch (cancelled: CancellationException) {
    throw cancelled                              // leaving a screen is not a failure
} catch (cause: Throwable) {
    // anything else — a missing engine, a bad transform — is a failed call, not a crash
    ApiResult.Fail(cause.toReason())
}

fun Throwable.toReason(): ApiReason = when (this) {
    is HttpRequestTimeoutException, is IOException -> ApiReason.Offline
    else -> ApiReason.Unknown
}
