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
 * Pickable mushaf fonts. [scripts] is what each one is drawn for: the Tanzil set draws the Tanzil
 * text, Nastaleeq is the IndoPak hand. A list because a font may serve more than one, not because
 * any does today.
 *
 * Saleem serves both scripts: it seats the Taj-style waqf marks and is the printed-mushaf look,
 * so it opens the IndoPak script, while staying pickable for the Tanzil texts it always drew.
 */
@kotlinx.serialization.Serializable
enum class QuranFont(
    val labelRes: StringResource,
    val sample: String,
    val res: FontResource,
    val scripts: List<QuranScript>,
) {
    Nastaleeq(Res.string.font_nastaleeq, "بِسْمِ اللّٰهِ", Res.font.indopak_nastaleeq, listOf(QuranScript.Indopak)),
    Hafs(Res.string.font_uthmani, "بِسْمِ اللَّهِ", Res.font.tanzil_hafs, listOf(QuranScript.Tanzil)),
    Naskh(Res.string.font_naskh, "بِسْمِ اللَّهِ", Res.font.tanzil_naskh, listOf(QuranScript.Tanzil)),
    MeQuran(Res.string.font_madani, "بِسْمِ اللَّهِ", Res.font.tanzil_me_quran, listOf(QuranScript.Tanzil)),
    Scheherazade(Res.string.font_scheherazade, "بِسْمِ اللَّهِ", Res.font.tanzil_scheherazade, listOf(QuranScript.Tanzil)),
    Saleem(Res.string.font_saleem, "بِسْمِ اللَّهِ", Res.font.tanzil_saleem, listOf(QuranScript.Indopak, QuranScript.Tanzil));

    val label: String @Composable get() = stringResource(labelRes)

    /** The spelling this font draws. For a face serving several scripts, the one it was made for. */
    val script: QuranScript get() = scripts.first()
}
