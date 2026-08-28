package com.kodeelite.nooreislam.feature.quran.data

import kotlinx.serialization.Serializable

data class WordRef(val surah: Int, val ayah: Int, val word: Int) {
    fun toKey() = "$surah:$ayah:$word"

    companion object {
        fun fromKey(k: String) = k.split(":").let { WordRef(it[0].toInt(), it[1].toInt(), it[2].toInt()) }
    }
}

// one `ayah` row. Both spellings ride along: one read at startup, and switching script is then a
// choice at render time rather than another query — which is also why nothing has to reload.
@Serializable
data class Ayah(
    val id: Int,
    val surah: Int,
    val ayah: Int,
    val textTanzil: String,
    val textIndopak: String,
    val juz: Int,
    val endsRuku: Boolean,
    val sajda: Sajda?,
) {
    /** The verse in one spelling. Whoever draws it says which; the model never decides. */
    fun textIn(script: QuranScript): String = if (script == QuranScript.Indopak) textIndopak else textTanzil
}

// a juz = its number, the ayah it starts at, and the surahs it spans (so the UI never re-derives them)
data class Juz(val number: Int, val startsAt: Ayah, val surahs: List<Surah>)

// one `surah` row
data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameTransliterated: String,
    val nameEnglish: String,
    val ayahCount: Int,
    val rukuCount: Int,
    val revelation: Revelation,
    val revelationOrder: Int,
    val startId: Int,
)
