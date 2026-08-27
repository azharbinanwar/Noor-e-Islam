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
import com.kodeelite.nooreislam.core.datetime.formatted
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
import com.kodeelite.nooreislam.feature.tracker.domain.owedPrayers
import com.kodeelite.nooreislam.feature.tracker.presentation.components.TrackControl
import com.kodeelite.nooreislam.feature.tracker.presentation.components.TrackingSheet
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.today
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Today's prayer list with per-prayer tracking. Reads the stores; owns its tracking sheet. */
@Composable
fun TodayPrayers() {
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val clock by Now.now.collectAsState()
    val now = clock.time
    // the day being worshipped, which until Fajr is still the calendar day before: ask for its date
    // and its times, and everything below scores against the right day on its own
    val date by MiqatTimesStore.activeDate.collectAsState()
    val dayTimes by MiqatTimesStore.activeTimes.collectAsState()
    val tracker = koinInject<TrackerStore>()
    val history by tracker.history.collectAsState()
    val exemptionPeriods by tracker.exempt.collectAsState()
    val owed = owedPrayers(date, exemptionPeriods, date)
    val streakEnabled by SettingsStore.streakEnabled.collectAsState()
    // paused shows from the moment it starts, not once the prayer's time comes: the row has to say
    // why it cannot be logged, and an empty row says nothing
    val paused: (MiqatTime) -> Boolean = { streakEnabled && it.miqat.isPrayer && it.miqat !in owed }
    // the whole timestamp, not the time of day: after midnight these are yesterday's prayers, all past
    val markable: (MiqatTime) -> Boolean = { streakEnabled && it.miqat in owed && it.at <= clock }

    val dailyTimes = remember(dayTimes) { dayTimes.filter { it.miqat in Miqat.DAILY && it.miqat != Miqat.Sunrise } }
    val prayerTimes = dailyTimes.filter { it.miqat.isPrayer }
    val currentPrayer = dayTimes.currentPrayer(now)
    val nextMt = prayerTimes.firstOrNull { it.at.time > now } ?: prayerTimes.firstOrNull()

    var sheetPrayer by remember { mutableStateOf<MiqatTime?>(null) }

    val items = mutableListOf<AppTileItem>()
    for (mt in dailyTimes) {
        val status = when (mt.miqat) {
            currentPrayer -> MiqatTimeStatus.Current
            // "Soon" has to be earned by the clock: on the next prayer alone it was still saying
            // soon four hours out, which is only another way of saying next
            nextMt?.miqat -> MiqatTimeStatus.nextIn(mt.at.time.minutesFromNow(now))
            else -> null
        }
        val localizedTitle = mt.miqat.label(mt.at.date)
        items.add(
            AppTileItem(
                title = localizedTitle,
                subtitle = prayerWindow(dayTimes, mt.miqat, timeFormat.pattern) ?: mt.at.time.format(timeFormat.pattern),
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
                            // the row already carries the time and the running prayer already pulses,
                            // so a soon/upcoming word is a third way of saying the same thing
                            // no "soon" beside a paused pill — nothing is coming for that prayer
                            // if (!paused(mt) && (status == MiqatTimeStatus.Soon || status == MiqatTimeStatus.Upcoming)) {
                            //     Text(status.label, color = status.color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            // }
                            // hidden only while the streak is off; an exempt prayer says paused
                            if (paused(mt)) TrackControl(null, exempt = true)
                            else if (markable(mt)) TrackControl(history[mt.at.date]?.get(mt.miqat))
                        }
                    }
                } else null,
                onClick = if (mt.miqat.isPrayer && markable(mt)) {
                    { sheetPrayer = mt }
                } else null,
            )
        )
    }

    // "Today" only while it is one: after midnight this list is still yesterday's, and the header
    // is the only thing that can say which day is on screen
    val dateFormat by SettingsStore.gregorianDateFormat.collectAsState()
    AppTileGroup(
        title = if (date == clock.date) stringResource(Res.string.today) else date.formatted(dateFormat),
        items = items
    )

    // the tapped row carries its own day, so what gets written is the day that was on screen
    sheetPrayer?.let { mt ->
        TrackingSheet(
            prayer = mt.miqat,
            date = mt.at.date,
            current = history[mt.at.date]?.get(mt.miqat),
            onSelect = { tracker.setStatus(mt.at.date, mt.miqat, it); sheetPrayer = null },
            onDismiss = { sheetPrayer = null },
        )
    }
}

// minutes from [now] to this time today; a time already past reads as a whole day out, which
// only happens for the wrap-around to tomorrow's first prayer
private fun LocalTime.minutesFromNow(now: LocalTime): Int =
    ((toSecondOfDay() - now.toSecondOfDay() + 24 * 3600) % (24 * 3600)) / 60
