package com.kodeelite.nooreislam.feature.focus.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.kodeelite.nooreislam.resources.silence_for
import com.kodeelite.nooreislam.resources.silence_mode
import com.kodeelite.nooreislam.resources.start_after
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource

// One tile group per prayer: enable + (when on) start-after / silence-for / mode. Reads the stores directly.
@Composable
fun FocusPrayerList() {
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val today by MiqatTimesStore.today.collectAsState()
    val configs by PrayerFocusStore.configs.collectAsState()
    val setup = rememberFocusSetup()
    val min = stringResource(Res.string.minutes_short)
    FocusDefaults.rows.forEach { row ->
        val cfg = configs.getValue(row.key)
        val label = if (row.friday) row.miqat.jumuahLabel else row.miqat.label()
        val prayerTime = today.firstOrNull { it.miqat == row.miqat }?.at?.time
        val title = if (prayerTime != null) "$label · ${prayerTime.format(timeFormat.pattern)}" else label
        AppTileGroup(
            modifier = Modifier.fillMaxWidth().animateContentSize(),
            items = buildList {
                add(
                    AppTileItem(
                        title = title,
                        subtitle = if (cfg.enabled && prayerTime != null) "${cfg.mode.label()} · ${
                            windowRange(
                                prayerTime,
                                cfg.startAfter,
                                cfg.duration,
                                timeFormat.pattern
                            )
                        }" else null,
                        leadingIcon = row.miqat.icon,
                        trailing = { Switch(checked = cfg.enabled, onCheckedChange = { PrayerFocusStore.setEnabled(row.key, it) }) },
                    )
                )
                if (cfg.enabled) {
                    add(
                        AppTileItem(
                            title = stringResource(Res.string.start_after),
                            trailing = {
                                MiniStepper(
                                    cfg.startAfter,
                                    min,
                                    { PrayerFocusStore.setStartAfter(row.key, it) },
                                    min = 0,
                                    max = row.default.max
                                )
                            })
                    )
                    add(
                        AppTileItem(
                            title = stringResource(Res.string.silence_for),
                            trailing = {
                                MiniStepper(
                                    cfg.duration,
                                    min,
                                    { PrayerFocusStore.setDuration(row.key, it) },
                                    min = 5,
                                    max = row.default.max
                                )
                            })
                    )
                    add(AppTileItem(title = stringResource(Res.string.silence_mode), trailing = {
                        ModeToggle(cfg.mode) { m ->
                            if (m == SilenceMode.Silent && !setup.hasSilenceAccess()) setup.requestSilenceAccess()
                            PrayerFocusStore.setMode(row.key, m)
                        }
                    }))
                }
            },
        )
    }
}

// "4:40–5:10" window from a prayer time + the offsets; the mode label is prepended at the call site.
private fun windowRange(prayer: LocalTime, after: Int, duration: Int, pattern: String): String {
    val start = LocalTime.fromSecondOfDay((prayer.toSecondOfDay() + after * 60) % 86400)
    val end = LocalTime.fromSecondOfDay((prayer.toSecondOfDay() + (after + duration) * 60) % 86400)
    return "${start.format(pattern)}–${end.format(pattern)}"
}
