package com.kodeelite.nooreislam.core.backup

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.enums.BackupFrequency
import com.kodeelite.nooreislam.core.enums.BackupNetwork
import com.kodeelite.nooreislam.core.platform.AppCtx
import com.kodeelite.nooreislam.feature.backup.data.BackupStore
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import java.util.concurrent.TimeUnit

actual object BackupScheduler {
    private const val SCHEDULED = "backup.scheduled"
    private const val NOW = "backup.now"

    private fun constraints() = Constraints.Builder()
        .setRequiredNetworkType(if (BackupStore.network.value == BackupNetwork.WifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
        .build()

    // WorkManager's periodic jobs cannot pin a clock time, so each run is a one-off aimed at the next slot,
    // and the worker arms the following one when it finishes
    actual fun reschedule() {
        val wm = WorkManager.getInstance(AppCtx.context)
        val frequency = BackupStore.frequency.value
        if (BackupStore.account.value == null || frequency == BackupFrequency.Off) { wm.cancelUniqueWork(SCHEDULED); return }
        val delay = (nextRunMillis(frequency) - Now.epochMillis()).coerceAtLeast(60_000L)
        val request = OneTimeWorkRequestBuilder<BackupWorker>()
            .setConstraints(constraints())
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(SCHEDULED)
            .build()
        wm.enqueueUniqueWork(SCHEDULED, ExistingWorkPolicy.REPLACE, request)
    }

    actual fun runNow() {
        val request = OneTimeWorkRequestBuilder<BackupWorker>().setConstraints(constraints()).addTag(NOW).build()
        WorkManager.getInstance(AppCtx.context).enqueueUniqueWork(NOW, ExistingWorkPolicy.KEEP, request)
    }

    actual fun cancel() {
        WorkManager.getInstance(AppCtx.context).apply { cancelUniqueWork(SCHEDULED); cancelUniqueWork(NOW) }
    }

    private fun nextRunMillis(frequency: BackupFrequency): Long {
        val tz = TimeZone.currentSystemDefault()
        val time = BackupStore.time.value
        var day = Now.date()
        if (frequency == BackupFrequency.Weekly) {
            val target = BackupStore.weekday.value
            while (day.dayOfWeek != target) day = day.plus(1, DateTimeUnit.DAY)
        }
        var at = LocalDateTime(day, time).toInstant(tz).toEpochMilliseconds()
        if (at <= Now.epochMillis()) {
            day = day.plus(if (frequency == BackupFrequency.Weekly) 7 else 1, DateTimeUnit.DAY)
            at = LocalDateTime(day, time).toInstant(tz).toEpochMilliseconds()
        }
        return at
    }
}
