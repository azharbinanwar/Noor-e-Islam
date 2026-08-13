package com.kodeelite.nooreislam.feature.notifications.scheduler

import com.kodeelite.nooreislam.core.navigation.NOTIF_ROUTE_KEY
import com.kodeelite.nooreislam.core.navigation.encodeRoute
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

@OptIn(ExperimentalForeignApi::class)
actual object LocalNotifier {
    actual fun schedule(event: NotificationEvent, title: String, body: String) {
        val content = UNMutableNotificationContent()
        content.setTitle(title)
        if (body.isNotEmpty()) content.setBody(body)
        // Same one-key contract as Android: the delegate reads this and hands it to PendingNavigation.
        encodeRoute(event.route)?.let { content.setUserInfo(mapOf(NOTIF_ROUTE_KEY to it)) }
        val date = NSDate.dateWithTimeIntervalSince1970(event.fireAtMillis / 1000.0)
        val units = NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or
                NSCalendarUnitHour or NSCalendarUnitMinute or NSCalendarUnitSecond
        val comps = NSCalendar.currentCalendar.components(units, fromDate = date)
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(comps, repeats = false)
        val request = UNNotificationRequest.requestWithIdentifier("notif_${event.slotId}", content, trigger)
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request, null)
    }

    actual fun cancelAll() {
        UNUserNotificationCenter.currentNotificationCenter().removeAllPendingNotificationRequests()
    }
}
