package com.kodeelite.nooreislam.core.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.darwin.NSObject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
actual fun rememberGeoLocator(): GeoLocator = remember { IosGeoLocator() }

private class IosGeoLocator : GeoLocator {
    private val manager = CLLocationManager()
    private var delegate: LocationDelegate? = null // strong ref so it survives until the callback fires

    override suspend fun current(): Coordinates? = suspendCoroutine { cont ->
        val d = LocationDelegate(cont)
        delegate = d
        manager.delegate = d
        manager.requestLocation() // one-shot; delivered via the delegate below
    }

    override fun servicesEnabled(): Boolean = CLLocationManager.locationServicesEnabled()

    // iOS offers no dialog and no route to the system switch, so the app's own page is the only move.
    override fun requestLocationOn() {
        NSURL.URLWithString(UIApplicationOpenSettingsURLString)?.let { UIApplication.sharedApplication.openURL(it) }
    }
}

private class LocationDelegate(
    private val cont: Continuation<Coordinates?>,
) : NSObject(), CLLocationManagerDelegateProtocol {
    private var resumed = false

    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        if (resumed) return
        resumed = true
        val loc = didUpdateLocations.lastOrNull() as? CLLocation
        cont.resume(loc?.coordinate?.useContents { Coordinates(latitude, longitude) })
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        if (resumed) return
        resumed = true
        cont.resume(null)
    }
}
