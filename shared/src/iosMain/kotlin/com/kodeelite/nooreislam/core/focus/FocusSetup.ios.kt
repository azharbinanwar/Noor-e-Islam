package com.kodeelite.nooreislam.core.focus

import androidx.compose.runtime.Composable

/** iOS has no OEM battery/auto-start problem, so nothing to set up. */
private object NoopFocusSetup : FocusSetup {
    override fun batteryUnrestricted() = true
    override fun requestBatteryUnrestricted() {}
    override fun backgroundRestricted() = false
    override fun hasSilenceAccess() = false
    override fun requestSilenceAccess() {}
}

@Composable
actual fun rememberFocusSetup(): FocusSetup = NoopFocusSetup
