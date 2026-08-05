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

    const val QURAN_SEARCH_RESULT_LIMIT = 50  // QuranSearchSheet caps matches shown
}
