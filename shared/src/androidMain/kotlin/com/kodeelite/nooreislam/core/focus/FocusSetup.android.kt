package com.kodeelite.nooreislam.core.focus

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

class AndroidFocusSetup(context: Context) : FocusSetup {
    private val app: Context = context.applicationContext

    override fun batteryUnrestricted(): Boolean {
        val pm = app.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(app.packageName)
    }

    // The direct dialog needs REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, which only the main app declares.
    // Without it startActivity can "succeed" while Settings silently drops the request (Vivo does),
    // so declaration is checked up front instead of trusting the launch. The fallback lands on this
    // app's own settings page, one tap from the battery entry, rather than the every-app list.
    override fun phoneMaker(): String = android.os.Build.MANUFACTURER.lowercase()

    override fun requestBatteryUnrestricted() {
        val declared = app.checkSelfPermission(android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        if (declared) {
            val direct = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, "package:${app.packageName}".toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (launch(direct)) return
        }
        val details = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${app.packageName}".toUri())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (launch(details)) return
        launch(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    override fun backgroundRestricted(): Boolean =
        (app.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).isBackgroundRestricted

    override fun hasSilenceAccess(): Boolean = Ringer.hasDndAccess()

    override fun requestSilenceAccess() {
        launch(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun launch(i: Intent): Boolean = try {
        app.startActivity(i); true
    } catch (_: Exception) {
        false
    }
}

@Composable
actual fun rememberFocusSetup(): FocusSetup {
    val context = LocalContext.current
    return remember(context) { AndroidFocusSetup(context) }
}
