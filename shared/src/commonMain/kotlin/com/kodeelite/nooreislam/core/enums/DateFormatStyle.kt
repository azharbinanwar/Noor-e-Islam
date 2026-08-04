package com.kodeelite.nooreislam.core.enums

import androidx.compose.runtime.Composable
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.date_format_compact
import com.kodeelite.nooreislam.resources.date_format_compact_year_first
import com.kodeelite.nooreislam.resources.date_format_dash
import com.kodeelite.nooreislam.resources.date_format_day_month
import com.kodeelite.nooreislam.resources.date_format_dot
import com.kodeelite.nooreislam.resources.date_format_iso
import com.kodeelite.nooreislam.resources.date_format_long
import com.kodeelite.nooreislam.resources.date_format_medium
import com.kodeelite.nooreislam.resources.date_format_short
import com.kodeelite.nooreislam.resources.date_format_slash
import com.kodeelite.nooreislam.resources.date_format_us
import com.kodeelite.nooreislam.resources.date_format_weekday
import com.kodeelite.nooreislam.resources.date_format_weekday_compact
import com.kodeelite.nooreislam.resources.date_format_year_first
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * One shared "shape" for how a date displays — reused for both the Gregorian and Hijri date settings
 * (two separate SettingsStore values, same enum type). [pattern] feeds LocalDate.format(pattern) for
 * Gregorian; Hijri has no LocalDate pattern engine, so it's composed by hand per style — see
 * DateFormatPickerSheet.hijriPreview.
 */
enum class DateFormatStyle(private val labelRes: StringResource, val pattern: String) {
    Short(Res.string.date_format_short, "d MMM yyyy"),               // 12 Jul 2026
    Medium(Res.string.date_format_medium, "d MMMM yyyy"),            // 12 July 2026
    Long(Res.string.date_format_long, "EEEE, d MMMM yyyy"),          // Sunday, 12 July 2026
    Slash(Res.string.date_format_slash, "dd/MM/yyyy"),               // 12/07/2026
    Dash(Res.string.date_format_dash, "dd-MM-yyyy"),                 // 12-07-2026
    Dot(Res.string.date_format_dot, "dd.MM.yyyy"),                   // 12.07.2026
    Iso(Res.string.date_format_iso, "yyyy-MM-dd"),                   // 2026-07-12
    YearFirstSlash(Res.string.date_format_year_first, "yyyy/MM/dd"), // 2026/07/12
    Compact(Res.string.date_format_compact, "dd/MM/yy"),             // 12/07/26
    UsStyle(Res.string.date_format_us, "MMM d, yyyy"),                // Aug 12, 2026
    Weekday(Res.string.date_format_weekday, "EEE, d MMM yyyy"),       // Tue, 12 Aug 2026
    WeekdayCompact(Res.string.date_format_weekday_compact, "EEE, MMM d"), // Tue, Aug 12
    DayMonth(Res.string.date_format_day_month, "d MMM"),              // 12 Aug
    CompactYearFirst(Res.string.date_format_compact_year_first, "yy/MM/dd"), // 26/08/12
    ;

    val value: String get() = name

    @Composable
    fun label(): String = stringResource(labelRes)

    companion object {
        val default = Short
        fun fromValue(value: String?) = entries.firstOrNull { it.value == value } ?: default
    }
}
