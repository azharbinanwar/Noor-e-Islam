package com.kodeelite.nooreislam.core.datetime

import androidx.compose.runtime.Composable
import com.kodeelite.nooreislam.core.debug.Debug
import com.kodeelite.nooreislam.core.store.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.milliseconds

/** App clock: date, time and Hijri from one place. Debug can pin a date or run a fast clock. */
object Now {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val startDate = Debug.DATE_OVERRIDE ?: currentDate()

    private val _now = MutableStateFlow(
        if (Debug.FAST_CLOCK) LocalDateTime(startDate, LocalTime(0, 0)) else LocalDateTime(startDate, currentTime()),
    )

    /** The clock. Collect this; it advances on its own. */
    val now: StateFlow<LocalDateTime> = _now.asStateFlow()

    init {
        scope.launch {
            if (Debug.FAST_CLOCK) {
                // +5 virtual minutes per tick, rolls through all days
                var m = 0
                while (true) {
                    _now.value = LocalDateTime(startDate.plus(m / 1440, DateTimeUnit.DAY), LocalTime((m % 1440) / 60, (m % 1440) % 60))
                    m += 5
                    delay(80.milliseconds)
                }
            } else {
                while (true) {
                    _now.value = LocalDateTime(Debug.DATE_OVERRIDE ?: currentDate(), currentTime())
                    delay(1_000.milliseconds) // per-second so the countdown ticks and prayers switch on time
                }
            }
        }
    }

    /** Today (or the pushed/running debug date). */
    fun date(): LocalDate = _now.value.date

    /** Wall-clock time now. */
    fun time(): LocalTime = _now.value.time

    /** Today's Hijri date with [offsetDays] applied. */
    fun hijri(offsetDays: Int = 0): HijriDate = toHijri(date().plus(offsetDays, DateTimeUnit.DAY))

    /** Wall-clock epoch millis — for stored timestamps (e.g. "saved at"). */
    fun epochMillis(): Long = currentEpochMillis()

    /** Today, in the user's chosen Gregorian date format (Settings ▸ Date formats). One call, no pattern to carry around. */
    fun formattedDate(): String = date().formatted(SettingsStore.gregorianDateFormat.value)

    /** Today's Hijri date (± [offsetDays]), in the user's chosen Hijri date format. */
    @Composable
    fun formattedHijri(offsetDays: Int = 0): String = hijri(offsetDays).formatted(SettingsStore.hijriDateFormat.value)

    private fun localDateTime(epochMillis: Long): LocalDateTime =
        kotlin.time.Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())

    /** An arbitrary stored timestamp's date (e.g. a note's createdAt), in the user's chosen Gregorian date format. */
    fun formattedTimestamp(epochMillis: Long): String = localDateTime(epochMillis).date.formatted(SettingsStore.gregorianDateFormat.value)

    /** An arbitrary stored timestamp's time, in the user's chosen 12h/24h format. */
    fun formattedTime(epochMillis: Long): String = localDateTime(epochMillis).time.format(SettingsStore.timeFormat.value.pattern)

    /** An arbitrary stored timestamp, date + time together — each half in its own chosen format. */
    fun formattedDateTime(epochMillis: Long): String = "${formattedTimestamp(epochMillis)}, ${formattedTime(epochMillis)}"
}
