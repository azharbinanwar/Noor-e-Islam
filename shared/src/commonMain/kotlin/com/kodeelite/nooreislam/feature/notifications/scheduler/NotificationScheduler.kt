package com.kodeelite.nooreislam.feature.notifications.scheduler

import com.kodeelite.nooreislam.core.constants.defaults.NotificationDefaults
import com.kodeelite.nooreislam.core.datetime.currentDate
import com.kodeelite.nooreislam.core.datetime.currentTime
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.NotificationType
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.miqat.domain.MiqatTime
import com.kodeelite.nooreislam.feature.miqat.store.MiqatTimesStore
import com.kodeelite.nooreislam.feature.notifications.data.NotificationScheduleRepository
import com.kodeelite.nooreislam.feature.notifications.data.SurahReminder
import com.kodeelite.nooreislam.feature.notifications.data.firesOn
import com.kodeelite.nooreislam.feature.notifications.store.SurahReminderStore
import com.kodeelite.nooreislam.feature.notifications.store.NotificationSettings
import com.kodeelite.nooreislam.feature.notifications.store.NotificationStore
import com.kodeelite.nooreislam.feature.notifications.store.NotificationTestStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import org.koin.mp.KoinPlatform
import kotlin.time.Duration.Companion.minutes

// The brain: settings + prayer times -> the nearest 63 alerts. Rebuilds on any change; writes the OS + the mirror.
object NotificationScheduler {
    private val repo: NotificationScheduleRepository by lazy { KoinPlatform.getKoin().get<NotificationScheduleRepository>() }
    private val reminderStore: SurahReminderStore by lazy { KoinPlatform.getKoin().get<SurahReminderStore>() }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()
    private var started = false

    // Call once at app start (after AppCtx on Android). Rebuilds now + on every settings/time change.
    fun start() {
        if (started) return
        started = true
        rebuildAsync()
        scope.launch {
            // language included: copy is resolved here, not at fire time, so a switch must reschedule
            combine(MiqatTimesStore.today, NotificationStore.settings, reminderStore.reminders, SettingsStore.language) { t, s, r, l ->
                listOf(t, s, r, l)
            }
                .distinctUntilChanged()
                .drop(1)
                .collect { rebuild() }
        }
    }

    // Fire-and-forget rebuild (app open).
    fun rebuildAsync() {
        scope.launch { rebuild() }
    }

    // Same work, but the caller waits. Android receivers use this under goAsync() so the process
    // isn't killed with the alarms half-armed.
    suspend fun rebuildNow() = rebuild()

    // Dev test sheet: the current scheduled mirror.
    fun scheduled() = repo.observeUpcoming()

    private suspend fun rebuild() = mutex.withLock {
        val reminders = reminderStore.current()
        val events = computeDesired(reminders).mapIndexed { i, e -> e.copy(slotId = i) }
        LocalNotifier.cancelAll()
        val armed = events.map { e ->
            val c = notificationCopy(e)
            LocalNotifier.schedule(e, c.title, c.body)
            e.toEntity(c.title, c.body)
        }
        repo.replaceAll(armed)
    }

    // Expand settings + times over the horizon, drop past, keep the nearest 63 by time.
    private fun computeDesired(reminders: List<SurahReminder>): List<NotificationEvent> {
        val tz = TimeZone.currentSystemDefault()
        val settings = NotificationStore.settings.value
        val today = currentDate()
        val now = LocalDateTime(today, currentTime()).toInstant(tz).toEpochMilliseconds()
        NotificationTestStore.prunePast(now)
        // Master gate: when All alerts is off, no real alerts are scheduled (test slots still fire — dev tool).
        val real = if (!settings.allAlerts) emptyList() else (0 until NotificationDefaults.Scheduler.horizonDays).flatMap { d ->
            val date = today.plus(d, DateTimeUnit.DAY)
            eventsFor(date, MiqatTimesStore.timesFor(date), settings, tz) +
                    reminderEventsFor(date, reminders, tz)
        }
        val test = NotificationTestStore.items.value.map {
            NotificationEvent("test:${it.id}", "test", NotificationType.REMINDER, it.fireAtMillis)
        }
        return (real + test).filter { it.fireAtMillis > now }.sortedBy { it.fireAtMillis }.take(NotificationDefaults.Scheduler.budget)
    }

