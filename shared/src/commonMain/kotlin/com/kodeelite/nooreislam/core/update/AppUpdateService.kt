package com.kodeelite.nooreislam.core.update

// Play tells the app directly and draws its own update screen; Apple has no update API, so iOS asks
// the App Store lookup endpoint and opens the listing. Both return false rather than throwing when
// they can't tell — no network, sideloaded build, app not published yet.
expect object AppUpdateService {
    suspend fun isUpdateAvailable(): Boolean

    fun startUpdate()
}
