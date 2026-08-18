package com.kodeelite.nooreislam.core.enums

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.CircleDot
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.Pause
import com.kodeelite.nooreislam.config.theme.AppColors
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.day_complete
import com.kodeelite.nooreislam.resources.day_excused
import com.kodeelite.nooreislam.resources.day_partial
import com.kodeelite.nooreislam.resources.not_tracked
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** A whole day, one step above per-prayer [PrayerTrackerStatus]. Derived, never stored. */
enum class DayProgress(val labelRes: StringResource, val icon: ImageVector) {
    /** All five prayed. The only value that extends a streak. */
    Complete(Res.string.day_complete, Lucide.Check),
    Partial(Res.string.day_partial, Lucide.Minus),
    None(Res.string.not_tracked, Lucide.CircleDot),

    /** Hayd or nifas — skipped, not scored. */
    Excused(Res.string.day_excused, Lucide.Pause);

    val key: String get() = name.lowercase()
}

val DayProgress.label: String
    @Composable get() = stringResource(this.labelRes)

/** Non-composable so widgets and aggregates can read it outside composition. */
fun DayProgress.colorOf(c: AppColors): Color = when (this) {
    DayProgress.Complete -> c.success
    DayProgress.Partial -> c.warning
    DayProgress.None -> c.neutralMuted
    DayProgress.Excused -> c.info
}

val DayProgress.color: Color
    @Composable get() = colorOf(AppTheme.colors)

val DayProgress.onColor: Color
    @Composable get() = AppTheme.colors.let {
        when (this) {
            DayProgress.Complete -> it.onSuccess
            DayProgress.Partial -> it.onWarning
            DayProgress.None -> it.onNeutralMuted
            DayProgress.Excused -> it.onInfo
        }
    }
