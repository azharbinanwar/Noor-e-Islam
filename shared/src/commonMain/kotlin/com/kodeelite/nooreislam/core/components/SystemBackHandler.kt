package com.kodeelite.nooreislam.core.components

import androidx.compose.runtime.Composable

/**
 * Intercept the platform back gesture / button while [enabled]. Android delegates to the activity
 * BackHandler; iOS to the Compose UI back handler. Kept as expect/actual because the multiplatform
 * ui-backhandler artifact doesn't resolve uniformly across targets in this Compose version.
 */
@Composable
expect fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit)
