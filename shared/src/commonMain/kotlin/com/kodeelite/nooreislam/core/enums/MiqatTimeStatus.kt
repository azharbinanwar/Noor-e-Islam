package com.kodeelite.nooreislam.core.enums

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.CalendarClock
import com.composables.icons.lucide.CircleDot
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.status_now
import com.kodeelite.nooreislam.resources.status_soon
import com.kodeelite.nooreislam.resources.status_upcoming
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** A prayer's timing state relative to now. Icon/label here; colors in AppColors. */
enum class MiqatTimeStatus(val labelRes: StringResource, val icon: ImageVector) {
    Current(Res.string.status_now, Lucide.CircleDot),
    Soon(Res.string.status_soon, Lucide.Clock),
    Upcoming(Res.string.status_upcoming, Lucide.CalendarClock),
}

val MiqatTimeStatus.label: String
    @Composable get() = stringResource(this.labelRes)

val MiqatTimeStatus.color: Color
    @Composable get() = AppTheme.colors.let {
        when (this) {
            MiqatTimeStatus.Current -> it.currentColor
            MiqatTimeStatus.Soon -> it.soonColor
            MiqatTimeStatus.Upcoming -> it.upcomingColor
        }
    }

val MiqatTimeStatus.onColor: Color
    @Composable get() = AppTheme.colors.let {
        when (this) {
            MiqatTimeStatus.Current -> it.onCurrentColor
            MiqatTimeStatus.Soon -> it.onSoonColor
            MiqatTimeStatus.Upcoming -> it.onUpcomingColor
        }
    }
