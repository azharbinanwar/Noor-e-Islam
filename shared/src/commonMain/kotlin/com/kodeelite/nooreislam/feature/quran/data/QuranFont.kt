package com.kodeelite.nooreislam.feature.quran.data

import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.tanzil_hafs
import com.kodeelite.nooreislam.resources.tanzil_me_quran
import com.kodeelite.nooreislam.resources.tanzil_naskh
import com.kodeelite.nooreislam.resources.tanzil_saleem
import com.kodeelite.nooreislam.resources.tanzil_scheherazade
import org.jetbrains.compose.resources.FontResource

/**
 * Pickable mushaf fonts, the genuine Tanzil set. They all render the same clean-Unicode Tanzil text,
 * so switching is just a font swap, no per-font db. Saleem is IndoPak-styled so it reads slightly
 * different on Uthmani spelling; kept as an option.
 */
@kotlinx.serialization.Serializable
enum class QuranFont(val label: String, val sample: String, val res: FontResource) {
    Hafs("Uthmani", "بِسْمِ اللَّهِ", Res.font.tanzil_hafs),
    Naskh("Naskh", "بِسْمِ اللَّهِ", Res.font.tanzil_naskh),
    MeQuran("Madani", "بِسْمِ اللَّهِ", Res.font.tanzil_me_quran),
    Scheherazade("Scheherazade", "بِسْمِ اللَّهِ", Res.font.tanzil_scheherazade),
    Indopak("IndoPak", "بِسْمِ اللَّهِ", Res.font.tanzil_saleem);

    companion object {
        val DEFAULT = Hafs
    }
}
