package com.kodeelite.nooreislam.core.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.kodeelite.nooreislam.core.constants.defaults.LocationDefaults
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.darwin.NSObject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.milliseconds

@Composable
actual fun rememberGeoLocator(): GeoLocator = remember { IosGeoLocator() }

private class IosGeoLocator : GeoLocator {
    private val manager = CLLocationManager()
    private var delegate: LocationDelegate? = null // strong ref so it survives until the callback fires

    override suspend fun current(): Coordinates? = try {
        withTimeout(LocationDefaults.FIX_TIMEOUT_MS.milliseconds) {
            if (!authorize()) null else requestFix()
        }
    } catch (timedOut: TimeoutCancellationException) {
        null   // iOS can simply never call back, and a screen must not wait forever
    }

    /** True once the user has decided and said yes. Asks only when they have not been asked. */
    private suspend fun authorize(): Boolean {
        val status = manager.authorizationStatus
        if (status != kCLAuthorizationStatusNotDetermined) return status.isGranted()

        return suspendCancellableCoroutine { cont ->
            val d = LocationDelegate(onAuthorization = { cont.resume(it.isGranted()) })
            delegate = d
            manager.delegate = d
            manager.requestWhenInUseAuthorization()
        }
    }

    private suspend fun requestFix(): Coordinates? = suspendCancellableCoroutine { cont ->
        val d = LocationDelegate(onFix = { cont.resume(it) })
        delegate = d
        manager.delegate = d
        manager.requestLocation() // one-shot
    }

    override fun servicesEnabled(): Boolean = CLLocationManager.locationServicesEnabled()

    // iOS offers no dialog and no route to the system switch, so the app's own page is the only move.
    override fun requestLocationOn() {
        NSURL.URLWithString(UIApplicationOpenSettingsURLString)?.let { UIApplication.sharedApplication.openURL(it) }
    }
}

private fun CLAuthorizationStatus.isGranted() =
    this == kCLAuthorizationStatusAuthorizedWhenInUse || this == kCLAuthorizationStatusAuthorizedAlways

/** Serves one question at a time: either the authorization answer or the fix. */
private class LocationDelegate(
    private val onAuthorization: ((CLAuthorizationStatus) -> Unit)? = null,
    private val onFix: ((Coordinates?) -> Unit)? = null,
) : NSObject(), CLLocationManagerDelegateProtocol {
    private var answered = false

    private fun once(block: () -> Unit) {
        if (answered) return
        answered = true
        block()
    }

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        val status = manager.authorizationStatus
        // fires once with the current status before the user has chosen; wait for their answer
        if (status == kCLAuthorizationStatusNotDetermined) return
        once { onAuthorization?.invoke(status) }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val loc = didUpdateLocations.lastOrNull() as? CLLocation
        once { onFix?.invoke(loc?.coordinate?.useContents { Coordinates(latitude, longitude) }) }
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        once { onFix?.invoke(null) }
    }
}
