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
}
