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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.kodeelite.nooreislam.core.components.AppCalendar
import com.kodeelite.nooreislam.core.components.AppCard
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.LocalDrawerState
import com.kodeelite.nooreislam.core.components.PulseDot
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.datetime.labelRes
import com.kodeelite.nooreislam.core.enums.DayProgress
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.PrayerTrackerStatus
import com.kodeelite.nooreislam.core.enums.color
import com.kodeelite.nooreislam.core.enums.label
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.miqat.domain.currentPrayer
import com.kodeelite.nooreislam.feature.miqat.presentation.components.prayerWindow
import com.kodeelite.nooreislam.feature.miqat.store.MiqatTimesStore
import com.kodeelite.nooreislam.feature.tracker.data.TrackerStore
import com.kodeelite.nooreislam.feature.tracker.domain.StreakStats
import com.kodeelite.nooreislam.feature.tracker.domain.dayProgress
import com.kodeelite.nooreislam.feature.tracker.presentation.components.TrackControl
import com.kodeelite.nooreislam.feature.tracker.presentation.components.TrackingSheet
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
import com.kodeelite.nooreislam.resources.on_time
import com.kodeelite.nooreislam.resources.prayer_tracker
import com.kodeelite.nooreislam.resources.selected_full_date
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject


private val trackablePrayers = Miqat.PRAYERS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen() {
    val tracker = koinInject<TrackerStore>()
    val clock by Now.now.collectAsState()
    val today = clock.date
    val history by tracker.history.collectAsState()
    val exempt by tracker.exempt.collectAsState()
    val stats by tracker.stats.collectAsState()

    var selected by remember { mutableStateOf(today) }
    var sheetPrayer by remember { mutableStateOf<Miqat?>(null) }

    val drawerState = LocalDrawerState.current
    val scope = rememberCoroutineScope()
    val statusColors = PrayerTrackerStatus.entries.associateWith { it.color }
    val exemptColor = DayProgress.Exempt.color
    val emptyDot = AppTheme.colors.onSurfaceVariant.copy(alpha = 0.22f)

    val selectedStatuses = history[selected].orEmpty()
    val selectedExempt = dayProgress(selectedStatuses, selected, exempt, today) == DayProgress.Exempt
    val todayTimes by MiqatTimesStore.today.collectAsState()
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val streakEnabled by SettingsStore.streakEnabled.collectAsState()
    val isToday = selected == today
    // a prayer can only be logged once its time has come; past days are all fair game
    val started: (Miqat) -> Boolean = { p ->
        !isToday || todayTimes.firstOrNull { it.miqat == p }?.at?.time?.let { it <= clock.time } == true
    }
    val currentPrayer = todayTimes.currentPrayer(clock.time)

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
            if (streakEnabled) StatsHeader(stats)

            AppCalendar(
                selected = selected,
                today = today,
                onSelect = { selected = it },
                lastSelectable = today,
                dayDots = { date ->
                    val statuses = history[date]
                    if (dayProgress(statuses, date, exempt, today) == DayProgress.Exempt) {
                        List(trackablePrayers.size) { exemptColor }
                    } else {
                        trackablePrayers.map { p ->
                            statuses?.get(p)?.let { statusColors.getValue(it) } ?: emptyDot
                        }
                    }
                },
            )

            Text(
                formatSelected(selected),
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.onSurface,
            )

            AppTileGroup(
                items = trackablePrayers.map { p ->
                    val status = selectedStatuses[p]
                    val markable = !selectedExempt && started(p)
                    AppTileItem(
                        title = p.label(selected),
                        subtitle = if (isToday) prayerWindow(todayTimes, p, timeFormat.pattern) else null,
                        selected = isToday && p == currentPrayer,
                        leadingIcon = p.icon,
                        leadingColor = AppTheme.colors.primary,
                        badge = if (isToday && p == currentPrayer) {
                            { PulseDot(color = AppTheme.colors.primary) }
                        } else null,
                        trailing = if (markable || selectedExempt) {
                            { TrackControl(status, exempt = selectedExempt) }
                        } else null,
                        onClick = if (markable) ({ sheetPrayer = p }) else null,
                    )
                }
            )
        }
    }

    sheetPrayer?.let { p ->
        TrackingSheet(
            prayer = p,
            date = selected,
            current = selectedStatuses[p],
            onSelect = { tracker.setStatus(selected, p, it); sheetPrayer = null },
            onDismiss = { sheetPrayer = null },
        )
    }
}

@Composable
private fun StatsHeader(stats: StreakStats) {
    AppCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Stat("${stats.current}", stringResource(Res.string.day_streak))
            Stat("${stats.best}", stringResource(Res.string.best))
            Stat("${stats.onTimePercent}%", stringResource(Res.string.on_time))
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
