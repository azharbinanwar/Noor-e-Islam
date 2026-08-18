package com.kodeelite.nooreislam.core.database

import androidx.room.TypeConverter
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.PrayerTrackerStatus
import com.kodeelite.nooreislam.feature.quran.data.HighlightColor
import kotlinx.datetime.LocalDate

// Room column converters. Enums persisted by name, dates by ISO string (LocalDate.toString()).
class Converters {
    @TypeConverter
    fun colorToName(c: HighlightColor): String = c.name
    @TypeConverter
    fun nameToColor(n: String): HighlightColor = HighlightColor.valueOf(n)

    @TypeConverter
    fun miqatToName(m: Miqat): String = m.name
    @TypeConverter
    fun nameToMiqat(n: String): Miqat = Miqat.valueOf(n)

    @TypeConverter
    fun statusToName(s: PrayerTrackerStatus): String = s.name
    @TypeConverter
    fun nameToStatus(n: String): PrayerTrackerStatus = PrayerTrackerStatus.valueOf(n)

    @TypeConverter
    fun dateToString(d: LocalDate?): String? = d?.toString()
    @TypeConverter
    fun stringToDate(s: String?): LocalDate? = s?.let(LocalDate::parse)
}
