package com.kodeelite.nooreislam.core.constants.defaults

import com.kodeelite.nooreislam.config.theme.ThemeChoice
import com.kodeelite.nooreislam.core.enums.DateFormatStyle
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.TimeFormat
import com.kodeelite.nooreislam.core.locale.Language

/**
 * Ship defaults for the general app settings — the values a new user gets before touching Settings.
 * Read only by `SettingsStore`, as the fallback when `PrefsService` has no saved value. Mirrors [MiqatDefaults].
 */
object SettingsDefaults {
    val theme = ThemeChoice.default          // System
    val language = Language.English          // fromCode(null) resolves here
    val timeFormat = TimeFormat.default      // 12-hour
    val gregorianDateFormat = DateFormatStyle.default
    val hijriDateFormat = DateFormatStyle.default
    const val HIJRI_OFFSET = 0
    val sehriReference = Miqat.Imsak         // cautious default; user can switch to Fajr in Ramadan
    const val STREAK_ENABLED = true           // on by default; opt out if the habit framing isn't wanted
    const val TRACK_EXCUSED_DAYS = false      // opt-in; most users never need it
}
