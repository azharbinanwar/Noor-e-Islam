package com.kodeelite.nooreislam.core.util

import androidx.compose.runtime.Composable
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.surah_meanings
import org.jetbrains.compose.resources.stringArrayResource

// number → font-ligature keys (the surah/juz fonts render these as the ornate name glyph) + Arabic-Indic digits
fun Int.toSurahKey(): String = "surah" + toString().padStart(3, '0')   // 2 -> "surah002"
fun Int.toJuzKey(): String = "j" + toString().padStart(3, '0')       // 5 -> "j005"
fun Int.toArabicIndic(): String = toString().map { (0x0660 + (it - '0')).toChar() }.joinToString("") // 255 -> "٢٥٥"

// Arabic-Indic (٠-٩) and Extended Arabic-Indic/Urdu (۰-۹) digits -> ASCII, so numeric input is
// recognized regardless of which digit script the user typed in.
fun String.fromArabicIndicDigits(): String = map {
    when (it.code) {
        in 0x0660..0x0669 -> '0' + (it.code - 0x0660)
        in 0x06F0..0x06F9 -> '0' + (it.code - 0x06F0)
        else -> it
    }
}.joinToString("")

// localized surah-name meaning by number (1-based), from the per-locale surah_meanings array
@Composable
fun Int.toSurahMeaning(): String = stringArrayResource(Res.array.surah_meanings)[this - 1]
