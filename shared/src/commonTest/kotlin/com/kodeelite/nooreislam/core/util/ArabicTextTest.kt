package com.kodeelite.nooreislam.core.util

import kotlin.test.Test
import kotlin.test.assertEquals

// \u escapes throughout: the variants are visually identical, so a literal glyph proves nothing here.
class ArabicTextTest {

    private val allah = "الله" // ا ل ل ه, the canonical shape everything folds to

    @Test
    fun urduAndQuranicSpellingsConverge() {
        // what an Urdu keyboard sends: heh goal instead of heh
        assertEquals(allah, "اللہ".normalizeArabic())
        // what quran.db stores: alef wasla, fatha, shadda, superscript alef
        assertEquals(allah, "ٱللَّٰه".normalizeArabic())
        // and an Arabic keyboard with no harakat at all
        assertEquals(allah, allah.normalizeArabic())
    }

    @Test
    fun foldsLetterFamilies() {
        assertEquals("ا", "آأإٱ".normalizeArabic().distinctChars()) // alef
        assertEquals("ي", "ئىیېےۓ".normalizeArabic().distinctChars()) // yeh
        assertEquals("ه", "ةھۀہۂ".normalizeArabic().distinctChars()) // heh
        assertEquals("ك", "کڪ".normalizeArabic().distinctChars()) // kaf
        assertEquals("و", "ؤۇۈۋ".normalizeArabic().distinctChars()) // waw
    }

    @Test
    fun stripsMarksJoinersAndTatweel() {
        // tatweel, zero-width non-joiner, bare hamza, a Quranic pause mark
        assertEquals(allah, "اـل‌لۖهء".normalizeArabic())
    }

    @Test
    fun collapsesWhitespaceAndTrims() {
        assertEquals("لا إله".normalizeArabic(), "  لا   إله  ".normalizeArabic())
    }

    @Test
    fun leavesLatinAlone() {
        // surah names are matched as transliteration through the same call, so this must pass through
        assertEquals("Al-Baqarah", "Al-Baqarah".normalizeArabic())
    }

    private fun String.distinctChars() = toSet().joinToString("")
}
