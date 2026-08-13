package com.kodeelite.nooreislam.core.util

import kotlin.test.Test
import kotlin.test.assertEquals

// names as quran.db actually spells them — the whole point is matching what users type against these
class LatinTextTest {

    private fun tier(q: String, name: String) = nameMatch(latinKeys(q), name)

    @Test
    fun foldCollapsesVariantSpellings() {
        assertEquals("albaqara", "Al-Baqarah".normalizeLatin())
        assertEquals("albaqara", "Al-Baqara".normalizeLatin())
        assertEquals("arahman", "Ar-Rahmaan".normalizeLatin())
        assertEquals("yasen", "Yaseen".normalizeLatin())
    }

    @Test
    fun exactAcrossSpellingAndArticle() {
        assertEquals(NameMatch.EXACT, tier("Al-Baqarah", "Al-Baqara"))
        assertEquals(NameMatch.EXACT, tier("baqara", "Al-Baqara"))
        assertEquals(NameMatch.EXACT, tier("al fatihah", "Al-Faatiha"))
        // equals the article-stripped form, so it ranks exact — stronger than the partial it looks like
        assertEquals(NameMatch.EXACT, tier("imran", "Aal-i-Imraan"))
    }

    @Test
    fun partialCoversPrefixAndInnerMatch() {
        assertEquals(NameMatch.PARTIAL, tier("baqar", "Al-Baqara"))
        assertEquals(NameMatch.PARTIAL, tier("rahman", "Ar-Rahmaan"))
        assertEquals(NameMatch.PARTIAL, tier("nas", "An-Naas"))
        assertEquals(NameMatch.PARTIAL, tier("cow", "The Cow"))
    }

    @Test
    fun fuzzyCatchesTyposButNeedsLength() {
        assertEquals(NameMatch.FUZZY, tier("bakara", "Al-Baqara"))
        assertEquals(NameMatch.FUZZY, tier("yasin", "Yaseen"))
        assertEquals(NameMatch.FUZZY, tier("mulq", "Al-Mulk"))
        assertEquals(null, tier("mul", "Al-Kahf")) // short junk never fuzzes
        assertEquals(null, tier("xyzzy", "Al-Baqara"))
    }
}
