package com.kodeelite.nooreislam.core.platform

/**
 * What the OS itself allows, fixed for the whole run. Anything that has to ask the device — a
 * permission, a setting the user can change while the app is open — belongs to its own feature.
 */
object Platform {
    /** Apple forbids third-party apps from touching system Do Not Disturb, so Prayer Focus is Android-only. */
    val canControlDnd: Boolean get() = platformCanControlDnd

    /** iOS's widget is still a stub, so the gallery would offer something that can't be pinned. */
    val hasHomeScreenWidgets: Boolean get() = platformHasHomeScreenWidgets
}

internal expect val platformCanControlDnd: Boolean
internal expect val platformHasHomeScreenWidgets: Boolean
