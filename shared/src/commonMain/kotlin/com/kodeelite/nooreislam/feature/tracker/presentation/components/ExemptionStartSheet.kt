package com.kodeelite.nooreislam.feature.tracker.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.BellOff
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Infinity
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppButtonVariant
import com.kodeelite.nooreislam.core.components.AppCalendar
import com.kodeelite.nooreislam.core.components.AppChip
import com.kodeelite.nooreislam.core.components.AppSwitch
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.MiniStepper
import com.kodeelite.nooreislam.core.constants.defaults.ExemptionDefaults
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.datetime.formatted
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.colorOf
import com.kodeelite.nooreislam.core.platform.Platform
import com.kodeelite.nooreislam.core.store.PrayerFocusStore
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.miqat.domain.currentPrayer
import com.kodeelite.nooreislam.feature.miqat.store.MiqatTimesStore
import com.kodeelite.nooreislam.feature.notifications.store.NotificationStore
import com.kodeelite.nooreislam.feature.tracker.data.ExemptionStore
import com.kodeelite.nooreislam.feature.tracker.data.TrackerStore
import com.kodeelite.nooreislam.feature.tracker.domain.resumePoint
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.all_day
import com.kodeelite.nooreislam.resources.azkar_duas_and_tasbih_keep_running
import com.kodeelite.nooreislam.resources.cancel
import com.kodeelite.nooreislam.resources.days
import com.kodeelite.nooreislam.resources.done
import com.kodeelite.nooreislam.resources.from_prayer
import com.kodeelite.nooreislam.resources.from_which_prayer
import com.kodeelite.nooreislam.resources.how_long
import com.kodeelite.nooreislam.resources.no_end_date_nothing_comes_back_on_its_own
import com.kodeelite.nooreislam.resources.no_prayer_alerts_until_it_ends
import com.kodeelite.nooreislam.resources.no_prayer_alerts_until_you_turn_this_off
import com.kodeelite.nooreislam.resources.pause_notifications
import com.kodeelite.nooreislam.resources.pause_prayer_focus
import com.kodeelite.nooreislam.resources.phone_wont_be_silenced_for_prayers
import com.kodeelite.nooreislam.resources.prayer_exemption
import com.kodeelite.nooreislam.resources.resume_after
import com.kodeelite.nooreislam.resources.start
import com.kodeelite.nooreislam.resources.starts_from
import com.kodeelite.nooreislam.resources.streak_off_but_log_kept
import com.kodeelite.nooreislam.resources.today
import com.kodeelite.nooreislam.resources.until_i_turn_it_off
import com.kodeelite.nooreislam.resources.what_pauses
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * Shown when the exemption is switched on: what it will pause, and for how long.
 * [onStart] gives null days for "until I turn it off".
 */
