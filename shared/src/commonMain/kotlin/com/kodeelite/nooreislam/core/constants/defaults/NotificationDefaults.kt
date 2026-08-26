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
    const val ALL_ALERTS = false              // off out of the box — user opts in

    /** Per-prayer alert. Same defaults for all five daily prayers. */
    object Prayer {
        const val ENABLED = false            // prayer alert off until the user turns it on
        const val REMIND_BEFORE_ON = false     // off out of the box — user opts in
        const val REMIND_BEFORE = 20          // minutes before the prayer to nudge
        const val REMIND_BEFORE_MIN = 5        // lowest the "remind before" stepper allows
        const val REMIND_BEFORE_MAX = 60       // highest the "remind before" stepper allows
        const val AT_TIME = false             // off out of the box — user opts in
        const val JAMAAT = false             // second reminder for the congregation time
        const val JAMAAT_AFTER = 10           // minutes after the start for JAMAAT
        const val JAMAAT_AFTER_MIN = 5         // lowest JAMAAT offset
        const val JAMAAT_AFTER_MAX = 150        // highest JAMAAT offset
        const val STEP = 1                   // stepper jump per tap (minutes); long-press accelerates
    }

    /** Friday Jumu'ah. */
    object Jumuah {
        const val ENABLED = false            // Jumu'ah alert off by default
        const val REMIND_BEFORE_ON = false     // off out of the box — user opts in
        const val REMIND_BEFORE = 30          // minutes before Jumu'ah to nudge
        const val REMIND_BEFORE_MIN = 15       // lowest "remind before"
        const val REMIND_BEFORE_MAX = 120      // highest "remind before"
        const val JAMAAT = false             // off out of the box — user opts in
        const val JAMAAT_AFTER = 45           // minutes after Dhuhr start for Jumu'ah (~45min, Hanafi ~1h after)
        const val JAMAAT_AFTER_MIN = 5         // lowest JAMAAT offset
        const val JAMAAT_AFTER_MAX = 150       // highest JAMAAT offset (Dhuhr start + 2.5h headroom)
        const val STEP = 1                   // stepper jump per tap (minutes); long-press accelerates
    }

    /** Surah Al-Mulk, nightly after Isha. */
    object Mulk {
        const val ENABLED = false            // off out of the box — user opts in
        const val AFTER_ISHA = 30             // minutes after Isha to remind
        const val AFTER_ISHA_MIN = 5           // lowest offset
        const val AFTER_ISHA_MAX = 150         // highest offset
        const val STEP = 1                   // stepper jump per tap (minutes); long-press accelerates
    }

    /** Surah Al-Kahf, Friday at a chosen clock time. */
    object Kahf {
        const val ENABLED = false            // off out of the box — user opts in
        const val HOUR = 10                  // default reminder HOUR (24h)
        const val MINUTE = 0                 // default reminder MINUTE
    }

    /** Daily reading reminder, at a chosen clock time every day. */
    object DailyReading {
        const val ENABLED = false            // off out of the box — user opts in
        const val HOUR = 5                    // default reminder HOUR (24h) — morning, not evening
        const val MINUTE = 0                 // default reminder MINUTE
    }

    /** Morning and evening adhkar, offset after Fajr / Asr. */
    object Dhikr {
        const val MORNING_ENABLED = false     // off out of the box — user opts in
        // Fajr's start, not its JAMAAT: with JAMAAT often 45 minutes in, a smaller offset lands mid-prayer
        const val AFTER_FAJR = 40             // minutes after Fajr for morning adhkar
        const val EVENING_ENABLED = false     // off out of the box — user opts in
        const val AFTER_ASR = 15              // minutes after Asr for evening adhkar
        const val OFFSET_MIN = 0              // lowest offset for either
        const val MORNING_OFFSET_MAX = 80      // far enough past a late JAMAAT to still be morning
        const val OFFSET_MAX = 60             // highest offset for evening
        const val STEP = 1                   // stepper jump per tap (minutes); long-press accelerates
    }

    /** Nafil prayers. */
    object Nafil {
        const val TAHAJJUD = false           // Tahajjud reminder off by default
        const val ISHRAQ = false             // Ishraq reminder off by default
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
            // the shipped list: all off, the user turns on what they want
            seed(67, EVERY_DAY, 22, 30),
            seed(18, FRIDAY, 9, 0),
            seed(2, EVERY_DAY, 23, 0, ayah = 285, title = "Last 2 ayah of Al-Baqara"),
            seed(36, EVERY_DAY, 6, 15),
            seed(56, EVERY_DAY, 19, 30),
            seed(2, EVERY_DAY, 22, 0, ayah = 255, title = "Ayat al-Kursi"),
            seed(55, FRIDAY, 10, 0),
            seed(32, EVERY_DAY, 22, 45),
            seed(112, EVERY_DAY, 7, 0),
            seed(109, EVERY_DAY, 23, 15),
            seed(73, EVERY_DAY, 4, 0),
            seed(93, EVERY_DAY, 8, 30),
            seed(94, EVERY_DAY, 8, 45),
            seed(48, FRIDAY, 8, 0),
            seed(2, EVERY_DAY, 7, 30),
            seed(19, EVERY_DAY, 10, 30),
            seed(12, EVERY_DAY, 20, 30),

            // test rig, kept for the next round: 24 reminders, one per HOUR, pre-ENABLED so a
            // fresh install starts firing without touching the switches. Swap with the block above.
//            seed(1, EVERY_DAY, 0, 0).copy(enabled = true),
//            seed(67, EVERY_DAY, 1, 0).copy(enabled = true),
//            seed(18, EVERY_DAY, 2, 0).copy(enabled = true),
//            seed(2, EVERY_DAY, 3, 0, ayah = 285, title = "Last 2 ayah of Al-Baqara").copy(enabled = true),
//            seed(36, EVERY_DAY, 4, 0).copy(enabled = true),
//            seed(56, EVERY_DAY, 5, 0).copy(enabled = true),
//            seed(2, EVERY_DAY, 6, 0, ayah = 255, title = "Ayat al-Kursi").copy(enabled = true),
//            seed(55, EVERY_DAY, 7, 0).copy(enabled = true),
//            seed(32, EVERY_DAY, 8, 0).copy(enabled = true),
//            seed(112, EVERY_DAY, 9, 0).copy(enabled = true),
//            seed(109, EVERY_DAY, 10, 0).copy(enabled = true),
//            seed(73, EVERY_DAY, 11, 0).copy(enabled = true),
//            seed(93, EVERY_DAY, 12, 0).copy(enabled = true),
//            seed(94, EVERY_DAY, 13, 0).copy(enabled = true),
//            seed(48, EVERY_DAY, 14, 0).copy(enabled = true),
//            seed(2, EVERY_DAY, 15, 0).copy(enabled = true),
//            seed(19, EVERY_DAY, 16, 0).copy(enabled = true),
//            seed(12, EVERY_DAY, 17, 0).copy(enabled = true),
//            seed(113, EVERY_DAY, 18, 0).copy(enabled = true),
//            seed(114, EVERY_DAY, 19, 0).copy(enabled = true),
//            seed(103, EVERY_DAY, 20, 0).copy(enabled = true),
//            seed(108, EVERY_DAY, 21, 0).copy(enabled = true),
//            seed(97, EVERY_DAY, 22, 0).copy(enabled = true),
//            seed(95, EVERY_DAY, 23, 0).copy(enabled = true),
        )

        // id and createdAt are assigned on insert; ENABLED and isSeed are the same for every row.
        // title is set only where the surah name alone wouldn't say what the reading is
        private fun seed(surah: Int, days: Int, HOUR: Int, MINUTE: Int, ayah: Int? = null, title: String = "") =
            SurahReminder(
                surah = surah,
                ayah = ayah,
                title = title,
                days = days,
                hour = HOUR,
                minute = MINUTE,
                enabled = false,
                isSeed = true,
                createdAt = 0,
            )
    }

    /** Scheduler knobs (engine, not user settings). Change to test. */
    object Scheduler {
        const val BUDGET = 55           // slots to book; iOS caps at 64, leave 1 buffer
        const val HORIZON_DAYS = 30      // how far ahead to expand events
        const val DAILY_REBUILD_HOUR = 0
        const val DAILY_REBUILD_MINUTE = 5
    }
}
