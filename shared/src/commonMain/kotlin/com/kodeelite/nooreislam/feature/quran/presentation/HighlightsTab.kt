package com.kodeelite.nooreislam.feature.quran.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Highlighter
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.feature.quran.data.Highlight
import com.kodeelite.nooreislam.feature.quran.data.HighlightsStore
import com.kodeelite.nooreislam.feature.quran.presentation.components.HighlightActionSheet
import com.kodeelite.nooreislam.feature.quran.presentation.components.HighlightItem
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.highlights_hint
import com.kodeelite.nooreislam.resources.no_highlights
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

// Highlighted ayahs — one HighlightItem per highlight (its color shown); tap to jump to the reader.
@Composable
fun HighlightsTab() {
    val nav = LocalAppNavigator.current
    val store = koinInject<HighlightsStore>()
    val highlights by store.highlights.collectAsState()
    var actionHighlight by remember { mutableStateOf<Highlight?>(null) }

    if (highlights.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            StateView(
                title = stringResource(Res.string.no_highlights),
                message = stringResource(Res.string.highlights_hint),
                icon = { Icon(Lucide.Highlighter, null, tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(40.dp)) }
            )
        }
    } else {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp), // Matched to standard AppTileGroup spacing
        ) {
            items(highlights.size) { i ->
                val h = highlights[i]
                HighlightItem(
                    h, i, highlights.size,
                    onLongClick = { actionHighlight = h }
                ) { nav.navigate(AppRoute.QuranReader(h.surah, h.ayah)) }
            }
        }
    }

    actionHighlight?.let { h ->
        HighlightActionSheet(
            highlight = h,
            store = store,
            onShareToStudio = { nav.navigate(AppRoute.Studio(h.surah, h.ayah)) },
            onDismiss = { actionHighlight = null }
        )
    }
}
