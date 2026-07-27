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
import com.kodeelite.nooreislam.feature.quran.data.BookmarksStore
import com.kodeelite.nooreislam.feature.quran.data.QuranRepository
import com.kodeelite.nooreislam.feature.quran.presentation.components.BookmarkItem
import org.koin.compose.koinInject

// Saved ayahs — one BookmarkItem per bookmark; tap to jump to the reader.
@Composable
fun BookmarksTab() {
    val nav = LocalAppNavigator.current
    val store = koinInject<BookmarksStore>()
    val bookmarks by store.bookmarks.collectAsState()
    // ayah text for each item (preview), fetched by surah:ayah so it matches the current script
    val texts = remember { mutableStateMapOf<String, String>() }
    LaunchedEffect(bookmarks) {
        bookmarks.forEach { b ->
            val key = "${b.surah}:${b.ayah}"
            if (key !in texts) QuranRepository.ayah(b.surah, b.ayah)?.let { texts[key] = it.text }
        }
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(bookmarks.size) { i ->
            val b = bookmarks[i]
            BookmarkItem(
                b, texts["${b.surah}:${b.ayah}"] ?: "",
                TilePosition.at(i, bookmarks.size),
            ) { nav.navigate(AppRoute.QuranReader(b.surah, b.ayah)) }
        }
    }
}
