package com.kodeelite.nooreislam.feature.notifications.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kodeelite.nooreislam.core.platform.AppCtx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// Silent nightly wake -> roll the window forward without the app being opened.
class NotificationRebuildReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = rebuild(context)
}

// Alarms die on reboot; rebuild.
class NotificationBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = rebuild(context)
}

// Clock or timezone changed; armed alarms target stale instants.
class NotificationTimeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = rebuild(context)
}

// Keeps the broadcast open until the alarms are actually armed. Fire-and-forget would let the
// system kill the process mid-rebuild, and the window would stay empty until the app is opened —
// worst at boot, where the schedule is empty to begin with and the system is at its busiest.
private fun BroadcastReceiver.rebuild(context: Context) {
    AppCtx.context = context.applicationContext
    val pending = goAsync()
    CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
        try {
            NotificationScheduler.rebuildNow()
        } finally {
            pending.finish()
        }
    }
}
