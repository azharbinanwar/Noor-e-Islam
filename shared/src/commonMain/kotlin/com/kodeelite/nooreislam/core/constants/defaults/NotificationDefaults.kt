package com.kodeelite.nooreislam.core.constants.defaults

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
        const val hour = 20                  // default reminder hour (24h)
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

    /** Scheduler knobs (engine, not user settings). Change to test. */
    object Scheduler {
        const val budget = 55           // slots to book; iOS caps at 64, leave 1 buffer
        const val horizonDays = 30      // how far ahead to expand events
        const val dailyRebuildHour = 0
        const val dailyRebuildMinute = 5
    }
}
