package com.kodeelite.nooreislam.feature.miqat.domain

import com.kodeelite.nooreislam.core.enums.Miqat
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

/** One Miqat point. The engine returns a day as a List<MiqatTime>, ordered by time. */
data class MiqatTime(
    val miqat: Miqat,
    val at: LocalDateTime,   // wall-clock date+time for the place, no timezone attached
)

/** The prayer being observed right now, or null in the sunrise-to-Dhuhr gap. */
fun List<MiqatTime>.currentPrayer(now: LocalTime): Miqat? {
    val prayers = filter { it.miqat.isPrayer }
    // before Fajr it's still last night's Isha
    val started = (prayers.lastOrNull { it.at.time <= now } ?: prayers.lastOrNull())?.miqat ?: return null
    val sunrise = firstOrNull { it.miqat == Miqat.Sunrise }?.at?.time
    return if (started == Miqat.Fajr && sunrise != null && now >= sunrise) null else started
}

/** When [prayer]'s window closes today, for showing a start-to-end range. */
fun List<MiqatTime>.endOf(prayer: Miqat): LocalTime? =
    prayer.endsAt?.let { end -> firstOrNull { it.miqat == end }?.at?.time }
