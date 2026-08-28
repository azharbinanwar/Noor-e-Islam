package com.kodeelite.nooreislam.feature.quran.data

import androidx.compose.runtime.Composable
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.font_madani
import com.kodeelite.nooreislam.resources.font_naskh
import com.kodeelite.nooreislam.resources.font_nastaleeq
import com.kodeelite.nooreislam.resources.font_saleem
import com.kodeelite.nooreislam.resources.font_scheherazade
import com.kodeelite.nooreislam.resources.font_uthmani
import com.kodeelite.nooreislam.resources.indopak_nastaleeq
import com.kodeelite.nooreislam.resources.tanzil_hafs
import com.kodeelite.nooreislam.resources.tanzil_me_quran
import com.kodeelite.nooreislam.resources.tanzil_naskh
import com.kodeelite.nooreislam.resources.tanzil_saleem
import com.kodeelite.nooreislam.resources.tanzil_scheherazade
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Pickable mushaf fonts. A font belongs to one [QuranScript] and cannot draw the other: the Tanzil
 * set has no glyphs for IndoPak's written-out vowels, and Nastaleeq has none for Uthmani's marks.
 * Within a script it is a pure swap — same text, same db column, different shapes.
 *
 * Saleem is IndoPak-styled but sits under Uthmani on purpose: it draws the Uthmani text in a
 * subcontinental hand, which is a look, not a script.
 */
@kotlinx.serialization.Serializable
enum class QuranFont(
    val labelRes: StringResource,
    val sample: String,
    val res: FontResource,
    val script: QuranScript,
) {
    Nastaleeq(Res.string.font_nastaleeq, "بِسْمِ اللّٰهِ", Res.font.indopak_nastaleeq, QuranScript.Indopak),
    Hafs(Res.string.font_uthmani, "بِسْمِ اللَّهِ", Res.font.tanzil_hafs, QuranScript.Uthmani),
    Naskh(Res.string.font_naskh, "بِسْمِ اللَّهِ", Res.font.tanzil_naskh, QuranScript.Uthmani),
    MeQuran(Res.string.font_madani, "بِسْمِ اللَّهِ", Res.font.tanzil_me_quran, QuranScript.Uthmani),
    Scheherazade(Res.string.font_scheherazade, "بِسْمِ اللَّهِ", Res.font.tanzil_scheherazade, QuranScript.Uthmani),
    Saleem(Res.string.font_saleem, "بِسْمِ اللَّهِ", Res.font.tanzil_saleem, QuranScript.Uthmani);

    val label: String @Composable get() = stringResource(labelRes)
}
