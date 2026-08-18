package com.kodeelite.nooreislam.feature.notifications.scheduler

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.navigation.NOTIF_ROUTE_KEY
import com.kodeelite.nooreislam.core.navigation.encodeRoute
import com.kodeelite.nooreislam.core.platform.AppCtx
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.notification_channel_dhikr
import com.kodeelite.nooreislam.resources.notification_channel_group
import com.kodeelite.nooreislam.resources.notification_channel_nafil
import com.kodeelite.nooreislam.resources.notification_channel_prayer
import com.kodeelite.nooreislam.resources.notification_channel_quran
import com.kodeelite.nooreislam.resources.notification_channel_verse
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform

// Fires at an alert's time and posts it. Title/body were resolved at schedule time and ride the intent;
// the channel is picked from the target so users can tune each category in system settings.
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppCtx.context = context.applicationContext
        val key = intent.getStringExtra(EXTRA_KEY) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
        val route = intent.getStringExtra(NOTIF_ROUTE_KEY)
        android.util.Log.i("MiqatNotif", "fired $key") // dev: watch in Logcat
        post(context, key, title, body, route)
    }

    private fun post(ctx: Context, key: String, title: String, body: String, route: String?) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        registerChannels(ctx)
        val channel = channelFor(key.substringBefore(':')) // target is the eventKey's first segment
        val iconId = ctx.resources.getIdentifier("ic_notification", "drawable", ctx.packageName)
        val notification = NotificationCompat.Builder(ctx, channel.id)
            .setContentTitle(title)
            .apply { if (body.isNotEmpty()) setContentText(body) }
            .setSmallIcon(if (iconId != 0) iconId else android.R.drawable.ic_popup_reminder)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(launchIntent(ctx, key, route))
            .build()
        if (NotificationManagerCompat.from(ctx).areNotificationsEnabled()) nm.notify(key.hashCode(), notification) // unique per event
    }

    // One channel per category, all under a single group, so each is tunable in system settings.
    private enum class Channel(val id: String, val nameRes: StringResource, val importance: Int) {
        PRAYER("prayer", Res.string.notification_channel_prayer, NotificationManager.IMPORTANCE_HIGH),
        QURAN("quran", Res.string.notification_channel_quran, NotificationManager.IMPORTANCE_DEFAULT),
        DHIKR("dhikr", Res.string.notification_channel_dhikr, NotificationManager.IMPORTANCE_DEFAULT),
        NAFIL("nafil", Res.string.notification_channel_nafil, NotificationManager.IMPORTANCE_DEFAULT),
        VERSE("verse", Res.string.notification_channel_verse, NotificationManager.IMPORTANCE_DEFAULT),
    }

    private fun channelFor(target: String): Channel = when (target) {
        NotificationTarget.MULK, NotificationTarget.KAHF, NotificationTarget.SURAH_REMINDER,
        NotificationTarget.DAILY_READING -> Channel.QURAN
        NotificationTarget.MORNING, NotificationTarget.EVENING -> Channel.DHIKR
        NotificationTarget.TAHAJJUD, NotificationTarget.ISHRAQ -> Channel.NAFIL
        "verse", "hadith" -> Channel.VERSE
        // fajr..isha, jumuah, test — anything unmapped still posts. The Quran app never registers
        // the prayer channel, so there it coerces to its own.
        else -> if (edition() == AppEdition.QURAN) Channel.QURAN else Channel.PRAYER
    }

    companion object {
        private const val GROUP = "reminders"
        private val RETIRED_CHANNELS = listOf("prayer_reminders", "surah") // old channel ids, deleted on run
        const val EXTRA_KEY = "key"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"

        private fun edition() = KoinPlatform.getKoin().get<AppEdition>()

        // Only the categories this edition can actually fire — the Quran app has no prayer, dhikr
        // or nafil alerts, so those channels shouldn't clutter its system settings page.
        private fun channelsFor(edition: AppEdition) =
            if (edition == AppEdition.QURAN) listOf(Channel.QURAN, Channel.VERSE) else Channel.entries.toList()

        // Create the group + this edition's channels together (lazily, on the first notification
        // that fires), so all categories show up in system settings at once. Idempotent.
        fun registerChannels(ctx: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val keep = channelsFor(edition())
            val drop = RETIRED_CHANNELS + Channel.entries.filterNot(keep::contains).map { it.id }
            drop.forEach { nm.deleteNotificationChannel(it) }
            nm.createNotificationChannelGroup(NotificationChannelGroup(GROUP, runBlocking { getString(Res.string.notification_channel_group) }))
            keep.forEach { c ->
                nm.createNotificationChannel(NotificationChannel(c.id, runBlocking { getString(c.nameRes) }, c.importance).apply { group = GROUP })
            }
        }

        fun intent(ctx: Context, e: NotificationEvent, title: String, body: String): Intent =
            Intent(ctx, NotificationReceiver::class.java)
                .putExtra(EXTRA_KEY, e.eventKey)
                .putExtra(EXTRA_TITLE, title)
                .putExtra(EXTRA_BODY, body)
                .putExtra(NOTIF_ROUTE_KEY, encodeRoute(e.route))

        // Reopens the launcher activity with the payload attached. Same one-key contract as iOS —
        // MainActivity reads it and hands the raw string to PendingNavigation.
        private fun launchIntent(ctx: Context, key: String, route: String?): PendingIntent? {
            val launch = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName) ?: return null
            launch.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            launch.putExtra(NOTIF_ROUTE_KEY, route)
            return PendingIntent.getActivity(
                ctx,
                key.hashCode(), // unique per event, so payloads don't overwrite each other
                launch,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
