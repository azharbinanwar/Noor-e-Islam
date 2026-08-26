package com.kodeelite.nooreislam.feature.sandbox.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppButtonVariant
import com.kodeelite.nooreislam.core.components.AppCalendar
import com.kodeelite.nooreislam.core.components.AppChip
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.datetime.formatted
import com.kodeelite.nooreislam.core.enums.DayProgress
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.PrayerTrackerStatus
import com.kodeelite.nooreislam.core.enums.colorOf
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.tracker.data.TrackerRepository
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import com.kodeelite.nooreislam.feature.tracker.data.TrackerStore
import com.kodeelite.nooreislam.feature.tracker.domain.dayProgress
import com.kodeelite.nooreislam.feature.tracker.domain.resumePoint
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Dev rig for the tracker and the exemption: seed a day, wipe both tables, and read back what the
 * rules make of it. Nothing here is reachable from a store build — it hangs off Sandbox.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TrackerLabScreen() {
    val nav = LocalAppNavigator.current
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    val repo = koinInject<TrackerRepository>()
    val tracker = koinInject<TrackerStore>()
    val history by tracker.history.collectAsState()
    val periods by tracker.exempt.collectAsState()
    val dateFormat by SettingsStore.gregorianDateFormat.collectAsState()
    val today = Now.date()
    var day by remember { mutableStateOf(today) }

    val logged = history[day].orEmpty()
    val resume = resumePoint(history)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Tracker lab") },
                navigationIcon = { IconButton(onClick = { nav.back() }) { Icon(Lucide.ChevronLeft, "Back") } },
            )
        },
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppCalendar(
                selected = day,
                today = today,
                onSelect = { day = it },
                lastSelectable = today,
                dayDots = { d ->
                    val st = history[d]
                    if (dayProgress(st, d, periods, today) == DayProgress.Exempt) List(5) { c.info }
                    else Miqat.PRAYERS.map { p -> st?.get(p)?.colorOf(c) ?: c.onSurfaceVariant.copy(alpha = 0.22f) }
                },
            )

            Text(day.formatted(dateFormat), fontWeight = FontWeight.SemiBold, color = c.onSurface)

            // tap a prayer to cycle: nothing -> on time -> jamaat -> kaza -> missed -> nothing
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Miqat.PRAYERS.forEach { p ->
                    val status = logged[p]
                    AppChip(
                        label = "${p.name}${status?.let { " · ${it.name}" } ?: ""}",
                        selected = status != null,
                        onClick = { scope.launch { repo.setStatus(day, p, status.next()) } },
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppButton(
                    "Fill day",
                    onClick = {
                        scope.launch {
                            Miqat.PRAYERS.forEach { repo.setStatus(day, it, PrayerTrackerStatus.PrayedOnTime) }
                        }
                    },
                    variant = AppButtonVariant.Outline,
                    modifier = Modifier.weight(1f),
                )
                AppButton(
                    "Clear day",
                    onClick = { scope.launch { Miqat.PRAYERS.forEach { repo.setStatus(day, it, null) } } },
                    variant = AppButtonVariant.Outline,
                    modifier = Modifier.weight(1f),
                )
            }

            AppButton(
                "Seed 110 days",
                onClick = {
                    scope.launch {
                        // a long, believable streak for store shots: mostly on time, a jamaat here and there
                        for (back in 1..110) {
                            val d = today.minus(back, DateTimeUnit.DAY)
                            Miqat.PRAYERS.forEachIndexed { i, p ->
                                repo.setStatus(d, p, if ((back + i) % 6 == 0) PrayerTrackerStatus.PrayedWithJamaat else PrayerTrackerStatus.PrayedOnTime)
                            }
                        }
                        repo.setStatus(today, Miqat.PRAYERS.first(), PrayerTrackerStatus.PrayedOnTime)
                    }
                },
                variant = AppButtonVariant.Outline,
                modifier = Modifier.fillMaxWidth(),
            )

            AppButton(
                "Wipe prayers and exemptions",
                onClick = { scope.launch { repo.wipe() } },
                variant = AppButtonVariant.ErrorOutline,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(4.dp))
            // what the rules make of the data above, so a case can be checked without the sheet
            Readout("resumePoint", resume?.let { "${it.first} · ${it.second.name}" } ?: "none")
            Readout("day progress", dayProgress(logged, day, periods, today).name)
            Readout("periods", if (periods.isEmpty()) "none" else periods.joinToString("\n") {
                "${it.startDate}${it.startPrayer?.let { p -> " (from ${p.name})" } ?: ""} → " +
                        "${it.endDate ?: "open"}${it.endPrayer?.let { p -> " (resume ${p.name})" } ?: ""}"
            })
        }
    }
}

private fun PrayerTrackerStatus?.next(): PrayerTrackerStatus? = when (this) {
    null -> PrayerTrackerStatus.PrayedOnTime
    PrayerTrackerStatus.PrayedOnTime -> PrayerTrackerStatus.PrayedWithJamaat
    PrayerTrackerStatus.PrayedWithJamaat -> PrayerTrackerStatus.PrayedKaza
    PrayerTrackerStatus.PrayedKaza -> PrayerTrackerStatus.Missed
    PrayerTrackerStatus.Missed -> null
}

@Composable
private fun Readout(label: String, value: String) {
    val c = AppTheme.colors
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(c.cardColor).padding(12.dp),
    ) {
        Text(label, fontSize = 11.sp, color = c.primary, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 13.sp, color = c.onSurface)
    }
}
