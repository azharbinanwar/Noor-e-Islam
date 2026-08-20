package com.kodeelite.nooreislam.feature.notifications.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Moon
import com.composables.icons.lucide.SlidersHorizontal
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.SunMedium
import com.composables.icons.lucide.Sunrise
import com.composables.icons.lucide.Sunset
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppSwitch
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.MiniStepper
import com.kodeelite.nooreislam.core.datetime.format
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.TimeFormat
import com.kodeelite.nooreislam.core.locale.tr
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.core.permissions.AppPermission
import com.kodeelite.nooreislam.core.permissions.PermissionStatus
import com.kodeelite.nooreislam.core.permissions.rememberPermissionService
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.miqat.store.MiqatTimesStore
import com.kodeelite.nooreislam.feature.notifications.presentation.components.ExactAlarmNeedsAttention
import com.kodeelite.nooreislam.feature.notifications.presentation.components.NotificationTestScreen
import com.kodeelite.nooreislam.feature.notifications.presentation.components.NotificationsNeedsAttention
import com.kodeelite.nooreislam.feature.notifications.store.NotificationStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.after_asr
import com.kodeelite.nooreislam.resources.after_fajr
import com.kodeelite.nooreislam.resources.after_isha
import com.kodeelite.nooreislam.resources.all_alerts
import com.kodeelite.nooreislam.resources.at_prayer_time
import com.kodeelite.nooreislam.resources.back
import com.kodeelite.nooreislam.resources.daily_quran_reminder
import com.kodeelite.nooreislam.resources.dhikr
import com.kodeelite.nooreislam.resources.evening_adhkar
import com.kodeelite.nooreislam.resources.every_day
import com.kodeelite.nooreislam.resources.friday
import com.kodeelite.nooreislam.resources.ishraq
import com.kodeelite.nooreislam.resources.jamaat_after_start
import com.kodeelite.nooreislam.resources.jamaat_reminder
import com.kodeelite.nooreislam.resources.last_third_of_the_night
import com.kodeelite.nooreislam.resources.master_switch_for_every_reminder
import com.kodeelite.nooreislam.resources.mid_morning
import com.kodeelite.nooreislam.resources.minutes_before
import com.kodeelite.nooreislam.resources.minutes_short
import com.kodeelite.nooreislam.resources.morning_adhkar
import com.kodeelite.nooreislam.resources.notif_at_before
import com.kodeelite.nooreislam.resources.notif_at_jamaah
import com.kodeelite.nooreislam.resources.notif_verse_sub
import com.kodeelite.nooreislam.resources.notifications
import com.kodeelite.nooreislam.resources.notifications_nafil
import com.kodeelite.nooreislam.resources.notifications_prayers
import com.kodeelite.nooreislam.resources.notifications_quran
import com.kodeelite.nooreislam.resources.prayer_jumuah
import com.kodeelite.nooreislam.resources.remind_before
import com.kodeelite.nooreislam.resources.reminder_time
import com.kodeelite.nooreislam.resources.save
import com.kodeelite.nooreislam.resources.surah_al_kahf
import com.kodeelite.nooreislam.resources.surah_al_mulk
import com.kodeelite.nooreislam.resources.tahajjud
import com.kodeelite.nooreislam.resources.verse_of_the_day
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds
import com.kodeelite.nooreislam.core.constants.defaults.NotificationDefaults as N

