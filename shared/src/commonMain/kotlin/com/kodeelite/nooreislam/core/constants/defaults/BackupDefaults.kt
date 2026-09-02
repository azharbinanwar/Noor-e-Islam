package com.kodeelite.nooreislam.core.constants.defaults

import com.kodeelite.nooreislam.core.constants.PrefConst
import com.kodeelite.nooreislam.core.enums.BackupFrequency
import com.kodeelite.nooreislam.core.enums.BackupNetwork
import kotlinx.datetime.DayOfWeek

/** Drive backup defaults: a connected account backs up nightly, on any connection, until she says otherwise. */
object BackupDefaults {
    val FREQUENCY = BackupFrequency.Daily
    val NETWORK = BackupNetwork.Any
    const val HOUR = 2          // the phone is idle and usually charging
    const val MINUTE = 0
    val WEEKDAY = DayOfWeek.FRIDAY

    /** Prefs that describe this phone rather than the person. Kept out of the file, untouched by a restore. */
    val EXCLUDED_PREFS: Set<String> = setOf(
        // the linked account and its record belong to the phone doing the restoring
        PrefConst.BACKUP_ACCOUNT,
        PrefConst.BACKUP_ACCOUNT_NAME,
        PrefConst.BACKUP_ACCOUNT_PHOTO,
        PrefConst.BACKUP_LAST_AT,
        PrefConst.BACKUP_LAST_SIZE_KB,
        // a silence window still running here must not travel and mute another phone
        PrefConst.FOCUS_SAVED_RINGER,
        PrefConst.FOCUS_SILENCE_END,
        PrefConst.FOCUS_SILENCE_MODE,
        PrefConst.FOCUS_SILENCE_LABEL,
        // where this phone is. An old city would move prayer times and the qibla somewhere the
        // reader is not; saved places still travel, so that city stays one tap away.
        PrefConst.ACTIVE_PLACE,
        // dev-only one-shot test rigs, tied to a moment rather than a setting
        PrefConst.NOTIF_TEST_SLOTS,
        PrefConst.FOCUS_TEST_SLOTS,
    )

    /** Keys built with a suffix, so they are matched by their start. */
    val EXCLUDED_PREF_PREFIXES: List<String> = listOf(
        // widget ids come from this phone's launcher: another phone's would key styles to widgets
        // that do not exist here, and leave the ones that do on their defaults
        PrefConst.WIDGET_STYLE,
    )

    fun excludesPref(key: String): Boolean =
        key in EXCLUDED_PREFS || EXCLUDED_PREF_PREFIXES.any { key.startsWith(it) }
}
