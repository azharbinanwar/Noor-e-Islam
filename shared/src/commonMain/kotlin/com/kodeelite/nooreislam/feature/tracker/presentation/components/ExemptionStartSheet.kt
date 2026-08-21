package com.kodeelite.nooreislam.feature.tracker.presentation.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.BellOff
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.Infinity
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppButtonVariant
import com.kodeelite.nooreislam.core.components.AppSwitch
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.MiniStepper
import com.kodeelite.nooreislam.core.constants.defaults.ExemptionDefaults
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.platform.Platform
import com.kodeelite.nooreislam.core.store.PrayerFocusStore
import com.kodeelite.nooreislam.feature.notifications.store.NotificationStore
import com.kodeelite.nooreislam.feature.tracker.data.ExemptionStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.azkar_duas_and_tasbih_keep_running
import com.kodeelite.nooreislam.resources.cancel
import com.kodeelite.nooreislam.resources.days
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
import com.kodeelite.nooreislam.resources.until_i_turn_it_off
import com.kodeelite.nooreislam.resources.what_pauses
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * Shown when the exemption is switched on: what it will pause, and for how long.
 * [onStart] gives null days for "until I turn it off".
 */
@Composable
fun ExemptionStartSheet(
    onStart: (days: Int?, pauseAlerts: Boolean, pauseFocus: Boolean) -> Unit,
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

    AppBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(Res.string.prayer_exemption),
        footer = {
            AppButton(
                stringResource(Res.string.start),
                // a row the sheet never showed was never running, so it was never paused —
                // otherwise ending the exemption would look like it switched something on
                onClick = {
                    onStart(if (openEnded) null else days, showAlerts && pauseAlerts, showFocus && pauseFocus)
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
                    subtitle = Now.formattedDate(days),
                    trailing = {
                        MiniStepper(
                            days,
                            stringResource(Res.string.days),
                            { days = it },
                            min = 1,
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
}

