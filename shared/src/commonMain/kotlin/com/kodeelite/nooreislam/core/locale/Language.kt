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
import org.jetbrains.compose.resources.FontResource

/**
 * Supported UI languages. The [label] stays in its own script (not translated — you always see "English"
 * and "العربية"). Add a language here and the Settings picker reflects it. [code] is stored in prefs;
 * [direction] drives the app's LTR/RTL layout — no per-language checks at the call site.
 *
 * The typeface is declared per entry too: Poppins has no Arabic script, Plex Arabic no Latin polish,
 * so a language that needs its own face (Bengali, Thai) just names it here and nothing else changes.
 */
enum class Language(
    val label: String,
    val code: String,
    val direction: LayoutDirection,
    private val regular: FontResource,
    private val medium: FontResource,
    private val bold: FontResource,
) {
    English("English", "en", LayoutDirection.Ltr, Res.font.poppins_regular, Res.font.poppins_medium, Res.font.poppins_semibold),
    Arabic("العربية", "ar", LayoutDirection.Rtl, Res.font.plex_arabic_regular, Res.font.plex_arabic_medium, Res.font.plex_arabic_semibold),
    Urdu("اردو", "ur", LayoutDirection.Rtl, Res.font.plex_arabic_regular, Res.font.plex_arabic_medium, Res.font.plex_arabic_semibold),
    French("Français", "fr", LayoutDirection.Ltr, Res.font.poppins_regular, Res.font.poppins_medium, Res.font.poppins_semibold),
    ;

    /** SemiBold doubles as Bold — neither family ships a heavier cut. */
    val font: FontFamily
        @Composable get() = FontFamily(
            Font(regular, FontWeight.Normal),
            Font(medium, FontWeight.Medium),
            Font(bold, FontWeight.SemiBold),
            Font(bold, FontWeight.Bold),
        )

    companion object {
        fun fromCode(code: String?) = entries.firstOrNull { it.code == code } ?: English

        /** The language in effect right now (reactive — recomposes on switch). */
        val current: Language
            @Composable get() = SettingsStore.language.collectAsState().value
    }
}

/**
 * Pick an LTR vs RTL value for the current language — the KMP take on Flutter's `getTr(en, ar)`.
 * Type-agnostic: strings, icons, alignments, whatever differs by locale, so it doubles as a
 * direction switch (`tr(startIcon, endIcon)`).
 *
 * Keyed on [Language.direction], not on Arabic: Urdu is RTL too, and matching the language itself
 * would point every back chevron the wrong way for it.
 */
@Composable
fun <T> tr(en: T, ar: T): T = if (Language.current.direction == LayoutDirection.Rtl) ar else en

/** [tr] for code outside composition — notification copy is built by the scheduler, not a screen. */
fun <T> trValue(en: T, ar: T): T =
    if (SettingsStore.language.value.direction == LayoutDirection.Rtl) ar else en