// the options button grows in from the switch rather than popping the row wider
private val optionsEnter = fadeIn() + expandHorizontally()
private val optionsExit = fadeOut() + shrinkHorizontally()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen() {
    val nav = LocalAppNavigator.current
    val edition = koinInject<AppEdition>()
    val c = AppTheme.colors
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val s by NotificationStore.settings.collectAsState()
    val today by MiqatTimesStore.today.collectAsState()

    // Master switch stays dimmed until both are granted — same check the banners above use.
    // Checked once on cold start, not polled: canScheduleExactAlarms() is only guaranteed accurate
    // for a fresh process — Android kills the app outright when the user revokes it in Settings.
    val perms = rememberPermissionService()
    var notifGranted by remember { mutableStateOf(true) }
    var exactAlarmGranted by remember { mutableStateOf(true) }
    // Notifications can genuinely change while the app is running, so poll it like the banner tile
    // does. Exact-alarm is checked once — Android only guarantees it accurate for a fresh process.
    LaunchedEffect(Unit) {
        while (true) {
            notifGranted = perms.status(AppPermission.Notifications) == PermissionStatus.Granted
            delay(1500.milliseconds)
        }
    }
    LaunchedEffect(Unit) {
        exactAlarmGranted = perms.status(AppPermission.ExactAlarm) == PermissionStatus.Granted
    }
    val remindersLocked = !notifGranted || !exactAlarmGranted

    var kahfPicker by remember { mutableStateOf(false) }
    var taps by remember { mutableStateOf(0) } // 7 taps opens the dev test screen
    var showTest by remember { mutableStateOf(false) }
    var verseOfDay by remember { mutableStateOf(false) } // ponytail: UI shell only, not wired yet
    var dailyReminderPicker by remember { mutableStateOf(false) }
    var sheetKey by remember { mutableStateOf<String?>(null) }
    if (showTest) {
        NotificationTestScreen(onBack = { showTest = false }); return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.notifications)) },
                navigationIcon = {
                    IconButton(onClick = { nav.back() }) {
                        Icon(
                            tr(Lucide.ChevronLeft, Lucide.ChevronRight),
                            stringResource(Res.string.back)
                        )
                    }
                },
            )
        },
    ) { pad ->
        val pat = timeFormat.pattern
        val lBefore = stringResource(Res.string.notif_at_before)
        val lJamaah = stringResource(Res.string.notif_at_jamaah)
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
        ) {
            NotificationsNeedsAttention()
            ExactAlarmNeedsAttention()

            Box(Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                taps++; if (taps >= 7) showTest = true
            }) {
                AppTileGroup(
                    items = listOf(
                        AppTileItem(
                            title = stringResource(Res.string.all_alerts),
                            subtitle = stringResource(Res.string.master_switch_for_every_reminder),
                            trailing = {
                                AppSwitch(
                                    checked = s.allAlerts && !remindersLocked,
                                    enabled = !remindersLocked,
                                    onCheckedChange = NotificationStore::setAllAlerts,
                                )
                            },
                        )
                    )
                )
            }

            // off collapses the list; states stay saved. Also collapsed while locked, even if the
            // saved preference is still "on" — the switch above only shows on once access is granted.
            if (s.allAlerts && !remindersLocked) {
                // Quran app has no prayer-time engine — only the Quran reminders group applies
                if (edition != AppEdition.QURAN) AppTileGroup(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    title = stringResource(Res.string.notifications_prayers),
                    items = buildList {
                        Miqat.PRAYERS.forEach { p ->
                            val cfg = s.prayers.getValue(p.key)
                            val base = timeOf(today, p)
                            val label = stringResource(p.labelRes)
                            add(
                                AppTileItem(
                                    title = if (base != null) "$label · ${base.format(pat)}" else label,
                                    subtitle = if (cfg.enabled) offsetSubtitle(
                                        base,
                                        if (cfg.remindBeforeOn) cfg.remindBefore else 0,
                                        cfg.jamaat,
                                        cfg.jamaatAfter,
                                        lBefore,
                                        lJamaah,
                                        pat
                                    ) else null,
                                    leadingIcon = p.icon,
                                    trailing = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            AnimatedVisibility(cfg.enabled, enter = optionsEnter, exit = optionsExit) {
                                                IconButton(onClick = { sheetKey = p.key }) {
                                                    Icon(
                                                        Lucide.SlidersHorizontal,
                                                        contentDescription = null,
                                                        tint = c.primary
                                                    )
                                                }
                                            }
                                            AppSwitch(checked = cfg.enabled, onCheckedChange = { NotificationStore.setPrayerEnabled(p.key, it) })
                                        }
                                    },
                                    onClick = if (cfg.enabled) ({ sheetKey = p.key }) else null,
                                )
                            )
                        }
                        val j = s.jumuah
                        val jBase = timeOf(today, Miqat.Dhuhr)
                        val jLabel = stringResource(Res.string.prayer_jumuah)
                        add(
                            AppTileItem(
                                title = if (jBase != null) "$jLabel · ${jBase.format(pat)}" else jLabel,
                                subtitle = if (j.enabled) offsetSubtitle(
                                    jBase,
                                    if (j.remindBeforeOn) j.remindBefore else 0,
                                    j.jamaat,
                                    j.jamaatAfter,
                                    lBefore,
                                    lJamaah,
                                    pat
                                ) else stringResource(Res.string.friday),
                                leadingIcon = Lucide.Calendar,
                                trailing = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AnimatedVisibility(j.enabled, enter = optionsEnter, exit = optionsExit) {
                                            IconButton(onClick = { sheetKey = Miqat.jumuahKey }) {
                                                Icon(
                                                    Lucide.SlidersHorizontal,
                                                    contentDescription = null,
                                                    tint = c.primary
                                                )
                                            }
                                        }
                                        AppSwitch(checked = j.enabled, onCheckedChange = { NotificationStore.setJumuahEnabled(it) })
                                    }
                                },
                                onClick = if (j.enabled) ({ sheetKey = Miqat.jumuahKey }) else null,
                            )
                        )
                    },
                )

                val mk = s.mulk
                val kf = s.kahf
                val dr = s.dailyReading
                AppTileGroup(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    title = stringResource(Res.string.notifications_quran),
                    items = listOf(
                        AppTileItem(
                            title = stringResource(Res.string.daily_quran_reminder),
                            subtitle = timeSubtitle(stringResource(Res.string.every_day), LocalTime(dr.hour, dr.minute), dr.enabled),
                            leadingIcon = Lucide.BookOpen,
                            trailing = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (dr.enabled) IconButton(onClick = { dailyReminderPicker = true }) {
                                        Icon(
                                            Lucide.SlidersHorizontal,
                                            contentDescription = null,
                                            tint = c.primary
                                        )
                                    }
                                    AppSwitch(checked = dr.enabled, onCheckedChange = NotificationStore::setDailyReadingEnabled)
                                }
                            },
                            onClick = if (dr.enabled) ({ dailyReminderPicker = true }) else null,
                        ),
                        AppTileItem(
                            title = stringResource(Res.string.surah_al_mulk),
                            subtitle = timeSubtitle(
                                stringResource(Res.string.after_isha),
                                shift(timeOf(today, Miqat.Isha), mk.afterIsha),
                                mk.enabled
                            ),
                            leadingIcon = Lucide.BookOpen,
                            trailing = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (mk.enabled) IconButton(onClick = { sheetKey = "mulk" }) {
                                        Icon(
                                            Lucide.SlidersHorizontal,
                                            contentDescription = null,
                                            tint = c.primary
                                        )
                                    }
                                    AppSwitch(checked = mk.enabled, onCheckedChange = NotificationStore::setMulkEnabled)
                                }
                            },
                            onClick = if (mk.enabled) ({ sheetKey = "mulk" }) else null,
                        ),
                        AppTileItem(
                            title = stringResource(Res.string.surah_al_kahf),
                            subtitle = timeSubtitle(stringResource(Res.string.friday), LocalTime(kf.hour, kf.minute), kf.enabled),
                            leadingIcon = Lucide.BookOpen,
                            trailing = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (kf.enabled) IconButton(onClick = { kahfPicker = true }) {
                                        Icon(
                                            Lucide.SlidersHorizontal,
                                            contentDescription = null,
                                            tint = c.primary
                                        )
                                    }
                                    AppSwitch(checked = kf.enabled, onCheckedChange = NotificationStore::setKahfEnabled)
                                }
                            },
                            onClick = if (kf.enabled) ({ kahfPicker = true }) else null,
                        ),
                    ),
                )

                val d = s.dhikr
                if (edition != AppEdition.QURAN) AppTileGroup(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    title = stringResource(Res.string.dhikr),
                    items = listOf(
                        AppTileItem(
                            title = stringResource(Res.string.morning_adhkar),
                            subtitle = timeSubtitle(
                                stringResource(Res.string.after_fajr),
                                shift(timeOf(today, Miqat.Fajr), d.afterFajr),
                                d.morningEnabled
                            ),
                            leadingIcon = Lucide.Sunrise,
                            trailing = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (d.morningEnabled) IconButton(onClick = { sheetKey = "morning" }) {
                                        Icon(
                                            Lucide.SlidersHorizontal,
                                            contentDescription = null,
                                            tint = c.primary
                                        )
                                    }
                                    AppSwitch(checked = d.morningEnabled, onCheckedChange = { NotificationStore.setMorningEnabled(it) })
                                }
                            },
                            onClick = if (d.morningEnabled) ({ sheetKey = "morning" }) else null,
                        ),
                        AppTileItem(
                            title = stringResource(Res.string.evening_adhkar),
                            subtitle = timeSubtitle(
                                stringResource(Res.string.after_asr),
                                shift(timeOf(today, Miqat.Asr), d.afterAsr),
                                d.eveningEnabled
                            ),
                            leadingIcon = Lucide.Sunset,
                            trailing = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (d.eveningEnabled) IconButton(onClick = { sheetKey = "evening" }) {
                                        Icon(
                                            Lucide.SlidersHorizontal,
                                            contentDescription = null,
                                            tint = c.primary
                                        )
                                    }
                                    AppSwitch(checked = d.eveningEnabled, onCheckedChange = { NotificationStore.setEveningEnabled(it) })
                                }
                            },
                            onClick = if (d.eveningEnabled) ({ sheetKey = "evening" }) else null,
                        ),
                    ),
                )

                if (edition != AppEdition.QURAN) AppTileGroup(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(Res.string.notifications_nafil),
                    items = listOf(
                        AppTileItem(
                            title = stringResource(Res.string.tahajjud),
                            subtitle = timeSubtitle(
                                stringResource(Res.string.last_third_of_the_night),
                                timeOf(today, Miqat.LastThird),
                                s.nafil.tahajjud
                            ),
                            leadingIcon = Lucide.Moon,
                            trailing = { AppSwitch(checked = s.nafil.tahajjud, onCheckedChange = NotificationStore::setTahajjud) }),
                        AppTileItem(
                            title = stringResource(Res.string.ishraq),
                            subtitle = timeSubtitle(stringResource(Res.string.mid_morning), timeOf(today, Miqat.Ishraq), s.nafil.ishraq),
                            leadingIcon = Lucide.SunMedium,
                            trailing = { AppSwitch(checked = s.nafil.ishraq, onCheckedChange = NotificationStore::setIshraq) }),
                    ),
                )

                // parked for now — not wired to a real trigger yet
                if (false) AppTileGroup(
                    items = listOf(
                        AppTileItem(
                            title = stringResource(Res.string.verse_of_the_day),
                            subtitle = stringResource(Res.string.notif_verse_sub),
                            leadingIcon = Lucide.Sparkles,
                            trailing = { AppSwitch(checked = verseOfDay, onCheckedChange = { verseOfDay = it }) })
                    )
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (kahfPicker) {
        val state = rememberTimePickerState(initialHour = s.kahf.hour, initialMinute = s.kahf.minute, is24Hour = timeFormat == TimeFormat.TwentyFour)
        AppBottomSheet(onDismiss = { kahfPicker = false }, title = stringResource(Res.string.reminder_time)) {
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TimePicker(state = state)
                AppButton(
                    text = stringResource(Res.string.save),
                    onClick = { NotificationStore.setKahfTime(state.hour, state.minute); kahfPicker = false },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (dailyReminderPicker) {
        val state = rememberTimePickerState(initialHour = s.dailyReading.hour, initialMinute = s.dailyReading.minute, is24Hour = timeFormat == TimeFormat.TwentyFour)
        AppBottomSheet(onDismiss = { dailyReminderPicker = false }, title = stringResource(Res.string.reminder_time)) {
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TimePicker(state = state)
                AppButton(
                    text = stringResource(Res.string.save),
                    onClick = { NotificationStore.setDailyReadingTime(state.hour, state.minute); dailyReminderPicker = false },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    sheetKey?.let { key ->
        val min = stringResource(Res.string.minutes_short)
        val pat = timeFormat.pattern
        val lb = stringResource(Res.string.notif_at_before)
        val lj = stringResource(Res.string.notif_at_jamaah)
        when (key) {
            Miqat.jumuahKey -> {
                val j = s.jumuah
                val jBase = timeOf(today, Miqat.Dhuhr)
                AppBottomSheet(
                    onDismiss = { sheetKey = null },
                    title = if (jBase != null) "${stringResource(Res.string.prayer_jumuah)} · ${jBase.format(pat)}" else stringResource(Res.string.prayer_jumuah),
                    subtitle = if (j.enabled) offsetSubtitle(
                        jBase,
                        if (j.remindBeforeOn) j.remindBefore else 0,
                        j.jamaat,
                        j.jamaatAfter,
                        lb,
                        lj,
                        pat
                    ) else null,
                ) {
                    AppTileGroup(modifier = Modifier.fillMaxWidth().animateContentSize(), items = buildList {
                        add(
                            AppTileItem(
                                title = stringResource(Res.string.remind_before),
                                trailing = {
                                    AppSwitch(
                                        checked = j.remindBeforeOn,
                                        onCheckedChange = { NotificationStore.setJumuahRemindBeforeOn(it) })
                                })
                        )
                        if (j.remindBeforeOn) add(
                            AppTileItem(
                                title = stringResource(Res.string.minutes_before),
                                trailing = {
                                    MiniStepper(
                                        j.remindBefore,
                                        min,
                                        { NotificationStore.setJumuahRemindBefore(it) },
                                        N.Jumuah.remindBeforeMin,
                                        N.Jumuah.remindBeforeMax,
                                        N.Jumuah.step
                                    )
                                })
                        )
                        add(
                            AppTileItem(
                                title = stringResource(Res.string.jamaat_reminder),
                                trailing = { AppSwitch(checked = j.jamaat, onCheckedChange = { NotificationStore.setJumuahJamaat(it) }) })
                        )
                        if (j.jamaat) add(
                            AppTileItem(
                                title = stringResource(Res.string.jamaat_after_start),
                                trailing = {
                                    MiniStepper(
                                        j.jamaatAfter,
                                        min,
                                        { NotificationStore.setJumuahJamaatAfter(it) },
                                        N.Jumuah.jamaatAfterMin,
                                        N.Jumuah.jamaatAfterMax,
                                        N.Jumuah.step
                                    )
                                })
                        )
                    })
                }
            }

            "mulk" -> {
                val mk = s.mulk
                AppBottomSheet(
                    onDismiss = { sheetKey = null },
                    title = stringResource(Res.string.surah_al_mulk),
                    subtitle = shift(timeOf(today, Miqat.Isha), mk.afterIsha)?.format(pat),
                ) {
                    AppTileGroup(
                        modifier = Modifier.fillMaxWidth(),
                        items = listOf(
                            AppTileItem(
                                title = stringResource(Res.string.after_isha),
                                trailing = {
                                    MiniStepper(
                                        mk.afterIsha,
                                        min,
                                        NotificationStore::setMulkAfter,
                                        N.Mulk.afterIshaMin,
                                        N.Mulk.afterIshaMax,
                                        N.Mulk.step
                                    )
                                })
                        )
                    )
                }
            }

            "morning" -> {
                val d = s.dhikr
                AppBottomSheet(
                    onDismiss = { sheetKey = null },
                    title = stringResource(Res.string.morning_adhkar),
                    subtitle = shift(timeOf(today, Miqat.Fajr), d.afterFajr)?.format(pat),
                ) {
                    AppTileGroup(
                        modifier = Modifier.fillMaxWidth(),
                        items = listOf(
                            AppTileItem(
                                title = stringResource(Res.string.after_fajr),
                                trailing = {
                                    MiniStepper(
                                        d.afterFajr,
                                        min,
                                        NotificationStore::setMorningAfter,
                                        N.Dhikr.offsetMin,
                                        N.Dhikr.offsetMax,
                                        N.Dhikr.step
                                    )
                                })
                        )
                    )
                }
            }

            "evening" -> {
                val d = s.dhikr
                AppBottomSheet(
                    onDismiss = { sheetKey = null },
                    title = stringResource(Res.string.evening_adhkar),
                    subtitle = shift(timeOf(today, Miqat.Asr), d.afterAsr)?.format(pat),
                ) {
                    AppTileGroup(
                        modifier = Modifier.fillMaxWidth(),
                        items = listOf(
                            AppTileItem(
                                title = stringResource(Res.string.after_asr),
                                trailing = {
                                    MiniStepper(
                                        d.afterAsr,
                                        min,
                                        NotificationStore::setEveningAfter,
                                        N.Dhikr.offsetMin,
                                        N.Dhikr.offsetMax,
                                        N.Dhikr.step
                                    )
                                })
                        )
                    )
                }
            }

            else -> {
                val p = Miqat.PRAYERS.first { it.key == key }
                val cfg = s.prayers.getValue(key)
                val base = timeOf(today, p)
                AppBottomSheet(
                    onDismiss = { sheetKey = null },
                    title = if (base != null) "${stringResource(p.labelRes)} · ${base.format(pat)}" else stringResource(p.labelRes),
                    subtitle = offsetSubtitle(base, if (cfg.remindBeforeOn) cfg.remindBefore else 0, cfg.jamaat, cfg.jamaatAfter, lb, lj, pat),
                ) {
                    AppTileGroup(modifier = Modifier.fillMaxWidth().animateContentSize(), items = buildList {
                        add(
                            AppTileItem(
                                title = stringResource(Res.string.at_prayer_time),
                                trailing = { AppSwitch(checked = cfg.atTime, onCheckedChange = { NotificationStore.setPrayerAtTime(key, it) }) })
                        )
                        add(
                            AppTileItem(
                                title = stringResource(Res.string.remind_before),
                                trailing = {
                                    AppSwitch(
                                        checked = cfg.remindBeforeOn,
                                        onCheckedChange = { NotificationStore.setPrayerRemindBeforeOn(key, it) })
                                })
                        )
                        if (cfg.remindBeforeOn) add(
                            AppTileItem(
                                title = stringResource(Res.string.minutes_before),
                                trailing = {
                                    MiniStepper(
                                        cfg.remindBefore,
                                        min,
                                        { NotificationStore.setPrayerRemindBefore(key, it) },
                                        N.Prayer.remindBeforeMin,
                                        N.Prayer.remindBeforeMax,
                                        N.Prayer.step
                                    )
                                })
                        )
                        add(
                            AppTileItem(
                                title = stringResource(Res.string.jamaat_reminder),
                                trailing = { AppSwitch(checked = cfg.jamaat, onCheckedChange = { NotificationStore.setPrayerJamaat(key, it) }) })
                        )
                        if (cfg.jamaat) add(
                            AppTileItem(
                                title = stringResource(Res.string.jamaat_after_start),
                                trailing = {
                                    MiniStepper(
                                        cfg.jamaatAfter,
                                        min,
                                        { NotificationStore.setPrayerJamaatAfter(key, it) },
                                        N.Prayer.jamaatAfterMin,
                                        N.Prayer.jamaatAfterMax,
                                        N.Prayer.step
                                    )
                                })
                        )
                    })
                }
            }
        }
    }
}
