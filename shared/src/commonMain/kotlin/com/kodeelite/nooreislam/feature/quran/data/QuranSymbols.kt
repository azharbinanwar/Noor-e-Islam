package com.kodeelite.nooreislam.feature.quran.data

/** Quran display glyphs, one place so they're never sprinkled as literals across the UI. */
object QuranSymbols {
    const val AYAH_OPEN = "﴾"  // ornate parenthesis around the ayah number
    const val AYAH_CLOSE = "﴿"
    const val RUKU = "ع"        // section (ruku) marker
    const val SAJDA = "۩"       // prostration marker
    const val BASMALAH = "﷽"    // ligature bismillah
    const val QURAN = "quran" // this will be used ot show a glyph under juz font
    const val BOOKMARK = "⚑"    // plain glyph, not emoji — inherits tint like any other char
    const val NOTE = "✎"

    // LTR OVERRIDE (not embedding/isolate): forces every enclosed char to STRONG left-to-right, so the
    // neutral brackets ( ) / ﴾ ﴿ can't reorder or mirror inside RTL Arabic. Override is Unicode 1.0, so it
    // works on every text engine (embeddings leak across runs -> "first ok, rest flipped"; isolates are
    // Unicode 6.3 and some Samsung/OneUI stacks ignore them). Balanced with POP so it never leaks out.
    val LTR_OVERRIDE = Char(0x202D).toString() // U+202D LEFT-TO-RIGHT OVERRIDE
    val POP_DIR = Char(0x202C).toString()      // U+202C POP DIRECTIONAL FORMATTING
    val WORD_JOINER = Char(0x2060).toString()  // U+2060 no line break here

    /** Pin [text] to left-to-right so brackets/numbers render the same in RTL on every device. */
    fun ltrLock(text: String) = LTR_OVERRIDE + text + POP_DIR

    // ornate ayah number as one unbreakable unit, e.g. ﴾١٢﴿ that never splits across a line
    fun ayahNumber(arabicIndic: String) =
        ltrLock(AYAH_OPEN + WORD_JOINER + arabicIndic + WORD_JOINER + AYAH_CLOSE)
}
