package com.kodeelite.nooreislam.core.enums

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.History
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Users
import com.composables.icons.lucide.X
import com.kodeelite.nooreislam.config.theme.AppColors
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.missed_prayer
import com.kodeelite.nooreislam.resources.on_time
import com.kodeelite.nooreislam.resources.prayed_kaza
import com.kodeelite.nooreislam.resources.prayed_with_jamaat
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** How a prayer was tracked. Icon/label here; colors live in AppColors (theme-aware). */
enum class PrayerTrackerStatus(val labelRes: StringResource, val icon: ImageVector) {
    PrayedOnTime(Res.string.on_time, Lucide.Check),
    PrayedWithJamaat(Res.string.prayed_with_jamaat, Lucide.Users),
    PrayedKaza(Res.string.prayed_kaza, Lucide.History),
    Missed(Res.string.missed_prayer, Lucide.X);

    /** Kaza counts — late is not skipped, so it keeps a streak alive. */
    val isPrayed: Boolean get() = this != Missed

    /** Kaza doesn't count, so making one up can't score as on time. */
    val isOnTime: Boolean get() = this == PrayedOnTime || this == PrayedWithJamaat
}

val PrayerTrackerStatus.label: String
    @Composable get() = stringResource(this.labelRes)

/** Non-composable so widgets and aggregates can read it outside composition. */
fun PrayerTrackerStatus.colorOf(c: AppColors): Color = when (this) {
    PrayerTrackerStatus.PrayedOnTime -> c.prayedColor
    PrayerTrackerStatus.PrayedWithJamaat -> c.jamaatColor
    PrayerTrackerStatus.PrayedKaza -> c.kazaColor
    PrayerTrackerStatus.Missed -> c.missedColor
}

val PrayerTrackerStatus.color: Color
    @Composable get() = colorOf(AppTheme.colors)

val PrayerTrackerStatus.onColor: Color
    @Composable get() = AppTheme.colors.let {
        when (this) {
            PrayerTrackerStatus.PrayedOnTime -> it.onPrayedColor
            PrayerTrackerStatus.PrayedWithJamaat -> it.onJamaatColor
            PrayerTrackerStatus.PrayedKaza -> it.onKazaColor
            PrayerTrackerStatus.Missed -> it.onMissedColor
        }
    }
