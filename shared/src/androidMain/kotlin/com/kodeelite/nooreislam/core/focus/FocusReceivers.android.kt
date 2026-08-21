package com.kodeelite.nooreislam.core.focus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.PrayerTrackerStatus
import com.kodeelite.nooreislam.core.platform.AppCtx
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.tracker.data.TrackerStore
import org.koin.core.context.GlobalContext

// Fires ~20s before a slot: starts the service, which mutes at the slot and restores at the end.
class FocusAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppCtx.context = context.applicationContext
        val start = intent.getLongExtra(PhoneSilenceService.EXTRA_START, 0L)
        val end = intent.getLongExtra(PhoneSilenceService.EXTRA_END, 0L)
        val label = intent.getStringExtra(PhoneSilenceService.EXTRA_LABEL) ?: "prayer"
        val mode = intent.getStringExtra(PhoneSilenceService.EXTRA_MODE) ?: SilenceMode.Vibrate.name
        android.util.Log.i("MiqatFocus", "alarm fired ($label/$mode), slot in ${(start - System.currentTimeMillis()) / 1000}s")
        PhoneSilencer.silence(start, end, label, mode)
    }
}

// The notification's action buttons + the end-alarm safety net.
class FocusActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppCtx.context = context.applicationContext
        when (intent.action) {
            ACTION_UNMUTE -> PhoneSilencer.unmuteNow()
            ACTION_EXTEND -> PhoneSilencer.extend()
            ACTION_MODE -> PhoneSilencer.toggleMode()
            // iOS bakes its buttons in when it schedules, so the guard lives here too, not only
            // in whether the button was drawn
            ACTION_PRAYED -> {
                logPrayed(intent.getStringExtra(PhoneSilenceService.EXTRA_LABEL))
                PhoneSilencer.unmuteNow()
            }
            // End alarm ("double alarm"): fires at the window end. If the service already restored,
            // this finds nothing saved and exits. If the OEM froze/killed the service, this restores.
            ACTION_RESTORE -> PhoneSilencer.restoreIfStuck()
        }
    }

    /** The window's own prayer, logged as prayed on time — the button only exists inside it. */
    private fun logPrayed(label: String?) {
        if (!SettingsStore.streakEnabled.value) return
        // Friday windows are labelled Jumu'ah, which is still Dhuhr as far as the tracker goes
        val prayer = if (label == "Jumu'ah") Miqat.Dhuhr else label?.let { runCatching { Miqat.valueOf(it) }.getOrNull() }
        prayer ?: return
        GlobalContext.getOrNull()?.get<TrackerStore>()?.setStatus(prayer, PrayerTrackerStatus.PrayedOnTime)
    }

    companion object {
        const val ACTION_UNMUTE = "com.kodeelite.nooreislam.focus.UNMUTE"
        const val ACTION_EXTEND = "com.kodeelite.nooreislam.focus.EXTEND"
        const val ACTION_MODE = "com.kodeelite.nooreislam.focus.MODE"
        const val ACTION_PRAYED = "com.kodeelite.nooreislam.focus.PRAYED"
        const val ACTION_RESTORE = "com.kodeelite.nooreislam.focus.RESTORE"
    }
}

// The wall clock or timezone changed under our feet; every armed alarm targets a stale instant. Re-arm.
class TimeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppCtx.context = context.applicationContext
        PhoneSilencer.rescheduleAll()
    }
}

// Fires ~00:10 nightly to roll the prayer windows forward a day.
class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppCtx.context = context.applicationContext
        PhoneSilencer.rescheduleAll()
    }
}

// Alarms don't survive a reboot; re-arm everything on boot.
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppCtx.context = context.applicationContext
        PhoneSilencer.rescheduleAll()
    }
}
