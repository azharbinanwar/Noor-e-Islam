package com.kodeelite.nooreislam.feature.quran.data

/**
 * What the app strips from the raw db text, applied once by QuranRepository at read time.
 * The db stays a faithful copy of the source; flip a flag to get the raw form back.
 */
object AyahTextRules {

    // ayah number glyph (PUA) + its circle; the reader draws its own badge. Waqf marks stay.
    val STRIP_INDOPAK_AYAH_MARKER = true

    // trailing ruku sign; endsRuku + RukuBlock own rukus for both scripts
    val STRIP_INDOPAK_RUKU_MARKER = true

    // embedded basmalah on ayah 1 outside Al-Fatihah; the reader draws it as a header
    val STRIP_EMBEDDED_BASMALAH = true


    private const val BASMALAH_WORDS = 4
    private const val AYAH_END_CIRCLE = '۟'
    private const val RUKU_SIGN = '۠'
    private val PRIVATE_USE = '\uE000'..'\uF8FF'

    fun cleanTanzil(text: String, surah: Int, ayah: Int): String {
        var t = text.trim()
        if (STRIP_EMBEDDED_BASMALAH && ayah == 1 && surah != 1) {
            val words = t.split(' ').filter { it.isNotBlank() }
            // compared bare of marks: after a tanween-ending surah Tanzil doubles the ب (بِّسْمِ),
            // which is how 95 and 97 kept their basmalah for so long
            if (words.size > BASMALAH_WORDS && bare(words.first()) == "بسم") {
                t = words.drop(BASMALAH_WORDS).joinToString(" ")
            }
        }
        return t
    }

    private fun bare(word: String) = word.filter { it.code !in 0x064B..0x065F && it.code != 0x0651 && it.code != 0x0670 }

    fun cleanIndopak(text: String): String {
        var t = text
        if (STRIP_INDOPAK_AYAH_MARKER) t = t.filterNot { it == AYAH_END_CIRCLE || it in PRIVATE_USE }
        if (STRIP_INDOPAK_RUKU_MARKER) t = t.trimEnd().removeSuffix(RUKU_SIGN.toString())
        return t.trim()
    }

    /**
     * A waqf sign standing alone between words — the font ships it as space+mark and positions it
     * on that space. Android's Minikin splits runs at spaces before shaping and orphans that seat,
     * so the reader swaps each match for an inline box drawing the font's own space+sign form
     * itself. The match swallows both surrounding spaces (the box provides the room); group 1 is the sign.
     * The stored text is never rewritten: it stays exactly as the font's makers ship it.
     */
    val STANDALONE_WAQF = Regex(" ([\u0610-\u061A\u06D6-\u06ED]+)(?: |$)")
}
