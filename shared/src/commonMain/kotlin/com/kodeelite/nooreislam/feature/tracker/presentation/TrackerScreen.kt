package com.kodeelite.nooreislam.feature.tracker.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Menu
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppCard
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.LocalDrawerState
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.datetime.labelRes
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.PrayerTrackerStatus
import com.kodeelite.nooreislam.core.enums.color
import com.kodeelite.nooreislam.core.enums.label
import com.kodeelite.nooreislam.feature.miqat.presentation.components.MonthCalendar
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.best
import com.kodeelite.nooreislam.resources.day_streak
import com.kodeelite.nooreislam.resources.gregorian_apr
import com.kodeelite.nooreislam.resources.gregorian_aug
import com.kodeelite.nooreislam.resources.gregorian_dec
import com.kodeelite.nooreislam.resources.gregorian_feb
import com.kodeelite.nooreislam.resources.gregorian_jan
import com.kodeelite.nooreislam.resources.gregorian_jul
import com.kodeelite.nooreislam.resources.gregorian_jun
import com.kodeelite.nooreislam.resources.gregorian_mar
import com.kodeelite.nooreislam.resources.gregorian_may
import com.kodeelite.nooreislam.resources.gregorian_nov
import com.kodeelite.nooreislam.resources.gregorian_oct
import com.kodeelite.nooreislam.resources.gregorian_sep
import com.kodeelite.nooreislam.resources.not_tracked
import com.kodeelite.nooreislam.resources.on_time
import com.kodeelite.nooreislam.resources.prayer_tracker
import com.kodeelite.nooreislam.resources.selected_full_date
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource

private val cycleOrder = listOf(
    PrayerTrackerStatus.PrayedOnTime,
    PrayerTrackerStatus.PrayedWithJamaat,
    PrayerTrackerStatus.PrayedKaza,
    PrayerTrackerStatus.Missed,
)
private val trackablePrayers = Miqat.PRAYERS

/**
 * Miqat tracker UI. Log each prayer's status per day; month heatmap tints days by
 * completion. Mock stats + local state until the Room repo is wired.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen() {
    val today = remember { Now.date() }
    var visible by remember { mutableStateOf(today) }
    var selected by remember { mutableStateOf(today) }
    val tracked = remember { mutableStateMapOf<Pair<LocalDate, Miqat>, PrayerTrackerStatus>() }

    val drawerState = LocalDrawerState.current
    val scope = rememberCoroutineScope()
    val statusColors = PrayerTrackerStatus.entries.associateWith { it.color }
    val emptyDot = AppTheme.colors.onSurfaceVariant.copy(alpha = 0.22f)

    fun cycle(p: Miqat) {
        val key = selected to p
        val next = when (val cur = tracked[key]) {
            null -> cycleOrder.first()
            PrayerTrackerStatus.Missed -> null
            else -> cycleOrder[cycleOrder.indexOf(cur) + 1]
        }
        if (next == null) tracked.remove(key) else tracked[key] = next
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.prayer_tracker)) },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Lucide.Menu, "Menu") }
                },
            )
        },
    ) { innerPadding ->
        Column(
            Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StatsHeader()

            MonthCalendar(
                year = visible.year,
                month = visible.monthNumber,
                selected = selected,
                today = today,
                onSelect = { selected = it },
                onPrevMonth = { visible = visible.minus(1, DateTimeUnit.MONTH) },
                onNextMonth = { visible = visible.plus(1, DateTimeUnit.MONTH) },
                dayDots = { date ->
                    trackablePrayers.map { p ->
                        val s = tracked[date to p] ?: mockStatus(date, p, today)
                        if (s != null) statusColors.getValue(s) else emptyDot
                    }
                },
            )

            Text(
                formatSelected(selected),
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.onSurface,
            )

            val items = mutableListOf<AppTileItem>()
            for (p in trackablePrayers) {
                val status = tracked[selected to p]
                val localizedTitle = p.label(selected)
                items.add(
                    AppTileItem(
                        title = localizedTitle,
                        leadingIcon = p.icon,
                        leadingColor = p.color,
                        trailing = {
                            if (status != null) {
                                Text(status.label, color = status.color, fontWeight = FontWeight.Medium)
                            } else {
                                Text(stringResource(Res.string.not_tracked), color = AppTheme.colors.onSurfaceVariant)
                            }
                        },
                        onClick = { cycle(p) },
                    )
                )
            }

            AppTileGroup(items = items)
        }
    }
}

@Composable
private fun StatsHeader() {
    AppCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            // ponytail: mock stats — wire to Room aggregates later
            Stat("12", stringResource(Res.string.day_streak))
            Stat("21", stringResource(Res.string.best))
            Stat("85%", stringResource(Res.string.on_time))
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = AppTheme.colors.primary)
        Text(label, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant)
    }
}

// mock per-prayer status for past days (deterministic); future = untracked. ponytail: replace with Room.
private fun mockStatus(date: LocalDate, p: Miqat, today: LocalDate): PrayerTrackerStatus? {
    if (date > today) return null
    return when ((date.dayOfMonth + p.ordinal * 3) % 5) {
        0, 1 -> PrayerTrackerStatus.PrayedOnTime
        2 -> PrayerTrackerStatus.PrayedWithJamaat
        3 -> PrayerTrackerStatus.Missed
        else -> PrayerTrackerStatus.PrayedKaza
    }
}

@Composable
private fun formatSelected(date: LocalDate): String {
    val day = stringResource(date.dayOfWeek.labelRes)
    val month = stringResource(
        when (date.monthNumber) {
            1 -> Res.string.gregorian_jan
            2 -> Res.string.gregorian_feb
            3 -> Res.string.gregorian_mar
            4 -> Res.string.gregorian_apr
            5 -> Res.string.gregorian_may
            6 -> Res.string.gregorian_jun
            7 -> Res.string.gregorian_jul
            8 -> Res.string.gregorian_aug
            9 -> Res.string.gregorian_sep
            10 -> Res.string.gregorian_oct
            11 -> Res.string.gregorian_nov
            12 -> Res.string.gregorian_dec
            else -> Res.string.gregorian_jan
        }
    )
    return stringResource(Res.string.selected_full_date, day, date.dayOfMonth, month, date.year)
}
