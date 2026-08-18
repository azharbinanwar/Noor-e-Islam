package com.kodeelite.nooreislam.feature.notifications.presentation

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.BellPlus
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.SlidersHorizontal
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppSwitch
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.datetime.labelRes
import com.kodeelite.nooreislam.core.enums.TimeFormat
import com.kodeelite.nooreislam.core.focus.rememberFocusSetup
import com.kodeelite.nooreislam.core.locale.tr
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.core.permissions.AppPermission
import com.kodeelite.nooreislam.core.permissions.PermissionStatus
import com.kodeelite.nooreislam.core.permissions.rememberPermissionService
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.notifications.data.EVERY_DAY
import com.kodeelite.nooreislam.feature.notifications.data.SurahReminder
import com.kodeelite.nooreislam.feature.notifications.data.toDaySet
import com.kodeelite.nooreislam.feature.notifications.presentation.components.BatteryNeedsAttention
import com.kodeelite.nooreislam.feature.notifications.presentation.components.ExactAlarmNeedsAttention
import com.kodeelite.nooreislam.feature.notifications.presentation.components.NotificationTestScreen
import com.kodeelite.nooreislam.feature.notifications.presentation.components.NotificationsNeedsAttention
import com.kodeelite.nooreislam.feature.notifications.presentation.components.SurahReminderSheet
import com.kodeelite.nooreislam.feature.notifications.store.NotificationStore
import com.kodeelite.nooreislam.feature.notifications.store.SurahReminderStore
import com.kodeelite.nooreislam.feature.quran.data.QuranRepository
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.add_reminder
import com.kodeelite.nooreislam.resources.all_alerts
import com.kodeelite.nooreislam.resources.back
import com.kodeelite.nooreislam.resources.daily_quran_reminder
import com.kodeelite.nooreislam.resources.every_day
import com.kodeelite.nooreislam.resources.master_switch_for_every_reminder
import com.kodeelite.nooreislam.resources.notifications
import com.kodeelite.nooreislam.resources.reminder_time
import com.kodeelite.nooreislam.resources.no_reminders_yet
import com.kodeelite.nooreislam.resources.save
import com.kodeelite.nooreislam.resources.surah_reminders
import kotlinx.coroutines.delay
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

/**
 * The Quran app's notifications. Same shell as [NotificationsScreen] — banners, master switch,
 * collapse-when-off — minus everything that needs prayer times. Every reminder is listed inline,
 * so Al-Mulk and Al-Kahf are ordinary rows the user can retime or rename, not fixed tiles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranNotificationsScreen() {
    val nav = LocalAppNavigator.current
    val c = AppTheme.colors
    val store = koinInject<SurahReminderStore>()
    val reminders by store.reminders.collectAsState()
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val s by NotificationStore.settings.collectAsState()
    val surahs by produceState(emptyList()) { value = QuranRepository.surahs() }

    val perms = rememberPermissionService()
    var notifGranted by remember { mutableStateOf(true) }
    var exactAlarmGranted by remember { mutableStateOf(true) }
    val focus = rememberFocusSetup()
    var permDump by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            notifGranted = perms.status(AppPermission.Notifications) == PermissionStatus.Granted
            exactAlarmGranted = perms.status(AppPermission.ExactAlarm) == PermissionStatus.Granted
            permDump = "exact=${perms.status(AppPermission.ExactAlarm)} · notif=${perms.status(AppPermission.Notifications)}" +
                    " · battery=${if (focus.batteryUnrestricted()) "unrestricted" else "optimized"}" +
                    " · bgRestricted=${focus.backgroundRestricted()}"
            delay(1500.milliseconds)
        }
    }
    val remindersLocked = !notifGranted || !exactAlarmGranted

    var taps by remember { mutableStateOf(0) } // 7 taps opens the dev test screen
    var showTest by remember { mutableStateOf(false) }
    var dailyReminderPicker by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<SurahReminder?>(null) }
    var reminderSheet by remember { mutableStateOf(false) }
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
        floatingActionButton = {
            if (s.allAlerts && !remindersLocked) FloatingActionButton(onClick = { editing = null; reminderSheet = true }) {
                Icon(Lucide.BellPlus, stringResource(Res.string.add_reminder))
            }
        },
    ) { pad ->
        val everyDay = stringResource(Res.string.every_day)
        val dayNames = DayOfWeek.entries.associateWith { stringResource(it.labelRes) }
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
        ) {
            // live permission readout, for testing only — uncomment with permDump above
//            if (permDump.isNotEmpty()) {
//                Text(permDump, fontSize = 11.sp, color = c.onSurfaceVariant)
//                Spacer(Modifier.height(12.dp))
//            }
            NotificationsNeedsAttention()
            ExactAlarmNeedsAttention()
            BatteryNeedsAttention()

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

            if (s.allAlerts && !remindersLocked) {
                val dr = s.dailyReading
                AppTileGroup(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
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
                    ),
                )

                AppTileGroup(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    title = stringResource(Res.string.surah_reminders),
                    items = reminders.map { r ->
                        val surah = surahs.firstOrNull { it.number == r.surah }
                        AppTileItem(
                            // blank title = never named, so the surah reads in the current language
                            title = r.title.ifBlank { surah?.let { tr(it.nameTransliterated, it.nameArabic) }.orEmpty() },
                            subtitle = timeSubtitle(daysLabel(r.days, everyDay, dayNames), LocalTime(r.hour, r.minute), r.enabled),
                            leadingIcon = Lucide.BookOpen,
                            trailing = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // always shown, unlike the enabled-only sliders elsewhere: every
                                    // reminder ships off, so this is the only way in to retime one
                                    IconButton(onClick = { editing = r; reminderSheet = true }) {
                                        Icon(Lucide.SlidersHorizontal, contentDescription = null, tint = c.primary)
                                    }
                                    AppSwitch(checked = r.enabled, onCheckedChange = { store.setEnabled(r, it) })
                                }
                            },
                        )
                    }.ifEmpty { listOf(AppTileItem(title = stringResource(Res.string.no_reminders_yet))) },
                )
            }

            Spacer(Modifier.height(80.dp)) // clears the FAB
        }
    }

    if (reminderSheet) SurahReminderSheet(
        existing = editing,
        onSave = { store.save(it); reminderSheet = false },
        onDelete = { store.delete(it); reminderSheet = false },
        onDismiss = { reminderSheet = false },
    )

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
}

// "Every day", or the picked weekdays in week order.
private fun daysLabel(mask: Int, everyDay: String, names: Map<DayOfWeek, String>): String =
    if (mask == EVERY_DAY) everyDay
    else mask.toDaySet().sortedBy { it.ordinal }.joinToString(", ") { names.getValue(it) }
