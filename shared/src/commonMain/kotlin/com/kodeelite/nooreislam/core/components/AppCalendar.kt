package com.kodeelite.nooreislam.core.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.icons.WeekIcon
import com.kodeelite.nooreislam.core.locale.tr
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.month_names
import com.kodeelite.nooreislam.resources.week_days
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringArrayResource

/** How much of the calendar is on screen. A sheet usually wants [Week]; a screen can afford [Month]. */
enum class CalendarSpan { Month, Week }

/**
 * Day picker. Tap a day to select it; the arrows step by whichever [span] is shown.
 * Monday-first, matching `week_days`.
 *
 * [visible] is any date inside the month or week being shown — the arrows hand back a new one.
 */
@Composable
fun AppCalendar(
    visible: LocalDate,
    selected: LocalDate,
    today: LocalDate,
    onSelect: (LocalDate) -> Unit,
    onVisibleChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    span: CalendarSpan = CalendarSpan.Month,
    // given, the header offers a chevron to swap week for month; omitted, the span is fixed
    onSpanChange: ((CalendarSpan) -> Unit)? = null,
    dayDots: (LocalDate) -> List<Color> = { emptyList() }, // optional per-prayer dots under each day
    lastSelectable: LocalDate? = null, // days after this are dimmed and unselectable; null = any day
) {
    val c = AppTheme.colors
    val weekStart = visible.minus(visible.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)

    val cells: List<LocalDate?> = when (span) {
        CalendarSpan.Week -> List(7) { weekStart.plus(it, DateTimeUnit.DAY) }
        CalendarSpan.Month -> {
            val first = LocalDate(visible.year, visible.monthNumber, 1)
            val days = first.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).dayOfMonth
            List(first.dayOfWeek.isoDayNumber - 1) { null } + // Monday = 0
                    List(days) { LocalDate(visible.year, visible.monthNumber, it + 1) }
        }
    }

    val months = stringArrayResource(Res.array.month_names)
    val step = if (span == CalendarSpan.Week) 7 else 1
    val unit = if (span == CalendarSpan.Week) DateTimeUnit.DAY else DateTimeUnit.MONTH

    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onVisibleChange(visible.minus(step, unit)) }) {
                Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), null, tint = c.onSurface)
            }
            // the label lands in place rather than sliding: the direction is the grid's job
            AnimatedContent(
                targetState = when (span) {
                    CalendarSpan.Month -> "${months[visible.monthNumber - 1]} ${visible.year}"
                    CalendarSpan.Week -> weekLabel(weekStart, months)
                },
                transitionSpec = {
                    (fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.88f)) togetherWith
                            (fadeOut(tween(140)) + scaleOut(tween(140), targetScale = 0.88f))
                },
                modifier = Modifier.weight(1f),
                label = "calendarLabel",
            ) { text ->
                Text(
                    text,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = c.onSurface,
                )
            }
            if (onSpanChange != null) {
                IconButton(onClick = {
                    onSpanChange(if (span == CalendarSpan.Week) CalendarSpan.Month else CalendarSpan.Week)
                }) {
                    Icon(
                        if (span == CalendarSpan.Week) Lucide.CalendarDays else WeekIcon,
                        null,
                        tint = c.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            IconButton(onClick = { onVisibleChange(visible.plus(step, unit)) }) {
                Icon(tr(Lucide.ChevronRight, Lucide.ChevronLeft), null, tint = c.onSurface)
            }
        }

        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            stringArrayResource(Res.array.week_days).forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = c.onSurfaceVariant,
                )
            }
        }

        // the grid carries the direction: forward comes in from the right, back from the left.
        // animateContentSize on the outside so week-to-month grows instead of jumping.
        AnimatedContent(
            targetState = cells,
            transitionSpec = {
                val forward = (targetState.filterNotNull().firstOrNull() ?: today) >
                        (initialState.filterNotNull().firstOrNull() ?: today)
                val edge = if (forward) 1 else -1
                (slideInHorizontally(tween(260)) { w -> edge * w } + fadeIn(tween(200))) togetherWith
                        (slideOutHorizontally(tween(260)) { w -> -edge * w } + fadeOut(tween(200)))
            },
            modifier = Modifier.fillMaxWidth().animateContentSize(),
            label = "calendarGrid",
        ) { shown ->
            Column(Modifier.fillMaxWidth()) {
                shown.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        week.forEach { date ->
                            Box(Modifier.weight(1f).padding(2.dp), contentAlignment = Alignment.Center) {
                                if (date != null) DayCell(
                                    date = date,
                                    selected = date == selected,
                                    today = date == today,
                                    dots = dayDots(date),
                                    enabled = lastSelectable == null || date <= lastSelectable,
                                    onSelect = onSelect,
                                )
                            }
                        }
                        repeat(7 - week.size) { Box(Modifier.weight(1f)) } // pad short last week
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    selected: Boolean,
    today: Boolean,
    dots: List<Color>,
    enabled: Boolean,
    onSelect: (LocalDate) -> Unit,
) {
    val c = AppTheme.colors
    Column(
        Modifier.fillMaxWidth().let { if (enabled) it.clickable { onSelect(date) } else it },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        var circle = Modifier.size(32.dp).clip(CircleShape)
        if (selected) circle = circle.background(c.primary)
        if (today && !selected) circle = circle.border(1.5.dp, c.primary, CircleShape)
        Box(circle, contentAlignment = Alignment.Center) {
            Text(
                date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected || today) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    selected -> c.onPrimary
                    today -> c.primary
                    !enabled -> c.onSurfaceVariant.copy(alpha = 0.35f)
                    else -> c.onSurface
                },
            )
        }
        if (dots.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Row(Modifier.height(5.dp), horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
                dots.forEach { Box(Modifier.size(4.dp).clip(CircleShape).background(it)) }
            }
        }
    }
}

// a week that straddles two months has to name both, or the range reads as nonsense.
// Short names only here: a full month name twice would not fit between the arrows.
@Composable
private fun weekLabel(start: LocalDate, months: List<String>): String {
    val end = start.plus(6, DateTimeUnit.DAY)
    val short = { m: Int -> months[m - 1].take(3) }
    val from = "${start.dayOfMonth} ${short(start.monthNumber)}"
    val to = if (end.monthNumber == start.monthNumber) "${end.dayOfMonth}"
    else "${end.dayOfMonth} ${short(end.monthNumber)}"
    return "$from - $to"
}
