package com.kodeelite.nooreislam.core.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.poppins_medium
import com.kodeelite.nooreislam.resources.poppins_regular
import com.kodeelite.nooreislam.resources.poppins_semibold
import com.kodeelite.nooreislam.resources.plex_arabic_medium
import com.kodeelite.nooreislam.resources.plex_arabic_regular
import com.kodeelite.nooreislam.resources.plex_arabic_semibold
import org.jetbrains.compose.resources.Font

/**
 * Supported UI languages. The [label] stays in its own script (not translated — you always see "English"
 * and "العربية"). Add a language here and the Settings picker reflects it. [code] is stored in prefs;
 * [direction] drives the app's LTR/RTL layout — no per-language checks at the call site.
 */
enum class Language(val label: String, val code: String, val direction: LayoutDirection) {
    English("English", "en", LayoutDirection.Ltr),
    Arabic("العربية", "ar", LayoutDirection.Rtl),
    // later: Urdu("اردو", "ur", LayoutDirection.Rtl), …
    ;

    /**
     * The UI face for this language — Poppins has no Arabic, so Arabic uses IBM Plex Sans Arabic —
     * each script gets a family drawn for it. A new language brings its own here and
     * nothing else changes; fall back to Plex Arabic, which covers both scripts.
     */
    val font: FontFamily
        @Composable get() = when (this) {
            English -> FontFamily(
                Font(Res.font.poppins_regular, FontWeight.Normal),
                Font(Res.font.poppins_medium, FontWeight.Medium),
                Font(Res.font.poppins_semibold, FontWeight.SemiBold),
                Font(Res.font.poppins_semibold, FontWeight.Bold),
            )

            else -> FontFamily(
                Font(Res.font.plex_arabic_regular, FontWeight.Normal),
                Font(Res.font.plex_arabic_medium, FontWeight.Medium),
                Font(Res.font.plex_arabic_semibold, FontWeight.SemiBold),
                Font(Res.font.plex_arabic_semibold, FontWeight.Bold),
            )
        }

    companion object {
        fun fromCode(code: String?) = entries.firstOrNull { it.code == code } ?: English

        /** The language in effect right now (reactive — recomposes on switch). */
        val current: Language
            @Composable get() = SettingsStore.language.collectAsState().value
    }
}

/**
 * Pick an English vs Arabic value for the current language — the KMP take on Flutter's `getTr(en, ar)`.
 * Type-agnostic: strings, icons, alignments, whatever differs by locale. Since Arabic is the only RTL
 * language, this doubles as an LTR/RTL switch (`tr(startIcon, endIcon)`).
 */
@Composable
fun <T> tr(en: T, ar: T): T = if (Language.current == Language.Arabic) ar else en

/** [tr] for code outside composition — notification copy is built by the scheduler, not a screen. */
fun <T> trValue(en: T, ar: T): T = if (SettingsStore.language.value == Language.Arabic) ar else en