@Composable
fun ExemptionStartSheet(
    onStart: (days: Int?, pauseAlerts: Boolean, pauseFocus: Boolean, from: LocalDate, fromPrayer: Miqat?) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = AppTheme.colors
    val exemption = koinInject<ExemptionStore>()
    var days by remember { mutableStateOf(exemption.lastDays.value) }
    var openEnded by remember { mutableStateOf(false) }
    // nothing to offer to pause if it isn't running — and Focus doesn't exist on iOS at all
    val alertsOn by NotificationStore.settings.collectAsState()
    val focusOn by PrayerFocusStore.allFocus.collectAsState()
    val showAlerts = alertsOn.allAlerts
    val showFocus = Platform.canControlDnd && focusOn
    var pauseAlerts by remember { mutableStateOf(ExemptionDefaults.PAUSE_ALERTS) }
    var pauseFocus by remember { mutableStateOf(ExemptionDefaults.PAUSE_FOCUS) }
    val streakOn by SettingsStore.streakEnabled.collectAsState()
    val todayTimes by MiqatTimesStore.today.collectAsState()
    val history by koinInject<TrackerStore>().history.collectAsState()
    // she cannot have been exempt before a prayer she prayed, so the log sets the earliest point
    val resume = remember(history) { resumePoint(history) }
    val emptyDot = c.onSurfaceVariant.copy(alpha = 0.22f)
    // remembering late is the common case, so it can be backdated. Null prayer = the whole day.
    var startDate by remember { mutableStateOf(resume?.first ?: Now.date()) }
    // it began now, so this morning's prayers were owed — "all day" would quietly wipe them.
    // Before Fajr there is nothing owed yet, and a backdated day has no "current" prayer at all.
    var fromPrayer by remember {
        mutableStateOf(resume?.takeIf { it.first == startDate }?.second ?: todayTimes.currentPrayer(Now.time()))
    }
    var picking by remember { mutableStateOf(false) }
    val dateFormat by SettingsStore.gregorianDateFormat.collectAsState()
    // backdating shortens what is left rather than extending the exemption, and it can never
    // be set to end before today — an exemption that is already over would look broken
    val minDays = startDate.daysUntil(Now.date()) + 1
    val lastDay = startDate.plus(days.coerceAtLeast(minDays) - 1, DateTimeUnit.DAY)

    AppBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(Res.string.prayer_exemption),
        footer = {
            AppButton(
                stringResource(Res.string.start),
                // a row the sheet never showed was never running, so it was never paused —
                // otherwise ending the exemption would look like it switched something on
                onClick = {
                    onStart(
                        if (openEnded) null else days.coerceAtLeast(minDays),
                        showAlerts && pauseAlerts,
                        showFocus && pauseFocus,
                        startDate,
                        if (streakOn || resume != null) fromPrayer else null,
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            AppButton(
                stringResource(Res.string.cancel),
                onClick = onDismiss,
                variant = AppButtonVariant.Text,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        AppTileGroup(
            title = stringResource(Res.string.starts_from),
            items = listOf(
                AppTileItem(
                    leadingIcon = Lucide.CalendarDays,
                    leadingColor = c.primary,
                    title = startDate.formatted(dateFormat),
                    subtitle = listOfNotNull(
                        if (startDate == Now.date()) stringResource(Res.string.today) else null,
                        if (!streakOn && resume == null) null
                        else fromPrayer?.let { stringResource(Res.string.from_prayer, it.label()) }
                            ?: stringResource(Res.string.all_day),
                    ).joinToString(", ").ifEmpty { startDate.formatted(dateFormat) },
                    onClick = { picking = true },
                ),
            ),
        )

        AppTileGroup(
            title = stringResource(Res.string.how_long),
            items = listOf(
                AppTileItem(
                    leadingIcon = Lucide.Infinity,
                    leadingColor = c.primary,
                    title = stringResource(Res.string.until_i_turn_it_off),
                    subtitle = stringResource(Res.string.no_end_date_nothing_comes_back_on_its_own),
                    trailing = { AppSwitch(openEnded, { openEnded = it }) },
                    onClick = { openEnded = !openEnded },
                ),
                if (openEnded) null else AppTileItem(
                    leadingIcon = Lucide.CalendarDays,
                    leadingColor = c.primary,
                    title = stringResource(Res.string.resume_after),
                    // counted from the day it began, so backdating eats into what is left
                    subtitle = lastDay.plus(1, DateTimeUnit.DAY).formatted(dateFormat),
                    trailing = {
                        MiniStepper(
                            days.coerceAtLeast(minDays),
                            stringResource(Res.string.days),
                            { days = it },
                            min = minDays,
                            max = ExemptionDefaults.MAX_DAYS,
                        )
                    },
                ),
            ),
        )

        AppTileGroup(
            title = stringResource(Res.string.what_pauses),
            items = listOf(
                if (!showAlerts) null else AppTileItem(
                    leadingIcon = Lucide.Bell,
                    leadingColor = c.primary,
                    title = stringResource(Res.string.pause_notifications),
                    subtitle = stringResource(
                        if (openEnded) Res.string.no_prayer_alerts_until_you_turn_this_off
                        else Res.string.no_prayer_alerts_until_it_ends
                    ),
                    trailing = { AppSwitch(pauseAlerts, { pauseAlerts = it }) },
                    onClick = { pauseAlerts = !pauseAlerts },
                ),
                if (!showFocus) null else AppTileItem(
                    leadingIcon = Lucide.BellOff,
                    leadingColor = c.primary,
                    title = stringResource(Res.string.pause_prayer_focus),
                    subtitle = stringResource(Res.string.phone_wont_be_silenced_for_prayers),
                    trailing = { AppSwitch(pauseFocus, { pauseFocus = it }) },
                    onClick = { pauseFocus = !pauseFocus },
                ),
            ),
        )
        Text(
            stringResource(Res.string.azkar_duas_and_tasbih_keep_running),
            fontSize = 12.sp,
            color = c.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp),
        )
    }

    if (picking) StartedPickerSheet(
        date = startDate,
        prayer = fromPrayer,
        // asked whenever there is anything logged, switch or no switch: turning the streak off is
        // itself a likely mistake, and hiding the record is what would lose her prayers
        askPrayer = streakOn || resume != null,
        streakOn = streakOn,
        resume = resume,
        dayDots = { d ->
            history[d].let { st -> Miqat.PRAYERS.map { p -> st?.get(p)?.colorOf(c) ?: emptyDot } }
        },
        onPick = { d, p -> startDate = d; fromPrayer = p },
        onDismiss = { picking = false },
    )
}

/** The day it began, and the prayer on that day. Future days are unselectable — this only looks back. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StartedPickerSheet(
    date: LocalDate,
    prayer: Miqat?,
    askPrayer: Boolean,
    streakOn: Boolean,
    resume: Pair<LocalDate, Miqat>?,
    dayDots: (LocalDate) -> List<Color>,
    onPick: (LocalDate, Miqat?) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = Now.date()
    val cap = today.minus(ExemptionDefaults.MAX_DAYS - 1, DateTimeUnit.DAY)
    var picked by remember { mutableStateOf(date) }
    var from by remember { mutableStateOf(prayer) }

    AppBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(Res.string.starts_from),
        footer = {
            AppButton(
                stringResource(Res.string.done),
                onClick = { onPick(picked, from); onDismiss() },
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        AppCalendar(
            selected = picked,
            today = today,
            // "current prayer" means nothing on a day already past, so an earlier day is a whole day
            onSelect = { picked = it; if (it != today) from = null },
            lastSelectable = today,
            // no further back than the longest exemption allows, so the length she picked is the
            // length she gets — a start beyond that would have to be stretched to reach today
            // the later of the two: never past the cap, and never before a prayer she prayed
            firstSelectable = maxOf(cap, resume?.first ?: cap),
        )
        if (!askPrayer) return@AppBottomSheet
        Spacer(Modifier.height(16.dp))
        // hayd rarely begins at Fajr, so the day alone is not enough
        Text(
            stringResource(Res.string.from_which_prayer),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        // wrapping, not scrolling: every option stays visible, and it reflows on its own once
        // the labels are Arabic or Urdu
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // on the day she stopped, the prayers before that point were prayed, not exempt.
            // They stay on screen, dimmed and checked, so the gap explains itself.
            val floor = resume?.takeIf { it.first == picked }?.second
            AppChip(
                stringResource(Res.string.all_day),
                selected = from == null,
                onClick = { from = null },
                enabled = floor == null,
            )
            Miqat.PRAYERS.forEach { p ->
                val prayed = floor != null && p.ordinal < floor.ordinal
                AppChip(
                    p.label(),
                    selected = from == p,
                    onClick = { from = p },
                    icon = if (prayed) Lucide.Check else p.icon,
                    enabled = !prayed,
                )
            }
        }

        // she reached this sheet with the streak switched off, which is often how someone tries to
        // pause tracking. Say the log survived before explaining why these are greyed out.
        if (!streakOn) {
            Text(
                stringResource(Res.string.streak_off_but_log_kept),
                fontSize = 12.sp,
                color = AppTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp, start = 4.dp, end = 4.dp),
            )
        }
    }
}

