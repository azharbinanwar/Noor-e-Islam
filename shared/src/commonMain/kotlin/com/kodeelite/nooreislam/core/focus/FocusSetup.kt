package com.kodeelite.nooreislam.core.focus

import androidx.compose.runtime.Composable

// Battery + auto-start toggles so the service survives on aggressive OEMs (Vivo/Xiaomi). No-op on iOS.
interface FocusSetup {
    val supported: Boolean
    fun batteryUnrestricted(): Boolean
    // The user actively restricted background use (App info's battery toggle). Distinct from the
    // exemption above: OEMs (Vivo) expose this toggle but give no path to the AOSP whitelist.
    fun backgroundRestricted(): Boolean
    fun requestBatteryUnrestricted()
    fun hasSilenceAccess(): Boolean // Do Not Disturb access, needed for Silent (Vibrate needs nothing)
    fun requestSilenceAccess()
}

@Composable
expect fun rememberFocusSetup(): FocusSetup
