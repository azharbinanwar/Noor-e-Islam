package com.kodeelite.nooreislam.core.util

/**
 * Latin-side matching for surah names, the [normalizeArabic] of transliteration. There is no agreed
 * English spelling of a surah name — Baqara/Baqarah/Bakara, Yaseen/Yasin, Rahmaan/Rahman — and the
 * db ships exactly one of them, so a plain contains fails on most of what people type.
 *
 * Matching is tiered, strongest first, and callers rank by tier: a typo can never outrank a real
 * match. Growing the system later (root match, translation match, …) is one [NameMatch] entry plus
 * its branch in [nameMatch] — call sites rank by ordinal and need no change.
 */
enum class NameMatch {
    EXACT,    // folded query equals a folded name form: "al-baqarah" == Al-Baqara
    PARTIAL,  // folded query inside a folded name form: "baqar", "rahman" in a-rahman
    FUZZY,    // 1-2 edits away, whole-key: "bakara", "yasin", "mulq"
}

/**
 * The fold: lowercase, letters only, doubled letters collapsed (aa/ee/oo and any stutter),
 * trailing h dropped — Baqarah, Faatiha and Rahmaan all land on one shape.
 */
fun String.normalizeLatin(): String {
    val out = StringBuilder(length)
    var prev = ' '
    for (c0 in this) {
        val c = c0.lowercaseChar()
        if (c !in 'a'..'z') continue
        if (c == prev) continue
        out.append(c)
        prev = c
    }
    if (out.length > 3 && out.last() == 'h') out.deleteAt(out.lastIndex)
    return out.toString()
}

// longest first, so Ash- strips before Al- gets a chance to half-strip it
private val ARTICLES = listOf("ash", "adh", "ath", "aal", "al", "ar", "an", "at", "ad", "as")

/** The folded string with and without a leading article — both spellings people actually type. */
fun latinKeys(raw: String): List<String> {
    val n = raw.normalizeLatin()
    if (n.length < 2) return if (n.isEmpty()) emptyList() else listOf(n)
    for (a in ARTICLES) {
        // only strip when something pronounceable is left, so An-Naas keeps "anas" -> "nas" findable
        if (n.startsWith(a) && n.length - a.length >= 3) return listOf(n, n.drop(a.length))
    }
    return listOf(n)
}

/** Strongest tier at which the query keys hit this name, or null. Fuzzy needs 4+ letters to engage. */
fun nameMatch(queryKeys: List<String>, name: String): NameMatch? {
    if (queryKeys.isEmpty()) return null
    val nameKeys = latinKeys(name)
    if (nameKeys.any { k -> queryKeys.any { it == k } }) return NameMatch.EXACT
    if (nameKeys.any { k -> queryKeys.any { k.contains(it) } }) return NameMatch.PARTIAL
    val long = queryKeys.filter { it.length >= 4 }
    if (long.isNotEmpty() &&
        nameKeys.any { k -> long.any { q -> editDistance(q, k) <= if (q.length <= 5) 1 else 2 } }
    ) return NameMatch.FUZZY
    return null
}

private fun editDistance(a: String, b: String): Int {
    if (kotlin.math.abs(a.length - b.length) > 2) return 3 // can't be within threshold, skip the table
    var prev = IntArray(b.length + 1) { it }
    var curr = IntArray(b.length + 1)
    for (i in 1..a.length) {
        curr[0] = i
        for (j in 1..b.length) {
            curr[j] = minOf(
                prev[j] + 1,
                curr[j - 1] + 1,
                prev[j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1,
            )
        }
        val t = prev; prev = curr; curr = t
    }
    return prev[b.length]
}
