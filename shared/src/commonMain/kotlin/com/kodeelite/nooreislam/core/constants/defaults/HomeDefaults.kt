package com.kodeelite.nooreislam.core.constants.defaults

import com.kodeelite.nooreislam.core.navigation.AppRoute

/** Ship defaults for the home screen's pinned shortcut row. */
object HomeDefaults {
    const val SHORTCUT_MIN = 2
    const val SHORTCUT_MAX = 5

    // what a fresh install pins; anything that later fails to decode falls back to this row
    val SHORTCUTS: List<AppRoute> = listOf(AppRoute.Qibla, AppRoute.Tracker, AppRoute.Quran, AppRoute.PrayerTimes)
}
