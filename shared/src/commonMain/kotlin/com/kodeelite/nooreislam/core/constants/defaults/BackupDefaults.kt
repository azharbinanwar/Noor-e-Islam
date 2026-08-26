package com.kodeelite.nooreislam.core.constants.defaults

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
}