    private fun eventsFor(date: LocalDate, times: List<MiqatTime>, s: NotificationSettings, tz: TimeZone): List<NotificationEvent> = buildList {
        val friday = date.dayOfWeek == DayOfWeek.FRIDAY
        val ds = date.toString()
        fun at(m: Miqat): Long? = times.firstOrNull { it.miqat == m }?.at?.toInstant(tz)?.toEpochMilliseconds()

        // Daily prayers — on Friday the Dhuhr row yields to Jumu'ah below.
        Miqat.PRAYERS.forEach { p ->
            if (friday && p == Miqat.Dhuhr) return@forEach
            val cfg = s.prayers[p.key] ?: return@forEach
            if (!cfg.enabled) return@forEach
            val base = at(p) ?: return@forEach
            if (cfg.remindBeforeOn && cfg.remindBefore > 0) add(ev(p.key, NotificationType.REMIND_BEFORE, base - cfg.remindBefore.mins(), ds))
            if (cfg.atTime) add(ev(p.key, NotificationType.AT_TIME, base, ds))
            if (cfg.jamaat) add(ev(p.key, NotificationType.JAMAAT, base + cfg.jamaatAfter.mins(), ds))
        }
        // Jumu'ah (Friday Dhuhr)
        if (friday && s.jumuah.enabled) at(Miqat.Dhuhr)?.let { d ->
            val j = s.jumuah
            if (j.remindBeforeOn && j.remindBefore > 0) add(ev(Miqat.jumuahKey, NotificationType.REMIND_BEFORE, d - j.remindBefore.mins(), ds))
            if (j.jamaat) add(ev(Miqat.jumuahKey, NotificationType.JAMAAT, d + j.jamaatAfter.mins(), ds))
        }
        // Surahs
        if (s.mulk.enabled) at(Miqat.Isha)?.let {
            add(ev(NotificationTarget.MULK, NotificationType.REMINDER, it + s.mulk.afterIsha.mins(), ds, AppRoute.QuranReader(67, 1)))
        }
        if (friday && s.kahf.enabled) {
            val k = LocalDateTime(date, LocalTime(s.kahf.hour, s.kahf.minute)).toInstant(tz).toEpochMilliseconds()
            add(ev(NotificationTarget.KAHF, NotificationType.REMINDER, k, ds, AppRoute.QuranReader(18, 1)))
        }
        if (s.dailyReading.enabled) {
            val r = LocalDateTime(date, LocalTime(s.dailyReading.hour, s.dailyReading.minute)).toInstant(tz).toEpochMilliseconds()
            add(ev(NotificationTarget.DAILY_READING, NotificationType.REMINDER, r, ds, AppRoute.Quran))
        }
        // Dhikr
        if (s.dhikr.morningEnabled) at(Miqat.Fajr)?.let {
            add(
                ev(
                    NotificationTarget.MORNING,
                    NotificationType.REMINDER,
                    it + s.dhikr.afterFajr.mins(),
                    ds,
                    AppRoute.Azkar,
                )
            )
        }
        if (s.dhikr.eveningEnabled) at(Miqat.Asr)?.let {
            add(
                ev(
                    NotificationTarget.EVENING,
                    NotificationType.REMINDER,
                    it + s.dhikr.afterAsr.mins(),
                    ds,
                    AppRoute.Azkar,
                )
            )
        }
        // Nafil
        if (s.nafil.tahajjud) at(Miqat.LastThird)?.let { add(ev(NotificationTarget.TAHAJJUD, NotificationType.REMINDER, it, ds)) }
        if (s.nafil.ishraq) at(Miqat.Ishraq)?.let { add(ev(NotificationTarget.ISHRAQ, NotificationType.REMINDER, it, ds)) }
    }

    // User-defined reminders: one event per enabled reminder whose day mask covers this date.
    private fun reminderEventsFor(date: LocalDate, reminders: List<SurahReminder>, tz: TimeZone): List<NotificationEvent> =
        reminders.filter { it.enabled && it.firesOn(date.dayOfWeek) }.map { r ->
            NotificationEvent(
                eventKey = "${NotificationTarget.SURAH_REMINDER}:${r.id}:$date",
                target = NotificationTarget.SURAH_REMINDER,
                kind = NotificationType.REMINDER,
                fireAtMillis = LocalDateTime(date, LocalTime(r.hour, r.minute)).toInstant(tz).toEpochMilliseconds(),
                title = r.title.ifBlank { null }, // blank = never named; copy resolves the seed name
                route = AppRoute.QuranReader(r.surah, r.ayah ?: 1),
            )
        }

    private fun ev(target: String, kind: NotificationType, fireAt: Long, date: String, route: AppRoute? = null) =
        NotificationEvent("$target:$kind:$date", target, kind, fireAt, route = route)

    private fun Int.mins() = this.minutes.inWholeMilliseconds
}
