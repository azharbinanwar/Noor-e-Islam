package com.kodeelite.nooreislam.feature.quran.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.core.components.TilePosition
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.feature.quran.data.Bookmark
import com.kodeelite.nooreislam.feature.quran.data.BookmarksStore
import com.kodeelite.nooreislam.feature.quran.data.QuranRepository
import com.kodeelite.nooreislam.feature.quran.presentation.components.BookmarkActionSheet
import com.kodeelite.nooreislam.feature.quran.presentation.components.BookmarkItem
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.bookmarks_hint
import com.kodeelite.nooreislam.resources.no_bookmarks
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun BookmarksTab() {
    val nav = LocalAppNavigator.current
    val store = koinInject<BookmarksStore>()
    val bookmarks by store.bookmarks.collectAsState()
    var actionBookmark by remember { mutableStateOf<Bookmark?>(null) }

    val texts = remember { mutableStateMapOf<String, String>() }
    LaunchedEffect(bookmarks) {
        bookmarks.forEach { b ->
            val key = "${b.surah}:${b.ayah}"
            if (key !in texts) QuranRepository.ayah(b.surah, b.ayah)?.let { texts[key] = it.text }
        }
    }

    if (bookmarks.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            StateView(
                title = stringResource(Res.string.no_bookmarks),
                message = stringResource(Res.string.bookmarks_hint),
                icon = { Icon(Lucide.Bookmark, null, tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(40.dp)) }
            )
        }
    } else {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(bookmarks.size) { i ->
                val b = bookmarks[i]
                BookmarkItem(
                    b.surah, b.ayah, texts["${b.surah}:${b.ayah}"] ?: "",
                    TilePosition.at(i, bookmarks.size),
                ) { nav.navigate(AppRoute.QuranReader(b.surah, b.ayah)) }
            }
        }
    }

    actionBookmark?.let { b ->
        BookmarkActionSheet(
            bookmark = b,
            store = store,
            onShareToStudio = { nav.navigate(AppRoute.Studio(b.surah, b.ayah)) },
            onDismiss = { actionBookmark = null }
        )
    }
}
