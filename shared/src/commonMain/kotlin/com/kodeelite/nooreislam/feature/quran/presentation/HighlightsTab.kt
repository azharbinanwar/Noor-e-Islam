package com.kodeelite.nooreislam.feature.quran.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kodeelite.nooreislam.core.components.TilePosition
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.feature.quran.data.HighlightsStore
import com.kodeelite.nooreislam.feature.quran.data.QuranRepository
import com.kodeelite.nooreislam.feature.quran.presentation.components.HighlightItem
import org.koin.compose.koinInject

// Highlighted ayahs — one HighlightItem per highlight (its color shown); tap to jump to the reader.
@Composable
fun HighlightsTab() {
    val nav = LocalAppNavigator.current
    val store = koinInject<HighlightsStore>()
    val highlights by store.highlights.collectAsState()
    // ayah text for each item (preview), fetched by surah:ayah so it matches the current script
    val texts = remember { mutableStateMapOf<String, String>() }
    LaunchedEffect(highlights) {
        highlights.forEach { h ->
            val key = "${h.surah}:${h.ayah}"
            if (key !in texts) QuranRepository.ayah(h.surah, h.ayah)?.let { texts[key] = it.text }
        }
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(highlights.size) { i ->
            val h = highlights[i]
            HighlightItem(
                h, texts["${h.surah}:${h.ayah}"] ?: "",
                TilePosition.at(i, highlights.size),
            ) { nav.navigate(AppRoute.QuranReader(h.surah, h.ayah)) }
        }
    }
}
