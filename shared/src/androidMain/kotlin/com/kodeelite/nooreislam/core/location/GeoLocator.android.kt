package com.kodeelite.nooreislam.core.location

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.kodeelite.nooreislam.core.constants.defaults.LocationDefaults
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

@Composable
actual fun rememberGeoLocator(): GeoLocator {
    val context = LocalContext.current
    // the in-app dialog is launched by an Activity, so it is captured here rather than looked up later
    val activity = remember(context) { generateSequence(context) { (it as? ContextWrapper)?.baseContext }.filterIsInstance<Activity>().firstOrNull() }
    return remember(context, activity) { AndroidGeoLocator(context, activity) }
}

private class AndroidGeoLocator(private val context: Context, private val activity: Activity?) : GeoLocator {
    // recent cache -> live fix -> stale cache. An old fix beats no fix, and the
    // 32 km move gate decides whether it matters.
    override suspend fun current(): Coordinates? =
        lastKnown(maxAgeMs = LocationDefaults.MAX_FIX_AGE_MS) ?: freshFix() ?: lastKnown(maxAgeMs = null)

    private fun lastKnown(maxAgeMs: Long?): Coordinates? = try {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val loc = listOfNotNull(
            lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER),
            lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER),
        ).filter { maxAgeMs == null || System.currentTimeMillis() - it.time < maxAgeMs }
            .maxByOrNull { it.time }
        loc?.let { Coordinates(it.latitude, it.longitude) }
    } catch (e: SecurityException) {
        null // permission not granted
    }

    // nothing cached: a fresh device, a new emulator, or a reboot
    private suspend fun freshFix(): Coordinates? = try {
        withTimeout(LocationDefaults.FIX_TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                // high accuracy so this reaches GPS: balanced waits on network positioning,
                // which an emulator never supplies and a phone indoors often cannot either
                val request = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setMaxUpdateAgeMillis(LocationDefaults.MAX_FIX_AGE_MS)
                    .setDurationMillis(LocationDefaults.FIX_TIMEOUT_MS)
                    .build()

                LocationServices.getFusedLocationProviderClient(context)
                    .getCurrentLocation(request, null)
                    .addOnSuccessListener { loc ->
                        if (cont.isActive) cont.resume(loc?.let { Coordinates(it.latitude, it.longitude) })
                    }
                    .addOnFailureListener { if (cont.isActive) cont.resume(null) }
            }
        }
    } catch (timedOut: TimeoutCancellationException) {
        null
    } catch (e: SecurityException) {
        null
    } catch (e: Throwable) {
        null   // a device without Play Services
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
