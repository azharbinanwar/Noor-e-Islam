package com.kodeelite.nooreislam.feature.focus.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.MiniStepper
import com.kodeelite.nooreislam.core.datetime.currentDate
import com.kodeelite.nooreislam.core.datetime.currentTime
import com.kodeelite.nooreislam.core.datetime.format
import com.kodeelite.nooreislam.core.focus.SilenceMode
import com.kodeelite.nooreislam.core.focus.rememberFocusSetup
import com.kodeelite.nooreislam.core.store.FocusTestStore
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.core.store.TestSlot
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.add_test_focus_slot
import com.kodeelite.nooreislam.resources.choose_silence_mode
import com.kodeelite.nooreislam.resources.focus_test_instructions_survive_reboot
import com.kodeelite.nooreislam.resources.minutes_short
import com.kodeelite.nooreislam.resources.remove_scheduled_slot
import com.kodeelite.nooreislam.resources.scheduled_test_slots
import com.kodeelite.nooreislam.resources.silence_duration_summary
import com.kodeelite.nooreislam.resources.silence_for_period
import com.kodeelite.nooreislam.resources.start_silence_after
import com.kodeelite.nooreislam.resources.test_background_focus_logic
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.jetbrains.compose.resources.stringResource

// Test tool: add one-shot mute slots that are saved (so they survive a reboot) and fire once.
@Composable
fun FocusTestSheet(onDismiss: () -> Unit) {
    val c = AppTheme.colors
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val saved by FocusTestStore.slots.collectAsState()

    var startIn by remember { mutableStateOf(2) } // minutes from now until the slot starts
    var duration by remember { mutableStateOf(2) }
    var mode by remember { mutableStateOf(SilenceMode.Vibrate) }
    val setup = rememberFocusSetup()
    val labelFormat = stringResource(Res.string.silence_duration_summary)

    AppBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(Res.string.test_background_focus_logic),
        subtitle = stringResource(Res.string.focus_test_instructions_survive_reboot),
        footer = {
            AppButton(
                text = stringResource(Res.string.add_test_focus_slot),
                onClick = { FocusTestStore.add(slotFor(startIn, duration, mode, timeFormat.pattern, labelFormat)) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(Res.string.start_silence_after), color = c.onSurface)
            MiniStepper(startIn, stringResource(Res.string.minutes_short), { startIn = it }, min = 1, max = 120)
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(Res.string.silence_for_period), color = c.onSurface)
            MiniStepper(duration, stringResource(Res.string.minutes_short), { duration = it }, min = 1, max = 60)
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(Res.string.choose_silence_mode), color = c.onSurface)
            ModeToggle(mode) { m ->
                if (m == SilenceMode.Silent && !setup.hasSilenceAccess()) setup.requestSilenceAccess()
                mode = m
            }
        }
        if (saved.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(stringResource(Res.string.scheduled_test_slots), fontSize = 12.sp, color = c.onSurfaceVariant)
        }
        saved.forEach { slot ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("${slot.label} · ${slot.mode}", color = c.onSurface)
                Text(
                    stringResource(Res.string.remove_scheduled_slot),
                    fontSize = 13.sp,
                    color = c.primary,
                    modifier = Modifier.clickable { FocusTestStore.remove(slot) })
            }
        }
    }
}

// "5:04 for 2 min" + absolute epoch millis, from "start in [startIn] min" and a duration.
private fun slotFor(startIn: Int, duration: Int, mode: SilenceMode, pattern: String, labelFormat: String): TestSlot {
    val tz = TimeZone.currentSystemDefault()
    val nowMillis = LocalDateTime(currentDate(), currentTime()).toInstant(tz).toEpochMilliseconds()
    val start = nowMillis + startIn * 60_000L
    val end = start + duration * 60_000L
    val base = currentTime()
    val total = (base.hour * 60 + base.minute + startIn) % 1440
    val timeStr = LocalTime(total / 60, total % 60).format(pattern)
    val label = labelFormat.replace("%1\$s", timeStr).replace("%2\$d", duration.toString())
    return TestSlot(start, end, mode.name, label)
}
