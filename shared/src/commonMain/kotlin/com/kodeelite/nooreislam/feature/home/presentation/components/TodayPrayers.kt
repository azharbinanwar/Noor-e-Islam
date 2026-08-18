package com.kodeelite.nooreislam.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.X
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppTile
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.PulseDot
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.datetime.format
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.MiqatTimeStatus
import com.kodeelite.nooreislam.core.enums.PrayerTrackerStatus
import com.kodeelite.nooreislam.core.enums.color
import com.kodeelite.nooreislam.core.enums.label
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.miqat.store.MiqatTimesStore
import com.kodeelite.nooreislam.feature.tracker.data.TrackerStore
import com.kodeelite.nooreislam.feature.tracker.presentation.components.TrackControl
import com.kodeelite.nooreislam.feature.tracker.presentation.components.TrackingSheet
import org.koin.compose.koinInject
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.clear
import com.kodeelite.nooreislam.resources.mark_prayer
import com.kodeelite.nooreislam.resources.menu
import com.kodeelite.nooreislam.resources.today
import org.jetbrains.compose.resources.stringResource

/** Today's prayer list with per-prayer tracking. Reads the stores; owns its tracking sheet. */
@Composable
fun TodayPrayers() {
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val clock by Now.now.collectAsState()
    val now = clock.time
    val todayTimes by MiqatTimesStore.today.collectAsState()
    val tracker = koinInject<TrackerStore>()
    val tracked by tracker.tracked.collectAsState()

    val dailyTimes = remember(todayTimes) { todayTimes.filter { it.miqat in Miqat.DAILY && it.miqat != Miqat.Sunrise } }
    val prayerTimes = dailyTimes.filter { it.miqat.isPrayer }
    // current fard: every prayer runs to the next except Fajr, which ends at sunrise (sunrise→Dhuhr is a gap).
    val sunriseTime = todayTimes.firstOrNull { it.miqat == Miqat.Sunrise }?.at?.time
    val startedPrayer = prayerTimes.lastOrNull { it.at.time <= now } ?: prayerTimes.lastOrNull()
    val currentPrayer = when {
        startedPrayer == null -> null
        startedPrayer.miqat == Miqat.Fajr && sunriseTime != null && now >= sunriseTime -> null
        else -> startedPrayer.miqat
    }
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
                subtitle = mt.at.time.format(timeFormat.pattern),
                selected = status == MiqatTimeStatus.Current,
                leadingIcon = mt.miqat.icon,
                leadingColor = AppTheme.colors.primary,
                badge = if (status == MiqatTimeStatus.Current) {
                    { PulseDot(color = AppTheme.colors.primary) }
                } else null,
                trailing = if (mt.miqat.isPrayer) {
                    {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (status == MiqatTimeStatus.Soon) {
                                Text(MiqatTimeStatus.Soon.label, color = MiqatTimeStatus.Soon.color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            TrackControl(tracked[mt.miqat])
                        }
                    }
                } else null,
                onClick = if (mt.miqat.isPrayer) {
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


