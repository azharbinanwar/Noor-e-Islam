package com.kodeelite.nooreislam.core.location

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberGeoLocator(): GeoLocator {
    val context = LocalContext.current
    // the in-app dialog is launched by an Activity, so it is captured here rather than looked up later
    val activity = remember(context) { generateSequence(context) { (it as? ContextWrapper)?.baseContext }.filterIsInstance<Activity>().firstOrNull() }
    return remember(context, activity) { AndroidGeoLocator(context, activity) }
}

private class AndroidGeoLocator(private val context: Context, private val activity: Activity?) : GeoLocator {
    override suspend fun current(): Coordinates? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        return try {
            // ponytail: last-known is instant and fine for city-level prayer times. Add a live single-update
            // fallback (requestLocationUpdates / getCurrentLocation) if last-known comes back null on a device.
            val loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            loc?.let { Coordinates(it.latitude, it.longitude) }
        } catch (e: SecurityException) {
            null // permission not granted
        }
    }

    override fun servicesEnabled(): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Play Services can offer a "Turn on location?" dialog that never leaves the app. It needs an
     * Activity to show it and Play Services to be present, so the settings page is the fallback for
     * a device that has neither.
     */
    override fun requestLocationOn() {
        val host = activity ?: return openSettings()
        val request = LocationSettingsRequest.Builder()
            .addLocationRequest(LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 0).build())
            .build()
        LocationServices.getSettingsClient(host)
            .checkLocationSettings(request)
            .addOnFailureListener { e ->
                val resolution = (e as? ResolvableApiException)?.resolution
                if (resolution != null) {
                    // the answer arrives as a settings change, which the caller re-reads on resume
                    runCatching { host.startIntentSenderForResult(resolution.intentSender, 0, null, 0, 0, 0) }
                        .onFailure { openSettings() }
                } else {
                    openSettings()
                }
            }
    }

    private fun openSettings() {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
}
