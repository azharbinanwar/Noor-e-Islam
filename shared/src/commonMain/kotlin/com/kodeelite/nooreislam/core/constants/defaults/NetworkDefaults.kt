package com.kodeelite.nooreislam.core.constants.defaults

/** Ship defaults for every call the app makes. */
object NetworkDefaults {

    /** Per-install token, once the device registers. */
    const val APP_TOKEN_HEADER = "X-App-Token"

    const val REQUEST_TIMEOUT_MS = 8_000L
    const val CONNECT_TIMEOUT_MS = 5_000L

    /** A 5xx or a dropped connection only — a 4xx is our own mistake. */
    const val MAX_RETRIES = 2
}
