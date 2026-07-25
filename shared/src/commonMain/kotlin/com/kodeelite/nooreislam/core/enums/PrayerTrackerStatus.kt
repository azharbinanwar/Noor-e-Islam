package com.kodeelite.nooreislam.core.enums

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.History
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Users
import com.composables.icons.lucide.X
import com.kodeelite.nooreislam.config.theme.AppTheme

/** How a prayer was tracked. Icon/label here; colors live in AppColors (theme-aware). */
enum class PrayerTrackerStatus(val label: String, val icon: ImageVector) {
    PrayedOnTime("On time", Lucide.Check),
    PrayedWithJamaat("Jamaat", Lucide.Users),
    PrayedKaza("Kaza", Lucide.History),
    Missed("Missed", Lucide.X),
}

val PrayerTrackerStatus.color: Color
    @Composable get() = AppTheme.colors.let {
        when (this) {
            PrayerTrackerStatus.PrayedOnTime -> it.prayedColor
            PrayerTrackerStatus.PrayedWithJamaat -> it.jamaatColor
            PrayerTrackerStatus.PrayedKaza -> it.kazaColor
            PrayerTrackerStatus.Missed -> it.missedColor
        }
    }

val PrayerTrackerStatus.onColor: Color
    @Composable get() = AppTheme.colors.let {
        when (this) {
            PrayerTrackerStatus.PrayedOnTime -> it.onPrayedColor
            PrayerTrackerStatus.PrayedWithJamaat -> it.onJamaatColor
            PrayerTrackerStatus.PrayedKaza -> it.onKazaColor
            PrayerTrackerStatus.Missed -> it.onMissedColor
        }
    }
