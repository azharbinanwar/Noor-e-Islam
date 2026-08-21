package com.kodeelite.nooreislam.core.enums

/**
 * What an alert is and when it fires. [pausedByExemption] is the rule in one place: a value cannot be
 * added without deciding whether an exemption drops it, which a `when` elsewhere would let you skip.
 * Stored as its name in the schedule mirror.
 */
enum class NotificationType(val pausedByExemption: Boolean) {
    PRAYER_AT_TIME(true),
    PRAYER_REMIND_BEFORE(true),
    PRAYER_JAMAAT(true),
    PRAYER_NAFIL(true),             // tahajjud, ishraq — voluntary, but still prayer
    SURAH_REMINDER(false),          // Mulk, Kahf, and the ones she sets herself
    DAILY_READING_REMINDER(false),
    DHIKR_REMINDER(false),          // morning and evening adhkar
    TEST(false),                    // dev-only slots; never a real alert to pause
}
