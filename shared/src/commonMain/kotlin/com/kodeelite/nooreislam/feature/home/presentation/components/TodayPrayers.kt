package com.kodeelite.nooreislam.feature.home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.PulseDot
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.datetime.format
import com.kodeelite.nooreislam.core.enums.DayProgress
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.MiqatTimeStatus
import com.kodeelite.nooreislam.core.enums.color
import com.kodeelite.nooreislam.core.enums.label
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.miqat.domain.MiqatTime
import com.kodeelite.nooreislam.feature.miqat.domain.currentPrayer
import com.kodeelite.nooreislam.feature.miqat.presentation.components.prayerWindow
import com.kodeelite.nooreislam.feature.miqat.store.MiqatTimesStore
import com.kodeelite.nooreislam.feature.tracker.data.TrackerStore
import com.kodeelite.nooreislam.feature.tracker.domain.dayProgress
import com.kodeelite.nooreislam.feature.tracker.presentation.components.TrackControl
import com.kodeelite.nooreislam.feature.tracker.presentation.components.TrackingSheet
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.today
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Today's prayer list with per-prayer tracking. Reads the stores; owns its tracking sheet. */
@Composable
fun TodayPrayers() {
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val clock by Now.now.collectAsState()
    val now = clock.time
    val todayTimes by MiqatTimesStore.today.collectAsState()
    val tracker = koinInject<TrackerStore>()
    val tracked by tracker.tracked.collectAsState()
    val exemptionPeriods by tracker.exempt.collectAsState()
    val todayExempt = dayProgress(tracked, clock.date, exemptionPeriods, clock.date) == DayProgress.Exempt
    val streakEnabled by SettingsStore.streakEnabled.collectAsState()
    val markable: (MiqatTime) -> Boolean = { streakEnabled && !todayExempt && it.at.time <= now }

    val dailyTimes = remember(todayTimes) { todayTimes.filter { it.miqat in Miqat.DAILY && it.miqat != Miqat.Sunrise } }
    val prayerTimes = dailyTimes.filter { it.miqat.isPrayer }
    val currentPrayer = todayTimes.currentPrayer(now)
    val nextMt = prayerTimes.firstOrNull { it.at.time > now } ?: prayerTimes.firstOrNull()

    var sheetPrayer by remember { mutableStateOf<Miqat?>(null) }

    val items = mutableListOf<AppTileItem>()
    for (mt in dailyTimes) {
        val status = when (mt.miqat) {
            currentPrayer -> MiqatTimeStatus.Current
            nextMt?.miqat -> MiqatTimeStatus.Soon
            else -> null
        }
        val localizedTitle = mt.miqat.label(clock.date)
        items.add(
            AppTileItem(
                title = localizedTitle,
                subtitle = prayerWindow(todayTimes, mt.miqat, timeFormat.pattern) ?: mt.at.time.format(timeFormat.pattern),
                selected = status == MiqatTimeStatus.Current,
                leadingIcon = mt.miqat.icon,
                leadingColor = AppTheme.colors.primary,
                badge = if (status == MiqatTimeStatus.Current) {
                    { PulseDot(color = AppTheme.colors.primary) }
                } else null,
                // a prayer can only be logged once its time has come
                trailing = if (mt.miqat.isPrayer) {
                    {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (status == MiqatTimeStatus.Soon) {
                                Text(MiqatTimeStatus.Soon.label, color = MiqatTimeStatus.Soon.color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            // hidden while exempt (the card says so) and while the streak is off
                            if (markable(mt)) TrackControl(tracked[mt.miqat])
                        }
                    }
                } else null,
                onClick = if (mt.miqat.isPrayer && markable(mt)) {
                    { sheetPrayer = mt.miqat }
                } else null,
            )
        )
    }

    AppTileGroup(
        title = stringResource(Res.string.today),
        items = items
    )

    sheetPrayer?.let { p ->
        TrackingSheet(
            prayer = p,
            date = Now.date(),
            current = tracked[p],
            onSelect = { tracker.setStatus(p, it); sheetPrayer = null },
            onDismiss = { sheetPrayer = null },
        )
    }
}


