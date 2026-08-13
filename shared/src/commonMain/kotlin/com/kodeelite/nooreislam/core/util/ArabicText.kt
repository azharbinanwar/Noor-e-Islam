package com.kodeelite.nooreislam.core.util

/**
 * Folds Arabic script to one canonical shape so a query matches whatever keyboard typed it. An Urdu
 * keyboard emits ہ ی ک where an Arabic one emits ه ي ك, and the Uthmani text carries full tashkeel
 * nobody types — same word, different bytes, no match.
 *
 * Both the stored text and the query must go through this, never one alone; the moment the two sides
 * disagree, search silently half-works.
 *
 * What gets matched is written as numeric codepoints on purpose — the variants are visually identical,
 * so pasting glyphs into the ranges is exactly how earlier attempts folded the wrong characters.
 */
fun String.normalizeArabic(): String = buildString(length) {
    for (c in this@normalizeArabic) when (c.code) {
        // dropped: small high marks, tashkeel, superscript alef, Quranic pause marks, extended marks,
        // tatweel, bare hamza, zero-width joiners (Urdu keyboards emit these constantly, invisibly)
        0x0621, 0x0640, 0xFEFF,
        in 0x0610..0x061A, in 0x064B..0x065F, 0x0670,
        in 0x06D6..0x06ED, in 0x08D3..0x08FF, in 0x200B..0x200D -> Unit

        // madda, hamza above/below, wasla -> plain alef
        0x0622, 0x0623, 0x0625, 0x0671, 0x0672, 0x0673, 0x0675 -> append('ا')
        // alef maksura, yeh with hamza, Farsi yeh, Urdu barree yeh -> yeh
        0x0626, 0x0649, 0x06CC, 0x06D0, 0x06D2, 0x06D3 -> append('ي')
        // teh marbuta, heh doachashmee, heh goal, heh goal with hamza -> heh
        0x0629, 0x06BE, 0x06C0, 0x06C1, 0x06C2 -> append('ه')
        // keheh, swash kaf -> kaf
        0x06A9, 0x06AA -> append('ك')
        // waw with hamza, Central Asian variants -> waw
        0x0624, 0x06C7, 0x06C8, 0x06CB -> append('و')

        // runs of space collapse to one — stripping marks and joiners above routinely leaves gaps
        else -> if (c.isWhitespace()) { if (lastOrNull() != ' ') append(' ') } else append(c)
    }
}.trim()
