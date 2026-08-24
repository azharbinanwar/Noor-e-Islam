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

private const val FIX_TIMEOUT_MS = 12_000L
private const val MAX_FIX_AGE_MS = 5 * 60 * 1000L   // a five-minute-old fix is still the same city

private class AndroidGeoLocator(private val context: Context, private val activity: Activity?) : GeoLocator {
    override suspend fun current(): Coordinates? = lastKnown() ?: freshFix()

    // instant, and fine for city-level prayer times
    private fun lastKnown(): Coordinates? = try {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val loc = lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        loc?.let { Coordinates(it.latitude, it.longitude) }
    } catch (e: SecurityException) {
        null // permission not granted
    }

    // nothing cached: a fresh device, a new emulator, or a reboot
    private suspend fun freshFix(): Coordinates? = try {
        withTimeout(FIX_TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                val request = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                    .setMaxUpdateAgeMillis(MAX_FIX_AGE_MS)
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
