package com.kodeelite.nooreislam.core.database

import androidx.room.TypeConverter
import com.kodeelite.nooreislam.feature.quran.data.HighlightColor

// Room column converters. HighlightColor persisted by enum name.
class Converters {
    @TypeConverter
    fun colorToName(c: HighlightColor): String = c.name
    @TypeConverter
    fun nameToColor(n: String): HighlightColor = HighlightColor.valueOf(n)
}
