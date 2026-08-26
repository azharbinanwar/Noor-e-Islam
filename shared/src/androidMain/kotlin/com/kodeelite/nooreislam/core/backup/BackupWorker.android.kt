package com.kodeelite.nooreislam.core.backup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.kodeelite.nooreislam.core.platform.AppCtx
import com.kodeelite.nooreislam.core.store.BackupStore
import com.kodeelite.nooreislam.feature.backup.data.BackupRepository
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.backing_up
import com.kodeelite.nooreislam.resources.backup_failed
import com.kodeelite.nooreislam.resources.backup_behind
import com.kodeelite.nooreislam.resources.backup_behind_notice
import com.kodeelite.nooreislam.resources.backup_failed_notice
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.koin.core.context.GlobalContext

/** The background backup. Same repository as the screen; the only difference is a token fetched without any UI. */
class BackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        AppCtx.context = applicationContext
        val repo = GlobalContext.getOrNull()?.get<BackupRepository>() ?: return Result.failure()
        if (BackupStore.account.value == null) return Result.success()
        // a quiet "Backing up…" while it runs makes this a foreground job the OS is slow to kill; it goes with the job
        runCatching { setForeground(progressInfo()) }
        val outcome = repo.backUpNow(HeadlessSignIn(applicationContext))
        val scheduled = tags.contains("backup.scheduled")
        if (scheduled) BackupScheduler.reschedule()
        if (outcome != BackupRepository.Outcome.Done && scheduled && BackupStore.isBehind()) notifyBehind()
        return when (outcome) {
            BackupRepository.Outcome.Done -> Result.success()
            BackupRepository.Outcome.Offline -> if (runAttemptCount < 3) Result.retry() else Result.failure().also { notifyFailure() }
            else -> Result.failure().also { notifyFailure() }
        }
    }

    // consent was given on the screen; here the token comes back silently or not at all
    private class HeadlessSignIn(private val context: Context) : GoogleSignIn {
        override val available = true
        override suspend fun connect(): GoogleAccount? = null
        override suspend fun driveToken(): String? {
            val request = AuthorizationRequest.builder().setRequestedScopes(listOf(Scope(DRIVE_APPDATA))).build()
            val result = runCatching { Identity.getAuthorizationClient(context).authorize(request).await() }.getOrNull() ?: return null
            return if (result.hasResolution()) null else result.accessToken
        }
        override suspend fun disconnect() {}
    }

    private suspend fun progressInfo(): ForegroundInfo {
        val ctx = applicationContext
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel(CHANNEL_QUIET, "Backup progress", NotificationManager.IMPORTANCE_MIN))
        val iconId = ctx.resources.getIdentifier("ic_notification", "drawable", ctx.packageName)
        val n = NotificationCompat.Builder(ctx, CHANNEL_QUIET)
            .setContentTitle(getString(Res.string.backing_up))
            .setSmallIcon(if (iconId != 0) iconId else android.R.drawable.stat_sys_upload)
            .setOngoing(true).setSilent(true).setProgress(0, 0, true)
            .build()
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q)
            ForegroundInfo(NOTIF_PROGRESS, n, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        else ForegroundInfo(NOTIF_PROGRESS, n)
    }

    private fun notifyBehind() = notify(Res.string.backup_behind, Res.string.backup_behind_notice, NOTIF_BEHIND)

    private fun notifyFailure() = notify(Res.string.backup_failed, Res.string.backup_failed_notice, NOTIF_ID)

    private fun notify(titleRes: org.jetbrains.compose.resources.StringResource, bodyRes: org.jetbrains.compose.resources.StringResource, id: Int) {
        val ctx = applicationContext
        if (!NotificationManagerCompat.from(ctx).areNotificationsEnabled()) return
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel(CHANNEL, "Backup", NotificationManager.IMPORTANCE_DEFAULT))
        val open = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)?.let {
            PendingIntent.getActivity(ctx, 0, it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        val iconId = ctx.resources.getIdentifier("ic_notification", "drawable", ctx.packageName)
        val (title, body) = runBlocking { getString(titleRes) to getString(bodyRes) }
        val n = NotificationCompat.Builder(ctx, CHANNEL)
            .setContentTitle(title).setContentText(body)
            .setSmallIcon(if (iconId != 0) iconId else android.R.drawable.stat_notify_error)
            .setContentIntent(open).setAutoCancel(true)
            .build()
        runCatching { nm.notify(id, n) }
    }

    private companion object {
        const val CHANNEL = "backup"
        const val NOTIF_ID = 4712
        const val CHANNEL_QUIET = "backup_progress"
        const val NOTIF_PROGRESS = 4713
        const val NOTIF_BEHIND = 4714
    }
}
