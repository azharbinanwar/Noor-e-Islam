package com.kodeelite.nooreislam.core.constants.defaults

import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.feature.notifications.data.EVERY_DAY
import com.kodeelite.nooreislam.feature.notifications.data.SurahReminder
import kotlinx.datetime.DayOfWeek

/**
 * Default values for every notification setting, one place to manage them. Sections match the
 * Notifications screen. The store reads these; only the numbers live here.
 */
object NotificationDefaults {

    /** Master switch for every reminder. */
    const val allAlerts = false              // off out of the box — user opts in

    /** Per-prayer alert. Same defaults for all five daily prayers. */
    object Prayer {
        const val enabled = false            // prayer alert off until the user turns it on
        const val remindBeforeOn = false     // off out of the box — user opts in
        const val remindBefore = 20          // minutes before the prayer to nudge
        const val remindBeforeMin = 5        // lowest the "remind before" stepper allows
        const val remindBeforeMax = 60       // highest the "remind before" stepper allows
        const val atTime = false             // off out of the box — user opts in
        const val jamaat = false             // second reminder for the congregation time
        const val jamaatAfter = 10           // minutes after the start for jamaat
        const val jamaatAfterMin = 5         // lowest jamaat offset
        const val jamaatAfterMax = 150        // highest jamaat offset
        const val step = 1                   // stepper jump per tap (minutes); long-press accelerates
    }

    /** Friday Jumu'ah. */
    object Jumuah {
        const val enabled = false            // Jumu'ah alert off by default
        const val remindBeforeOn = false     // off out of the box — user opts in
        const val remindBefore = 30          // minutes before Jumu'ah to nudge
        const val remindBeforeMin = 15       // lowest "remind before"
        const val remindBeforeMax = 120      // highest "remind before"
        const val jamaat = false             // off out of the box — user opts in
        const val jamaatAfter = 45           // minutes after Dhuhr start for Jumu'ah (~45min, Hanafi ~1h after)
        const val jamaatAfterMin = 5         // lowest jamaat offset
        const val jamaatAfterMax = 150       // highest jamaat offset (Dhuhr start + 2.5h headroom)
        const val step = 1                   // stepper jump per tap (minutes); long-press accelerates
    }

    /** Surah Al-Mulk, nightly after Isha. */
    object Mulk {
        const val enabled = false            // off out of the box — user opts in
        const val afterIsha = 30             // minutes after Isha to remind
        const val afterIshaMin = 5           // lowest offset
        const val afterIshaMax = 150         // highest offset
        const val step = 1                   // stepper jump per tap (minutes); long-press accelerates
    }

    /** Surah Al-Kahf, Friday at a chosen clock time. */
    object Kahf {
        const val enabled = false            // off out of the box — user opts in
        const val hour = 10                  // default reminder hour (24h)
        const val minute = 0                 // default reminder minute
    }

    /** Daily reading reminder, at a chosen clock time every day. */
    object DailyReading {
        const val enabled = false            // off out of the box — user opts in
        const val hour = 5                    // default reminder hour (24h) — morning, not evening
        const val minute = 0                 // default reminder minute
    }

    /** Morning and evening adhkar, offset after Fajr / Asr. */
    object Dhikr {
        const val morningEnabled = false     // off out of the box — user opts in
        const val afterFajr = 20             // minutes after Fajr for morning adhkar
        const val eveningEnabled = false     // off out of the box — user opts in
        const val afterAsr = 15              // minutes after Asr for evening adhkar
        const val offsetMin = 0              // lowest offset for either
        const val offsetMax = 60             // highest offset for either
        const val step = 1                   // stepper jump per tap (minutes); long-press accelerates
    }

    /** Nafil prayers. */
    object Nafil {
        const val tahajjud = false           // Tahajjud reminder off by default
        const val ishraq = false             // Ishraq reminder off by default
    }

    /**
     * The surah reminders the Quran app ships with, seeded once into an empty database and owned by
     * the user from then on. All off — nothing fires at someone who never asked. Order is the list
     * order on screen, most-asked-for first. Times are a sensible default for each habit.
     */
    object SurahReminders {
        private val FRIDAY = 1 shl DayOfWeek.FRIDAY.ordinal

