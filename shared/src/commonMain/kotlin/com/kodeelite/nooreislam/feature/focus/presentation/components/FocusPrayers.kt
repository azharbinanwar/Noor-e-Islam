package com.kodeelite.nooreislam.feature.focus.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.SlidersHorizontal
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppIconOption
import com.kodeelite.nooreislam.core.components.AppIconToggle
import com.kodeelite.nooreislam.core.components.AppSwitch
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.MiniStepper
import com.kodeelite.nooreislam.core.constants.defaults.FocusDefaults
import com.kodeelite.nooreislam.core.datetime.format
import com.kodeelite.nooreislam.core.focus.SilenceMode
import com.kodeelite.nooreislam.core.focus.rememberFocusSetup
import com.kodeelite.nooreislam.core.store.PrayerFocusStore
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.miqat.store.MiqatTimesStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.minutes_short
import com.kodeelite.nooreislam.resources.notifications_prayers
import com.kodeelite.nooreislam.resources.silence_for
import com.kodeelite.nooreislam.resources.silence_mode
import com.kodeelite.nooreislam.resources.start_after
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource

// the options button grows in from the switch rather than popping the row wider
private val optionsEnter = fadeIn() + expandHorizontally()
private val optionsExit = fadeOut() + shrinkHorizontally()

// One tile per prayer: enable it here, tune its window in the sheet — same shape as the notification list.
@Composable
fun FocusPrayers() {
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val today by MiqatTimesStore.today.collectAsState()
    val configs by PrayerFocusStore.configs.collectAsState()
    val setup = rememberFocusSetup()
    val c = AppTheme.colors
    val min = stringResource(Res.string.minutes_short)
    var sheetKey by remember { mutableStateOf<String?>(null) }
    // the prayer waiting on Do Not Disturb access; Silent is only committed once it is granted
    var dndKey by remember { mutableStateOf<String?>(null) }

    fun timeOf(key: String) = FocusDefaults.rows.first { it.key == key }
        .let { row -> today.firstOrNull { it.miqat == row.miqat }?.at?.time }

    AppTileGroup(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        title = stringResource(Res.string.notifications_prayers),
        items = FocusDefaults.rows.map { row ->
            val cfg = configs.getValue(row.key)
            val label = if (row.friday) row.miqat.jumuahLabel else row.miqat.label()
            val prayerTime = timeOf(row.key)
            AppTileItem(
                title = if (prayerTime != null) "$label · ${prayerTime.format(timeFormat.pattern)}" else label,
                subtitle = if (cfg.enabled && prayerTime != null) {
                    "${cfg.mode.label()} · ${windowRange(prayerTime, cfg.startAfter, cfg.duration, timeFormat.pattern)}"
                } else null,
                leadingIcon = row.miqat.icon,
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AnimatedVisibility(cfg.enabled, enter = optionsEnter, exit = optionsExit) {
                            IconButton(onClick = { sheetKey = row.key }) {
                                Icon(Lucide.SlidersHorizontal, contentDescription = null, tint = c.primary)
                            }
                        }
                        AppSwitch(checked = cfg.enabled, onCheckedChange = { PrayerFocusStore.setEnabled(row.key, it) })
                    }
                },
                onClick = if (cfg.enabled) ({ sheetKey = row.key }) else null,
            )
        },
    )

    sheetKey?.let { key ->
        val row = FocusDefaults.rows.first { it.key == key }
        val cfg = configs.getValue(key)
        val label = if (row.friday) row.miqat.jumuahLabel else row.miqat.label()
        val prayerTime = timeOf(key)
        AppBottomSheet(
            onDismiss = { sheetKey = null },
            title = if (prayerTime != null) "$label · ${prayerTime.format(timeFormat.pattern)}" else label,
            subtitle = prayerTime?.let { windowRange(it, cfg.startAfter, cfg.duration, timeFormat.pattern) },
        ) {
            AppTileGroup(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                items = listOf(
                    AppTileItem(
                        title = stringResource(Res.string.start_after),
                        trailing = {
                            MiniStepper(cfg.startAfter, min, { PrayerFocusStore.setStartAfter(key, it) }, min = 0, max = row.default.max)
                        },
                    ),
                    AppTileItem(
                        title = stringResource(Res.string.silence_for),
                        trailing = {
                            MiniStepper(cfg.duration, min, { PrayerFocusStore.setDuration(key, it) }, min = 5, max = row.default.max)
                        },
                    ),
                    AppTileItem(
                        title = stringResource(Res.string.silence_mode),
                        trailing = {
                            AppIconToggle(
                                options = SilenceMode.entries.map { AppIconOption(it.icon, it, it.label()) },
                                selected = cfg.mode,
                                onPick = { m ->
                                    if (m == SilenceMode.Silent && !setup.hasSilenceAccess()) dndKey = key
                                    else PrayerFocusStore.setMode(key, m)
                                },
                            )
                        },
                    ),
                ),
            )
        }
    }

    dndKey?.let { key ->
        // granted while we were away — commit the Silent that was never applied, and close
        LifecycleResumeEffect(key) {
            if (setup.hasSilenceAccess()) {
                PrayerFocusStore.setMode(key, SilenceMode.Silent)
                dndKey = null
            }
            onPauseOrDispose { }
        }
        DndAccessSheet(
            onOpenSettings = { setup.requestSilenceAccess() },
            onDismiss = { PrayerFocusStore.setMode(key, SilenceMode.Vibrate); dndKey = null },
        )
    }
}

// "4:40–5:10" window from a prayer time + the offsets; the mode label is prepended at the call site.
private fun windowRange(prayer: LocalTime, after: Int, duration: Int, pattern: String): String {
    val start = LocalTime.fromSecondOfDay((prayer.toSecondOfDay() + after * 60) % 86400)
    val end = LocalTime.fromSecondOfDay((prayer.toSecondOfDay() + (after + duration) * 60) % 86400)
    return "${start.format(pattern)}–${end.format(pattern)}"
}
