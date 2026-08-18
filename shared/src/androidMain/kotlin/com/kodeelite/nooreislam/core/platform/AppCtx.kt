package com.kodeelite.nooreislam.core.platform

import android.app.Activity
import android.content.Context
import java.lang.ref.WeakReference

/** App context for background code (scheduler/receivers) that has no Composable to read LocalContext from. */
object AppCtx {
    lateinit var context: Context

    // Play's update flow has to be launched from an Activity, and the app context never unwraps to one.
    // Weak so a finished Activity can still be collected.
    private var activityRef: WeakReference<Activity>? = null
    var activity: Activity?
        get() = activityRef?.get()
        set(value) { activityRef = value?.let { WeakReference(it) } }
}