        /** Empty for the main app — it has no screen for these, so its table stays clean. */
        fun seedsFor(edition: AppEdition): List<SurahReminder> =
            if (edition == AppEdition.QURAN) seeds else emptyList()

        // Most titles are blank on purpose: the screen falls back to the localized surah name, so
        // they read correctly in Arabic. Only the two that open mid-surah carry a name, since
        // "Al-Baqara" alone wouldn't say what the reading is.
        private val seeds = listOf(
            // todo: uncomment after testing
//            seed(67, EVERY_DAY, 22, 30),
//            seed(18, FRIDAY, 9, 0),
//            seed(2, EVERY_DAY, 23, 0, ayah = 285, title = "Last 2 ayah of Al-Baqara"),
//            seed(36, EVERY_DAY, 6, 15),
//            seed(56, EVERY_DAY, 19, 30),
//            seed(2, EVERY_DAY, 22, 0, ayah = 255, title = "Ayat al-Kursi"),
//            seed(55, FRIDAY, 10, 0),
//            seed(32, EVERY_DAY, 22, 45),
//            seed(112, EVERY_DAY, 7, 0),
//            seed(109, EVERY_DAY, 23, 15),
//            seed(73, EVERY_DAY, 4, 0),
//            seed(93, EVERY_DAY, 8, 30),
//            seed(94, EVERY_DAY, 8, 45),
//            seed(48, FRIDAY, 8, 0),
//            seed(2, EVERY_DAY, 7, 30),
//            seed(19, EVERY_DAY, 10, 30),
//            seed(12, EVERY_DAY, 20, 30),
            // todo : comment after testing for release
            // test run: the same 17, every day, one per hour on the hour from 01:00 to 17:00,
            // pre-enabled so a fresh install starts firing without touching 17 switches
            seed(67, EVERY_DAY, 1, 0).copy(enabled = true),
            seed(18, EVERY_DAY, 2, 0).copy(enabled = true),
            seed(2, EVERY_DAY, 3, 0, ayah = 285, title = "Last 2 ayah of Al-Baqara").copy(enabled = true),
            seed(36, EVERY_DAY, 4, 0).copy(enabled = true),
            seed(56, EVERY_DAY, 5, 0).copy(enabled = true),
            seed(2, EVERY_DAY, 6, 0, ayah = 255, title = "Ayat al-Kursi").copy(enabled = true),
            seed(55, EVERY_DAY, 7, 0).copy(enabled = true),
            seed(32, EVERY_DAY, 8, 0).copy(enabled = true),
            seed(112, EVERY_DAY, 9, 0).copy(enabled = true),
            seed(109, EVERY_DAY, 10, 0).copy(enabled = true),
            seed(73, EVERY_DAY, 11, 0).copy(enabled = true),
            seed(93, EVERY_DAY, 12, 0).copy(enabled = true),
            seed(94, EVERY_DAY, 13, 0).copy(enabled = true),
            seed(48, EVERY_DAY, 14, 0).copy(enabled = true),
            seed(2, EVERY_DAY, 15, 0).copy(enabled = true),
            seed(19, EVERY_DAY, 16, 0).copy(enabled = true),
            seed(12, EVERY_DAY, 17, 0).copy(enabled = true),
        )

        // id and createdAt are assigned on insert; enabled and isSeed are the same for every row.
        // title is set only where the surah name alone wouldn't say what the reading is
        private fun seed(surah: Int, days: Int, hour: Int, minute: Int, ayah: Int? = null, title: String = "") =
            SurahReminder(
                surah = surah,
                ayah = ayah,
                title = title,
                days = days,
                hour = hour,
                minute = minute,
                enabled = false,
                isSeed = true,
                createdAt = 0,
            )
    }

    /** Scheduler knobs (engine, not user settings). Change to test. */
    object Scheduler {
        const val budget = 55           // slots to book; iOS caps at 64, leave 1 buffer
        const val horizonDays = 30      // how far ahead to expand events
        const val dailyRebuildHour = 0
        const val dailyRebuildMinute = 5
    }
}
