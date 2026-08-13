package com.kodeelite.nooreislam.feature.notifications.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import com.kodeelite.nooreislam.core.locale.trValue
import com.kodeelite.nooreislam.feature.quran.data.QuranRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek

/**
 * One reading reminder: a surah, optionally an ayah to start at, and an alarm-clock schedule.
 * [ayah] is a starting point, not a range — the reader lands there and the user carries on.
 * [days] is a bitmask, bit 0 = Monday (DayOfWeek.ordinal), so a weekly and a daily reminder are one shape.
 */
@Entity(tableName = "surah_reminder")
data class SurahReminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val surah: Int,
    // Null = the whole surah, opened from the start. Only set when the reader should land mid-surah.
    val ayah: Int? = null,
    // The user's own words. Blank on most seeds, which fall back to the surah name.
    val title: String = "",
    val days: Int,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean = true,
    // Shipped with the app. Fully editable — retime it, rename it, point it elsewhere — but not
    // deletable, so the well-known reminders can always be found again after being switched off.
    val isSeed: Boolean = false,
    val createdAt: Long,
)

/** A surah's name in the current language — the fallback when a reminder has no title. */
suspend fun surahName(number: Int): String {
    val s = QuranRepository.surahs().firstOrNull { it.number == number } ?: return number.toString()
    return trValue(s.nameTransliterated, s.nameArabic)
}

fun SurahReminder.firesOn(day: DayOfWeek) = days and (1 shl day.ordinal) != 0

fun Set<DayOfWeek>.toDayMask() = fold(0) { acc, d -> acc or (1 shl d.ordinal) }

fun Int.toDaySet(): Set<DayOfWeek> = DayOfWeek.entries.filterTo(mutableSetOf()) { this and (1 shl it.ordinal) != 0 }

const val EVERY_DAY = 0b1111111

@Dao
interface SurahReminderDao {
    // Insertion order, not clock order: the seeds are listed most-asked-for first, and anything the
    // user adds lands at the bottom where they left it.
    @Query("SELECT * FROM surah_reminder ORDER BY id")
    fun allFlow(): Flow<List<SurahReminder>>

    @Query("SELECT COUNT(*) FROM surah_reminder")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(row: SurahReminder)

    @Delete
    suspend fun delete(row: SurahReminder)
}
