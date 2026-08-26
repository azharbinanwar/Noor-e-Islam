package com.kodeelite.nooreislam.core.focus

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kodeelite.nooreislam.core.constants.PrefConst
import com.kodeelite.nooreislam.core.datetime.format
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.platform.AppCtx
import com.kodeelite.nooreislam.core.prefs.PrefsService
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.focus_extend
import com.kodeelite.nooreislam.resources.focus_prayed
import com.kodeelite.nooreislam.resources.focus_unmute
import com.kodeelite.nooreislam.resources.notif_focus_body
import com.kodeelite.nooreislam.resources.notif_focus_title
import com.kodeelite.nooreislam.resources.prayer_focus
import com.kodeelite.nooreislam.resources.prayer_jumuah
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.getString

/**
 * The pinned "phone is silenced" notification with its +5 / Prayed / Unmute buttons. Plain ongoing
 * notification, no service behind it: the OS owns it once posted, the buttons are broadcasts, and the
 * end alarm takes it down. Nothing here needs the app's process to stay alive.
 */
object FocusNotification {
    const val EXTRA_START = "start"
    const val EXTRA_END = "end"
    const val EXTRA_LABEL = "label"
    const val EXTRA_MODE = "mode"
    private const val CHANNEL = "prayer_focus"
    private const val NOTIF_ID = 4711

    fun show(end: Long, label: String) {
        val ctx = AppCtx.context
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel(CHANNEL, "Prayer Focus", NotificationManager.IMPORTANCE_LOW))
        }
        if (!NotificationManagerCompat.from(ctx).areNotificationsEnabled()) return // silence still happens; only the buttons are lost
        // follow the app's chosen language; a cold-start receiver never saw the UI's locale
        PrefsService.getStringOrNull(PrefConst.LANGUAGE)?.let { java.util.Locale.setDefault(java.util.Locale(it)) }
        val pattern = SettingsStore.timeFormat.value.pattern
        fun action(a: String, code: Int) = PendingIntent.getBroadcast(
            ctx, code,
            // the label rides along so Prayed knows which prayer it is logging
            Intent(ctx, FocusActionReceiver::class.java).setAction(a).putExtra(EXTRA_LABEL, label),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val (title, body, unmute, extend, prayed) = runBlocking {
            listOf(
                getString(Res.string.notif_focus_title, prayerName(label)),
                getString(Res.string.notif_focus_body, fmt(end, pattern)),
                getString(Res.string.focus_unmute),
                getString(Res.string.focus_extend),
                getString(Res.string.focus_prayed),
            )
        }
        val iconId = ctx.resources.getIdentifier("ic_notification", "drawable", ctx.packageName)
        // button order = add order: +5 min | Prayed | Unmute, so Unmute sits rightmost under the thumb
        val notif = NotificationCompat.Builder(ctx, CHANNEL)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(if (iconId != 0) iconId else android.R.drawable.ic_lock_silent_mode)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(0, extend, action(FocusActionReceiver.ACTION_EXTEND, 2))
            .apply { if (SettingsStore.streakEnabled.value) addAction(0, prayed, action(FocusActionReceiver.ACTION_PRAYED, 4)) }
            .addAction(0, unmute, action(FocusActionReceiver.ACTION_UNMUTE, 1))
            .build()
        runCatching { nm.notify(NOTIF_ID, notif) }
    }

    fun hide() {
        (AppCtx.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIF_ID)
    }

    private suspend fun prayerName(label: String): String = when (label) {
        "Jumu'ah" -> getString(Res.string.prayer_jumuah)
        "prayer" -> getString(Res.string.prayer_focus)
        else -> runCatching { getString(Miqat.valueOf(label).labelRes) }.getOrDefault(label)
    }

    private fun fmt(ms: Long, pattern: String): String {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = ms }
        return LocalTime(cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE)).format(pattern)
    }
}
