package com.kodeelite.nooreislam.core.constants.defaults

import com.kodeelite.nooreislam.feature.quran.data.HighlightColor
import com.kodeelite.nooreislam.feature.quran.data.QuranFont
import com.kodeelite.nooreislam.feature.quran.data.QuranTheme

/**
 * Ship defaults for the Quran reader — every value a new user gets before touching anything.
 * Single home for the reader defaults (theme, font, highlight color, sizes). Read by QuranStore
 * as the fallback when PrefsService has no saved value, and by the reader UI for the size range.
 */
object QuranDefaults {
    val THEME = QuranTheme.System            // follows the app theme
    val FONT = QuranFont.Hafs
    val HIGHLIGHT_COLOR = HighlightColor.Green

    const val FONT_SP = 15        // default reading size
    const val MIN_FONT_SP = 12    // stepper floor
    const val MAX_FONT_SP = 40    // stepper ceiling

    // % of the base line height (fontSize * BASE_LINE_HEIGHT_RATIO) — a ratio, not an absolute size, so
    // it scales correctly no matter which font is active; switching fonts never needs a reset.
    const val BASE_LINE_HEIGHT_RATIO = 1.9f
    const val LINE_SPACING_PERCENT = 100  // 100% = the base ratio as-is
    const val MIN_LINE_SPACING_PERCENT = 100
    const val MAX_LINE_SPACING_PERCENT = 200
    const val LINE_SPACING_STEP_PERCENT = 10

    const val COLLECTION_SEARCH_THRESHOLD = 12  // collection picker shows a search field past this count

    const val RECENT_JUMPS_LIMIT = 10  // Jump To's "Recent" chips, most recent first
    val COMMON_SURAHS = listOf(18, 36, 55, 67)  // Al-Kahf, Ya-Sin, Ar-Rahman, Al-Mulk — Jump To's shortcut list
    const val JUMP_TO_DEFAULT_SURAH = 1  // Jump To's pick before the user has ever picked one — Al-Fatihah

    const val QURAN_SEARCH_RESULT_LIMIT = 50  // QuranSearchSheet caps matches shown

    const val KEEP_SCREEN_ON = true  // default on — the whole point is not fighting the lock timer mid-ayah

    const val READING_DWELL_MS = 10_000L  // minimum time on the reader before a visit counts as "reading" for Resume, not a glance

    const val AUTO_SCROLL_SPEED = 4       // default level — matches the old fixed 1x pace
    const val MIN_AUTO_SCROLL_SPEED = 1
    const val MAX_AUTO_SCROLL_SPEED = 10
    const val AUTO_SCROLL_PX_PER_TICK = 0.6f  // pixels per tick at a 1x multiplier (~37.5px/sec @ 60fps)
    const val AUTO_SCROLL_TICK_MS = 16L       // ~60fps

    // step -> multiplier of AUTO_SCROLL_PX_PER_TICK. 1-3 are slower than the old fixed 1x (step 4) for
    // comfortable reading; 4-8 match the old 1x-5x range; 9-10 go beyond it for fast skimming.
    val AUTO_SCROLL_STEP_MULTIPLIERS = listOf(0.25f, 0.5f, 0.75f, 1f, 2f, 3f, 4f, 5f, 6f, 7f)
}
