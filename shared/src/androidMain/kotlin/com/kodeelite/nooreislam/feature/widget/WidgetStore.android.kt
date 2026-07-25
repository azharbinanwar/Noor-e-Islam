package com.kodeelite.nooreislam.feature.widget

import android.content.Context
import com.kodeelite.nooreislam.core.platform.AppCtx

actual object WidgetStore {
    private const val PREFS = "nooreislam_widget"
    private const val KEY = "snapshot"
    private fun prefs() = AppCtx.context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    actual fun write(json: String) = prefs().edit().putString(KEY, json).apply()
    actual fun read(): String? = prefs().getString(KEY, null)
}
