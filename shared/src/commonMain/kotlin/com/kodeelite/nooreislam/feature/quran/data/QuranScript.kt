package com.kodeelite.nooreislam.feature.quran.data

import androidx.compose.runtime.Composable
import com.kodeelite.nooreislam.core.constants.PrefConst
import com.kodeelite.nooreislam.core.constants.defaults.QuranDefaults
import com.kodeelite.nooreislam.core.platform.deviceCountryCode
import com.kodeelite.nooreislam.core.prefs.PrefsService
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.font_hint_indopak
import com.kodeelite.nooreislam.resources.font_hint_uthmani
import com.kodeelite.nooreislam.resources.script_indopak
import com.kodeelite.nooreislam.resources.script_indopak_hint
import com.kodeelite.nooreislam.resources.script_uthmani
import com.kodeelite.nooreislam.resources.script_uthmani_hint
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The spellings the mushaf is written in. Not a font: a script is a different column of text, so
 * switching changes the letters on the page and not only their shape. IndoPak writes every vowel
 * out and uses the Farsi yeh — اللّٰهِ ، الرَّحِیْمِ where the other has اللَّهِ ، الرَّحِيمِ — which
 * is why a reader raised on one finds the other hard to follow.
 *
 * [column] is the `ayah` column holding it, the same name in quran.db and quran_search.db. The
 * Uthmani column is Tanzil's Simple Enhanced text; the label is what readers call the family.
 */
@kotlinx.serialization.Serializable
enum class QuranScript(
    val labelRes: StringResource,
    val hintRes: StringResource,
    val fontHintRes: StringResource,
    val sample: String,
    val column: String,
) {
    Indopak(
        Res.string.script_indopak,
        Res.string.script_indopak_hint,
        Res.string.font_hint_indopak,
        "بِسْمِ اللّٰهِ",
        "textIndopak",
    ),
    Uthmani(
        Res.string.script_uthmani,
        Res.string.script_uthmani_hint,
        Res.string.font_hint_uthmani,
        "بِسْمِ اللَّهِ",
        "text",
    );

    val label: String @Composable get() = stringResource(labelRes)

    /** One line saying whose mushaf this is, so nobody has to know what an alif is to choose. */
    val hint: String @Composable get() = stringResource(hintRes)

    /** Why the font row looks the way it does under this script — one choice, or five. */
    val fontHint: String @Composable get() = stringResource(fontHintRes)

    /** The fonts drawn for this script. Every script has at least one; only Uthmani has a choice. */
    val fonts: List<QuranFont> get() = QuranFont.entries.filter { it.script == this }

    companion object {
        /**
         * The reader's pick, or the spelling their region implies when they have never made one.
         * Read from here rather than passed around, so a query that arrives before the settings
         * store exists — a notification opening the studio, say — still gets the right column.
         */
        fun saved(): QuranScript =
            PrefsService.getStringOrNull(PrefConst.QURAN_SCRIPT)
                ?.let { runCatching { valueOf(it) }.getOrNull() }
                ?: if (deviceCountryCode() in QuranDefaults.INDOPAK_COUNTRIES) Indopak else QuranDefaults.SCRIPT
    }
}
