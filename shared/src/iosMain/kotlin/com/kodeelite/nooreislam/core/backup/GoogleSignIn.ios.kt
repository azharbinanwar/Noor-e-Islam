package com.kodeelite.nooreislam.core.backup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// iOS has no Google sign-in wired yet; the backup screen stays visible but connect does nothing.
@Composable
actual fun rememberGoogleSignIn(): GoogleSignIn = remember {
    object : GoogleSignIn {
        override val available = false
        override suspend fun connect(): GoogleAccount? = null
        override suspend fun driveToken(): String? = null
        override suspend fun disconnect() {}
    }
}
