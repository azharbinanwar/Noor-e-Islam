package com.kodeelite.nooreislam.feature.miqat.domain

import com.kodeelite.nooreislam.core.enums.Miqat
import kotlinx.datetime.LocalDateTime

/** One Miqat point. The engine returns a day as a List<MiqatTime>, ordered by time. */
data class MiqatTime(
    val miqat: Miqat,
    val at: LocalDateTime,   // wall-clock date+time for the place, no timezone attached
)
