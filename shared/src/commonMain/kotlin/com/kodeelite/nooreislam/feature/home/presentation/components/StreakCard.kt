package com.kodeelite.nooreislam.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppCard
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.enums.DayProgress
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.color
import com.kodeelite.nooreislam.core.enums.label
import com.kodeelite.nooreislam.feature.tracker.data.TrackerStore
import com.kodeelite.nooreislam.feature.tracker.domain.dayProgress
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.koin.compose.koinInject
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.best_days_streak_and_on_time_percentage
import com.kodeelite.nooreislam.resources.day_streak
import com.kodeelite.nooreislam.resources.this_week
import com.kodeelite.nooreislam.resources.week_days
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

/** Today's tracked count, the streak, and this week's days. */
@Composable
fun StreakCard() {
    val tracker = koinInject<TrackerStore>()
    val tracked by tracker.tracked.collectAsState()
    val stats by tracker.stats.collectAsState()
    val history by tracker.history.collectAsState()
    val excused by tracker.excused.collectAsState()
    val clock by Now.now.collectAsState()
    val today = clock.date
    val total = Miqat.PRAYERS.size
    val done = tracked.count { it.value.isPrayed }
    val streak = stats.current
    val best = stats.best
    val onTimePct = stats.onTimePercent
    val todayExcused = dayProgress(tracked, today, excused, today) == DayProgress.Excused

    AppCard(padding = 18.dp, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { if (todayExcused) 1f else done / total.toFloat() },
                    modifier = Modifier.size(72.dp),
                    strokeWidth = 7.dp,
                    color = if (todayExcused) DayProgress.Excused.color else AppTheme.colors.surfaceTint,
                    trackColor = AppTheme.colors.neutralMutedContainer,
                )
                // nothing is expected on an excused day, so don't show it as 0 of 5
                if (todayExcused) {
                    Icon(DayProgress.Excused.icon, DayProgress.Excused.label, tint = DayProgress.Excused.color, modifier = Modifier.size(24.dp))
                } else {
                    Text("$done/$total", color = AppTheme.colors.onSurface, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Lucide.Flame, null, tint = AppTheme.colors.warning, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("$streak", color = AppTheme.colors.onSurface, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(Res.string.day_streak), color = AppTheme.colors.onSurfaceVariant, fontSize = 13.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(Res.string.best_days_streak_and_on_time_percentage, best, onTimePct),
                    color = AppTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(Res.string.this_week), color = AppTheme.colors.onSurfaceVariant, fontSize = 11.sp)
            val days = stringArrayResource(Res.array.week_days)
            // week_days is Monday-first, matching DayOfWeek's ordinal
            val weekStart = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                days.forEachIndexed { i, d ->
                    val date = weekStart.plus(i, DateTimeUnit.DAY)
                    val isToday = date == today
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DayDot(dayProgress(history[date], date, excused, today))
                        Text(
                            d,
                            color = if (isToday) AppTheme.colors.primary else AppTheme.colors.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayDot(progress: DayProgress) {
    if (progress == DayProgress.None) {
        Box(Modifier.size(34.dp).clip(CircleShape).border(1.5.dp, AppTheme.colors.outlineVariant, CircleShape))
    } else {
        val c = progress.color
        Box(Modifier.size(34.dp).clip(CircleShape).background(c.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
            Icon(progress.icon, progress.label, tint = c, modifier = Modifier.size(18.dp))
        }
    }
}
