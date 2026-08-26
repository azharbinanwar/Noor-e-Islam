package com.kodeelite.nooreislam.core.backup

import androidx.compose.runtime.Composable

/** The Google account a backup belongs to; only what the screen shows. */
data class GoogleAccount(val email: String, val name: String?, val photoUrl: String?)

/** The Web client id from Google Auth Platform; empty means sign-in is not configured for this build. */
data class GoogleSignInConfig(val webClientId: String)

/** Sign-in failed for a reason worth telling her; cancelling is not one. */
class GoogleSignInException(message: String) : Exception(message)

/**
 * Google sign-in for the Drive backup. Identity comes from the account picker; the Drive token is a
 * separate consent for the app-private folder, asked for the first time it is needed.
 */
interface GoogleSignIn {
    val available: Boolean

    /** Shows the account picker; null when she backs out, [GoogleSignInException] when it went wrong. */
    suspend fun connect(): GoogleAccount?

    /** A short-lived access token for the app-data folder, prompting for consent once; null if refused. */
    suspend fun driveToken(): String?

    /** Forgets the sign-in on this device. The Drive file is untouched. */
    suspend fun disconnect()
}

/** Platform-backed [GoogleSignIn], bound to the current Compose context like the permission service. */
@Composable
expect fun rememberGoogleSignIn(): GoogleSignIn
