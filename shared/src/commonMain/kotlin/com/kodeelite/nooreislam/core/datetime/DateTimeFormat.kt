package com.kodeelite.nooreislam.core.datetime

import androidx.compose.runtime.Composable
import com.kodeelite.nooreislam.core.enums.DateFormatStyle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern

/**
 * Flutter-style `DateFormat`: the pattern is the whole instruction, the method returns exactly what it
 * describes, with no fixed notion of "time" vs "date".
 *
 *   time.format("mm")               -> "08"                 (just the minute)
 *   time.format("HH:mm")            -> "17:08"              (time, 24h)
 *   time.format("h:mm a")           -> "5:08 PM"            (time, 12h)
 *   date.format("dd/MM/yyyy")       -> "12/07/2026"         (date)
 *   dt.format("yyyy-MM-dd HH:mm")   -> "2026-07-12 17:08"   (date + time)
 *
 * Unicode letters: y year, M month, d day, E weekday, H 24h, h 12h, m minute, s second, a AM/PM.
 * byUnicodePattern rejects locale-dependent letters (clock h/a/K/k, month/weekday NAMES MMM/EEE), so we
 * pre-expand just those from the receiver into quoted literals and hand the rest to it. English names.
 */
@OptIn(FormatStringsInDatetimeFormats::class)
fun LocalDateTime.format(pattern: String): String =
    LocalDateTime.Format { byUnicodePattern(expandNames(expandClock(pattern, hour), month.ordinal, dayOfWeek.ordinal)) }.format(this)

@OptIn(FormatStringsInDatetimeFormats::class)
fun LocalTime.format(pattern: String): String =
    LocalTime.Format { byUnicodePattern(expandClock(pattern, hour)) }.format(this)

@OptIn(FormatStringsInDatetimeFormats::class)
fun LocalDate.format(pattern: String): String =
    LocalDate.Format { byUnicodePattern(expandNames(pattern, month.ordinal, dayOfWeek.ordinal)) }.format(this)

/** The user's chosen [DateFormatStyle], applied — single source of truth for Gregorian display, see also [Now.formattedDate]. */
fun LocalDate.formatted(style: DateFormatStyle): String = format(style.pattern)

/**
 * The user's chosen [DateFormatStyle], applied to a Hijri date — single source of truth for Hijri
 * display, see also [Now.formattedHijri]. Hijri isn't a LocalDate, so unlike [LocalDate.formatted]
 * this composes the string by hand per style, and needs @Composable for the localized month name.
 * Medium/Long land on the same shape — Hijri months have no abbreviated form and no easy weekday name.
 */
@Composable
fun HijriDate.formatted(style: DateFormatStyle): String {
    val monthLabel = HijriMonth.of(month).label()
    val dd = day.toString().padStart(2, '0')
    val mm = month.toString().padStart(2, '0')
    val yyyy = year.toString()
    val yy = yyyy.takeLast(2)
    return when (style) {
        DateFormatStyle.Short -> "$day $monthLabel $yyyy"
        DateFormatStyle.Medium, DateFormatStyle.Long -> "$monthLabel $day, $yyyy"
        DateFormatStyle.Slash -> "$dd/$mm/$yyyy"
        DateFormatStyle.Dash -> "$dd-$mm-$yyyy"
        DateFormatStyle.Dot -> "$dd.$mm.$yyyy"
        DateFormatStyle.Iso -> "$yyyy-$mm-$dd"
        DateFormatStyle.YearFirstSlash -> "$yyyy/$mm/$dd"
        DateFormatStyle.Compact -> "$dd/$mm/$yy"
        DateFormatStyle.UsStyle -> "$monthLabel $day, $yyyy"
        DateFormatStyle.Weekday -> "$day $monthLabel $yyyy"
        DateFormatStyle.WeekdayCompact -> "$monthLabel $day"
        DateFormatStyle.DayMonth -> "$day $monthLabel"
        DateFormatStyle.CompactYearFirst -> "$yy/$mm/$dd"
    }
}

private val MONTH_SHORT = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
private val MONTH_FULL =
    listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
private val DAY_SHORT = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val DAY_FULL = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

/**
 * Rewrite month/weekday NAME runs into quoted literals (MMM->Jul, MMMM->July, EEE->Fri, EEEE->Friday) so
 * byUnicodePattern accepts them. Runs of 1-2 M stay (numeric month, natively supported). Indices are enum
 * ordinals: [month] Jan=0, [dow] Mon=0 — the arrays are declared in that order.
 */
private fun expandNames(pattern: String, month: Int, dow: Int): String {
    val out = StringBuilder()
    var i = 0
    var inQuote = false
    while (i < pattern.length) {
        val c = pattern[i]
        when {
            c == '\'' -> {
                inQuote = !inQuote; out.append(c); i++
            }

            inQuote -> {
                out.append(c); i++
            }

            c == 'M' -> {
                var n = 0; while (i + n < pattern.length && pattern[i + n] == 'M') n++
                if (n >= 3) out.append('\'').append((if (n == 3) MONTH_SHORT else MONTH_FULL)[month]).append('\'')
                else repeat(n) { out.append('M') }
                i += n
            }

            c == 'E' -> {
                var n = 0; while (i + n < pattern.length && pattern[i + n] == 'E') n++
                if (n >= 3) out.append('\'').append((if (n == 3) DAY_SHORT else DAY_FULL)[dow]).append('\'')
                else repeat(n) { out.append('E') }
                i += n
            }

            else -> {
                out.append(c); i++
            }
        }
    }
    return out.toString()
}

/** Rewrite 12h/AM-PM letters (h hh a K k) as quoted literals so byUnicodePattern accepts the pattern. */
private fun expandClock(pattern: String, hour: Int): String {
    if (pattern.none { it == 'h' || it == 'a' || it == 'K' || it == 'k' }) return pattern
    val out = StringBuilder()
    var i = 0
    var inQuote = false
    while (i < pattern.length) {
        val c = pattern[i]
        when {
            c == '\'' -> {
                inQuote = !inQuote; out.append(c); i++
            }

            inQuote -> {
                out.append(c); i++
            }

            c == 'h' -> {                                   // 12h clock: 0/12 -> 12
                var n = 0; while (i + n < pattern.length && pattern[i + n] == 'h') n++
                out.append('\'').append((((hour + 11) % 12) + 1).toString().padStart(if (n >= 2) 2 else 1, '0')).append('\'')
                i += n
            }

            c == 'a' -> {
                out.append(if (hour < 12) "'AM'" else "'PM'"); i++
            }

            c == 'K' -> {
                out.append('\'').append(hour % 12).append('\''); i++
            }        // 0-11
            c == 'k' -> {
                out.append('\'').append(if (hour == 0) 24 else hour).append('\''); i++
            } // 1-24
            else -> {
                out.append(c); i++
            }
        }
    }
    return out.toString()
}
