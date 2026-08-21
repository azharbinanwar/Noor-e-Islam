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
import com.kodeelite.nooreislam.resources.complete
import com.kodeelite.nooreislam.resources.exempt
import com.kodeelite.nooreislam.resources.partial
import com.kodeelite.nooreislam.resources.not_tracked
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** A whole day, one step above per-prayer [PrayerTrackerStatus]. Derived, never stored. */
enum class DayProgress(val labelRes: StringResource, val icon: ImageVector) {
    /** All five prayed. The only value that extends a streak. */
    Complete(Res.string.complete, Lucide.Check),
    Partial(Res.string.partial, Lucide.Minus),
    None(Res.string.not_tracked, Lucide.CircleDot),

    /** Hayd or nifas — skipped, not scored. */
    Exempt(Res.string.exempt, Lucide.Pause),
}

val DayProgress.label: String
    @Composable get() = stringResource(this.labelRes)

/** Non-composable so widgets and aggregates can read it outside composition. */
fun DayProgress.colorOf(c: AppColors): Color = when (this) {
    DayProgress.Complete -> c.success
    DayProgress.Partial -> c.warning
    DayProgress.None -> c.neutralMuted
    DayProgress.Exempt -> c.info
}

val DayProgress.color: Color
    @Composable get() = colorOf(AppTheme.colors)
