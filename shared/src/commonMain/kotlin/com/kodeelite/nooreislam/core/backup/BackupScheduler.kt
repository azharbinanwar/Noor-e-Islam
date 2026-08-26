package com.kodeelite.nooreislam.core.backup

/**
 * Runs backups when the app is not on screen: the schedule from Settings, and "Back up now" once the
 * user leaves. The platform job system owns the timing and the network constraint; on failure it
 * tells the user with a notification, on success it stays quiet.
 */
expect object BackupScheduler {
    /** Re-arm from the current store values; called after any change to account, frequency, time, day or network. */
    fun reschedule()

    /** One immediate run, honouring the network choice. */
    fun runNow()

    fun cancel()
}
