package com.kodeelite.nooreislam.feature.backup.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.datetime.labelRes
import com.kodeelite.nooreislam.core.enums.BackupFrequency
import com.kodeelite.nooreislam.core.enums.TimeFormat
import com.kodeelite.nooreislam.core.store.BackupStore
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.auto_backup
import com.kodeelite.nooreislam.resources.backup_day
import com.kodeelite.nooreislam.resources.backup_schedule
import com.kodeelite.nooreislam.resources.backup_time
import com.kodeelite.nooreislam.resources.save
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.stringResource

/** Off / Daily / Weekly, and under it the time (and day, for weekly) the backup runs. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupFrequencySheet(onDismiss: () -> Unit) {
    val primary = AppTheme.colors.primary
    val frequency by BackupStore.frequency.collectAsState()
    val time by BackupStore.time.collectAsState()
    val weekday by BackupStore.weekday.collectAsState()
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    var pickingTime by remember { mutableStateOf(false) }
    var pickingDay by remember { mutableStateOf(false) }

    AppBottomSheet(onDismiss = onDismiss, title = stringResource(Res.string.auto_backup)) {
        AppTileGroup(
            modifier = Modifier.padding(top = 4.dp),
            items = BackupFrequency.entries.map { f ->
                val selected = f == frequency
                AppTileItem(
                    title = f.label(),
                    selected = selected,
                    trailing = if (selected) {
                        { Icon(Lucide.Check, null, tint = primary) }
                    } else null,
                    onClick = { BackupStore.setFrequency(f) },
                )
            },
        )
        // stays mounted: the group collapses and expands its rows itself, so Off never snaps the sheet
        AppTileGroup(
            title = stringResource(Res.string.backup_schedule),
            items = listOf(
                if (frequency == BackupFrequency.Weekly) AppTileItem(
                    leadingIcon = Lucide.CalendarDays,
                    title = stringResource(Res.string.backup_day),
                    subtitle = stringResource(weekday.labelRes),
                    onClick = { pickingDay = true },
                ) else null,
                if (frequency != BackupFrequency.Off) AppTileItem(
                    leadingIcon = Lucide.Clock,
                    title = stringResource(Res.string.backup_time),
                    subtitle = Now.formattedTime(time),
                    onClick = { pickingTime = true },
                ) else null,
            ),
        )
    }

    if (pickingDay) AppBottomSheet(onDismiss = { pickingDay = false }, title = stringResource(Res.string.backup_day)) {
        AppTileGroup(
            modifier = Modifier.padding(top = 4.dp),
            items = DayOfWeek.entries.map { d ->
                val selected = d == weekday
                AppTileItem(
                    title = stringResource(d.labelRes),
                    selected = selected,
                    trailing = if (selected) {
                        { Icon(Lucide.Check, null, tint = primary) }
                    } else null,
                    onClick = { BackupStore.setWeekday(d); pickingDay = false },
                )
            },
        )
    }

    if (pickingTime) {
        val state = rememberTimePickerState(initialHour = time.hour, initialMinute = time.minute, is24Hour = timeFormat == TimeFormat.TwentyFour)
        AppBottomSheet(onDismiss = { pickingTime = false }, title = stringResource(Res.string.backup_time)) {
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TimePicker(state = state)
                AppButton(
                    text = stringResource(Res.string.save),
                    onClick = { BackupStore.setTime(state.hour, state.minute); pickingTime = false },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
