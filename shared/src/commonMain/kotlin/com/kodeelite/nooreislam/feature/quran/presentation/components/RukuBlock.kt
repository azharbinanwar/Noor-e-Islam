package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.kodeelite.nooreislam.feature.quran.data.Ayah

// one ruku: surah header + basmalah at a surah start, the flowing ayahs, a ع marker at the end.
// A juz that begins on the first ayah shows before the surah header; one that begins mid-ruku splits the passage.
@Composable
fun RukuBlock(
    ruku: List<Ayah>,
    rukuNumber: Int,
    nextRukuNumber: Int?,
    prevJuz: Int,
    selected: Ayah?,
    onSelect: (Ayah) -> Unit,
    onLongSelect: (Ayah) -> Unit,
    onNoteTap: (Ayah) -> Unit = {},
    flashTarget: Ayah? = null,
    targetAyah: Ayah? = null,
    // reports the target ayah's exact pixel offset from this ruku item's own top — window-space math
    // done relative to [rukuCoords] below, so no manual summing of JuzMarker/SurahHeader/Bismalah/
    // sibling-passage heights, and correct at any font size since it's all real measured layout
    onTargetLocated: (Float) -> Unit = {},
) {
    var rukuCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val locateInRuku: (Float) -> Unit = { targetWindowY ->
        rukuCoords?.let { onTargetLocated(targetWindowY - it.localToWindow(Offset.Zero).y) }
    }

    Column(Modifier.onGloballyPositioned { rukuCoords = it }) {
        val first = ruku.first()
        var prev = prevJuz
        // a juz that begins on this ruku's first ayah comes BEFORE the surah header (you enter the juz, then the surah)
        if (first.juz != prev) {
            JuzMarker(first.juz, modifier = Modifier.padding(vertical = 14.dp))
            prev = first.juz
        }
        if (first.ayah == 1) {
            SurahHeader(first.surah)
            // surah 1 include bismilah, 9 exclude as it's surah Tawbah
            if (first.surah != 1 && first.surah != 9) Bismalah()
        }
        // a juz that begins mid-ruku splits the passage exactly at its first ayah
        var runStart = 0
        for (k in 1 until ruku.size) {
            if (ruku[k].juz != prev) {
                AyahPassage(ruku.subList(runStart, k), selected, onSelect, onLongSelect, onNoteTap, flashTarget, targetAyah, locateInRuku)
                JuzMarker(ruku[k].juz, modifier = Modifier.padding(vertical = 14.dp))
                runStart = k
                prev = ruku[k].juz
            }
        }
        AyahPassage(ruku.subList(runStart, ruku.size), selected, onSelect, onLongSelect, onNoteTap, flashTarget, targetAyah, locateInRuku)
        RukuMarker(rukuNumber = rukuNumber, nextRukuNumber = nextRukuNumber)
    }
}
