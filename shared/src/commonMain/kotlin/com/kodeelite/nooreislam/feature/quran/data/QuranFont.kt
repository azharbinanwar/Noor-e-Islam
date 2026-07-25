package com.kodeelite.nooreislam.feature.quran.data

import androidx.compose.runtime.Composable
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.font_indopak
import com.kodeelite.nooreislam.resources.font_madani
import com.kodeelite.nooreislam.resources.font_naskh
import com.kodeelite.nooreislam.resources.font_scheherazade
import com.kodeelite.nooreislam.resources.font_uthmani
import com.kodeelite.nooreislam.resources.tanzil_hafs
import com.kodeelite.nooreislam.resources.tanzil_me_quran
import com.kodeelite.nooreislam.resources.tanzil_naskh
import com.kodeelite.nooreislam.resources.tanzil_saleem
import com.kodeelite.nooreislam.resources.tanzil_scheherazade
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Pickable mushaf fonts, the genuine Tanzil set. They all render the same clean-Unicode Tanzil text,
 * so switching is just a font swap, no per-font db. Saleem is IndoPak-styled so it reads slightly
 * different on Uthmani spelling; kept as an option.
 */
@kotlinx.serialization.Serializable
enum class QuranFont(val labelRes: StringResource, val sample: String, val res: FontResource) {
    Hafs(Res.string.font_uthmani, "بِسْمِ اللَّهِ", Res.font.tanzil_hafs),
    Naskh(Res.string.font_naskh, "بِسْمِ اللَّهِ", Res.font.tanzil_naskh),
    MeQuran(Res.string.font_madani, "بِسْمِ اللَّهِ", Res.font.tanzil_me_quran),
    Scheherazade(Res.string.font_scheherazade, "بِسْمِ اللَّهِ", Res.font.tanzil_scheherazade),
    Indopak(Res.string.font_indopak, "بِسْمِ اللَّهِ", Res.font.tanzil_saleem);

    val label: String @Composable get() = stringResource(labelRes)

    companion object {
        val DEFAULT = Hafs
    }
}
