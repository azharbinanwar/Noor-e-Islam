package com.kodeelite.nooreislam.core.datetime

import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.friday
import com.kodeelite.nooreislam.resources.monday
import com.kodeelite.nooreislam.resources.saturday
import com.kodeelite.nooreislam.resources.sunday
import com.kodeelite.nooreislam.resources.thursday
import com.kodeelite.nooreislam.resources.tuesday
import com.kodeelite.nooreislam.resources.wednesday
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.StringResource

/** Localized weekday name for a date's day-of-week. */
val DayOfWeek.labelRes: StringResource
    get() = when (this) {
        DayOfWeek.MONDAY -> Res.string.monday
        DayOfWeek.TUESDAY -> Res.string.tuesday
        DayOfWeek.WEDNESDAY -> Res.string.wednesday
        DayOfWeek.THURSDAY -> Res.string.thursday
        DayOfWeek.FRIDAY -> Res.string.friday
        DayOfWeek.SATURDAY -> Res.string.saturday
        DayOfWeek.SUNDAY -> Res.string.sunday
    }
