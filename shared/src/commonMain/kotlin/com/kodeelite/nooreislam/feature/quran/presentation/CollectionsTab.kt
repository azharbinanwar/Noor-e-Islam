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
import com.composables.icons.lucide.Folder
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.feature.quran.data.CollectionAyah
import com.kodeelite.nooreislam.feature.quran.data.CollectionStore
import com.kodeelite.nooreislam.feature.quran.presentation.components.CollectionAyahActionSheet
import com.kodeelite.nooreislam.feature.quran.presentation.components.CollectionItem
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.collections_hint
import com.kodeelite.nooreislam.resources.loading_collections
import com.kodeelite.nooreislam.resources.no_collections
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

// Saved collections — each row expands to a quick ayah list (like JuzsTab); tap opens the details screen.
@Composable
fun CollectionsTab() {
    val nav = LocalAppNavigator.current
    val store = koinInject<CollectionStore>()
    val collectionsState by store.collections.collectAsState()
    var actionAyah by remember { mutableStateOf<CollectionAyah?>(null) }

    if (collectionsState == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            StateView.Loading(title = stringResource(Res.string.loading_collections))
        }
    } else if (collectionsState!!.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            StateView(
                title = stringResource(Res.string.no_collections),
                message = stringResource(Res.string.collections_hint),
                icon = { Icon(Lucide.Folder, null, tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(40.dp)) }
            )
        }
    } else {
        val collections = collectionsState!!
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(collections.size, key = { collections[it].id }) { i ->
                val c = collections[i]
                CollectionItem(
                    collection = c,
                    store = store,
                    onOpen = { nav.navigate(AppRoute.CollectionDetails(c.id)) },
                    onOpenAyah = { surah, ayah -> nav.navigate(AppRoute.QuranReader(surah, ayah)) },
                    onLongClickAyah = { actionAyah = it },
                )
            }
        }
    }

    actionAyah?.let { item ->
        CollectionAyahActionSheet(
            item = item,
            store = store,
            onOpen = { nav.navigate(AppRoute.QuranReader(item.surah, item.ayah)) },
            onShareToStudio = { nav.navigate(AppRoute.Studio(item.surah, item.ayah)) },
            onDismiss = { actionAyah = null },
        )
    }
}
