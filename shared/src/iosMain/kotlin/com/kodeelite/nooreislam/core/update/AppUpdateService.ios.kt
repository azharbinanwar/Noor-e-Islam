package com.kodeelite.nooreislam.core.update

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.dataTaskWithURL
import platform.UIKit.UIApplication
import kotlin.coroutines.resume

// Apple ships no update API, so this asks the public App Store lookup endpoint what version is live
// and compares it to the running build. Until the app is actually published the endpoint returns an
// empty result list, which reads as "no update" — nothing to special-case.
actual object AppUpdateService {

    private val bundleId: String? get() = NSBundle.mainBundle.bundleIdentifier
    private val currentVersion: String
        get() = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: ""

    private var storeUrl: String? = null

    actual suspend fun isUpdateAvailable(): Boolean {
        val id = bundleId ?: return false
        val lookup = NSURL.URLWithString("https://itunes.apple.com/lookup?bundleId=$id") ?: return false
        val json = fetch(lookup) ?: return false

        val results = json["results"] as? List<*> ?: return false
        val app = results.firstOrNull() as? Map<*, *> ?: return false
        storeUrl = app["trackViewUrl"] as? String
        val live = app["version"] as? String ?: return false
        return isNewer(live, currentVersion)
    }

    actual fun startUpdate() {
        val url = storeUrl?.let { NSURL.URLWithString(it) }
            ?: bundleId?.let { NSURL.URLWithString("https://apps.apple.com/app/id$it") }
            ?: return
        UIApplication.sharedApplication.openURL(url)
    }

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    private suspend fun fetch(url: NSURL): Map<*, *>? = suspendCancellableCoroutine { cont ->
        val task = NSURLSession.sharedSession.dataTaskWithURL(url) { data: NSData?, _, _ ->
            val parsed = data?.let {
                runCatching { NSJSONSerialization.JSONObjectWithData(it, 0uL, null) as? Map<*, *> }.getOrNull()
            }
            if (cont.isActive) cont.resume(parsed)
        }
        cont.invokeOnCancellation { task.cancel() }
        task.resume()
    }

    // "1.2" vs "1.10" can't be compared as text, so walk the dot-separated numbers instead.
    private fun isNewer(live: String, running: String): Boolean {
        val a = live.split('.').map { it.toIntOrNull() ?: 0 }
        val b = running.split('.').map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }
}
