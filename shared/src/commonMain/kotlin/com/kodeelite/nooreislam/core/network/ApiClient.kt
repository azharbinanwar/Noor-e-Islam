package com.kodeelite.nooreislam.core.network

import com.kodeelite.nooreislam.core.BuildType
import com.kodeelite.nooreislam.core.constants.defaults.NetworkDefaults
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** The one way out of the app — headers, retries and timeouts live here, not at call sites. */
class ApiClient(
    baseUrl: String,
    buildType: BuildType,
    token: () -> String? = { null },   // per-install token, once the device registers
) {
    // internal so nothing bypasses the plugins below
    internal val base: String = baseUrl.trimEnd('/')

    internal val http: HttpClient = HttpClient {
        expectSuccess = false   // a 4xx still carries a readable envelope

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true   // a new server field must not break an old install
                    explicitNulls = false
                    coerceInputValues = true
                }
            )
        }

        install(HttpTimeout) {
            requestTimeoutMillis = NetworkDefaults.REQUEST_TIMEOUT_MS
            connectTimeoutMillis = NetworkDefaults.CONNECT_TIMEOUT_MS
        }

        // a 5xx or a dropped connection only — a 4xx is our own mistake
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = NetworkDefaults.MAX_RETRIES)
            retryOnException(maxRetries = NetworkDefaults.MAX_RETRIES, retryOnTimeout = true)
            exponentialDelay()
        }

        if (buildType.isDebug) install(Logging) { level = LogLevel.INFO }

        defaultRequest {
            contentType(ContentType.Application.Json)
            token()?.let { header(NetworkDefaults.APP_TOKEN_HEADER, it) }
        }
    }

    fun close() = http.close()
}
