package com.kodeelite.nooreislam.feature.tracker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.enums.DayProgress
import com.kodeelite.nooreislam.core.enums.PrayerTrackerStatus
import com.kodeelite.nooreislam.core.enums.color
import com.kodeelite.nooreislam.core.enums.label

/** The round status pill on a prayer row. Shared by Home and the tracker. */
@Composable
fun TrackControl(status: PrayerTrackerStatus?, exempt: Boolean = false) {
    when {
        exempt -> {
            val c = DayProgress.Exempt.color
            Box(Modifier.size(32.dp).clip(CircleShape).background(c.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                Icon(DayProgress.Exempt.icon, DayProgress.Exempt.label, tint = c, modifier = Modifier.size(18.dp))
            }
        }

        status != null -> {
            val c = status.color
            Box(Modifier.size(32.dp).clip(CircleShape).background(c.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                Icon(status.icon, status.label, tint = c, modifier = Modifier.size(18.dp))
            }
        }

        else -> Box(
            Modifier.size(32.dp).clip(CircleShape).border(1.dp, AppTheme.colors.outlineVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Lucide.Plus, "Track", tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}
