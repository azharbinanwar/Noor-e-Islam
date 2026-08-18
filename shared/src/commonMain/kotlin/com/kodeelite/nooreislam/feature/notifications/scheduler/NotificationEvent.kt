package com.kodeelite.nooreislam.feature.notifications.scheduler

import com.kodeelite.nooreislam.core.database.ScheduledNotificationEntity
import com.kodeelite.nooreislam.core.enums.NotificationType
import com.kodeelite.nooreislam.core.navigation.AppRoute

// A concrete alert to fire: identity + when + how. Compute output, LocalNotifier input, mirrored to the DB.
data class NotificationEvent(
    val eventKey: String,
    val target: String,
    val kind: NotificationType,
    val fireAtMillis: Long,
    val slotId: Int = -1,
    // The user's own words, for reminders they named themselves. Null = copy comes from resources.
    val title: String? = null,
    // Where tapping lands. Null = just open the app. The only thing the tap path reads.
    val route: AppRoute? = null,
)

// Logical target keys for the non-prayer alerts (prayers use Miqat.key, Jumu'ah uses Miqat.jumuahKey).
object NotificationTarget {
    const val MULK = "mulk"
    const val KAHF = "kahf"
    const val DAILY_READING = "daily_reading"
    const val MORNING = "morning_adhkar"
    const val EVENING = "evening_adhkar"
    const val TAHAJJUD = "tahajjud"
    const val ISHRAQ = "ishraq"
    const val SURAH_REMINDER = "surah_reminder"
}

fun NotificationEvent.toEntity(title: String, body: String) = ScheduledNotificationEntity(
    slotId = slotId,
    eventKey = eventKey,
    target = target,
    kind = kind.name,
    fireAtMillis = fireAtMillis,
    title = title,
    body = body,
)
