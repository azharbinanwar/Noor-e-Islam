package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Folder
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.X
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppTextField
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.core.constants.defaults.QuranDefaults
import com.kodeelite.nooreislam.feature.quran.data.Ayah
import com.kodeelite.nooreislam.feature.quran.data.CollectionStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.action_add_to_collection
import com.kodeelite.nooreislam.resources.clear_search
import com.kodeelite.nooreislam.resources.collection_name_placeholder
import com.kodeelite.nooreislam.resources.no_collections_found
import com.kodeelite.nooreislam.resources.no_collections_found_hint
import com.kodeelite.nooreislam.resources.search
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

// pick an existing collection (adds immediately) or type a new name (creates it, then adds) —
// typing a name that matches an existing collection (case-insensitive) merges into it instead
// of creating a duplicate; see CollectionRepository.addToCollectionByName. Past
// QuranDefaults.COLLECTION_SEARCH_THRESHOLD a search field appears above the list.
@Composable
fun CollectionPickerSheet(ayah: Ayah, onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    val store = koinInject<CollectionStore>()
    val collections by store.collections.collectAsState()
    var name by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }

    AppBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(Res.string.action_add_to_collection),
        fillHeight = true,
        footer = {
            AppTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = stringResource(Res.string.collection_name_placeholder),
                trailing = {
                    IconButton(onClick = {
                        if (name.isNotBlank()) {
                            store.addToCollectionByName(name, ayah.surah, ayah.ayah)
                            onDismiss()
                        }
                    }) { Icon(Lucide.Plus, null, tint = colors.primary) }
                },
            )
        },
    ) {
        val all = collections.orEmpty()
        if (all.size > QuranDefaults.COLLECTION_SEARCH_THRESHOLD) {
            AppTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = stringResource(Res.string.search),
                trailing = if (query.isNotEmpty()) {
                    { IconButton(onClick = { query = "" }) { Icon(Lucide.X, stringResource(Res.string.clear_search), tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp)) } }
                } else null,
            )
            Spacer(Modifier.height(12.dp))
        }

        val filtered = if (query.isBlank()) all else all.filter { it.name.contains(query, ignoreCase = true) }
        if (filtered.isNotEmpty()) {
            AppTileGroup(
                items = filtered.map { c ->
                    AppTileItem(
                        title = c.name,
                        leadingIcon = Lucide.Folder,
                        onClick = {
                            store.addToCollection(c.id, ayah.surah, ayah.ayah)
                            onDismiss()
                        },
                    )
                },
            )
        } else if (query.isNotBlank()) {
            StateView(
                title = stringResource(Res.string.no_collections_found),
                message = stringResource(Res.string.no_collections_found_hint),
                icon = { Icon(Lucide.Search, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(36.dp)) },
                modifier = Modifier.padding(vertical = 28.dp),
            )
        }
    }
}
