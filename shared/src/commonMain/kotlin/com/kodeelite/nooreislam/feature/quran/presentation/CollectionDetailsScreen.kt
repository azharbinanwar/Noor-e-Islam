package com.kodeelite.nooreislam.feature.quran.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.FolderMinus
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppButtonVariant
import com.kodeelite.nooreislam.core.components.AppTextField
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.core.locale.tr
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.feature.quran.data.Collection
import com.kodeelite.nooreislam.feature.quran.data.CollectionAyah
import com.kodeelite.nooreislam.feature.quran.data.CollectionStore
import com.kodeelite.nooreislam.feature.quran.presentation.components.CollectionAyahActionSheet
import com.kodeelite.nooreislam.feature.quran.presentation.components.CollectionAyahItem
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.back
import com.kodeelite.nooreislam.resources.cancel
import com.kodeelite.nooreislam.resources.collection_details_empty_message
import com.kodeelite.nooreislam.resources.collection_empty_hint
import com.kodeelite.nooreislam.resources.delete
import com.kodeelite.nooreislam.resources.collection_name_placeholder
import com.kodeelite.nooreislam.resources.delete_collection_message
import com.kodeelite.nooreislam.resources.delete_collection_title
import com.kodeelite.nooreislam.resources.rename_collection
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

// A collection's own ayah list — collection name comes from the already-cached CollectionStore.collections
// list (looked up by id), not passed through the route; AppRoute stays feature-agnostic that way.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailsScreen(collectionId: Long) {
    val nav = LocalAppNavigator.current
    val store = koinInject<CollectionStore>()
    val collections by store.collections.collectAsState()
    val collection = collections?.firstOrNull { it.id == collectionId }
    val ayahsState by store.ayahsIn(collectionId).collectAsState(initial = null)
    var confirmingDelete by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf(false) }
    var actionAyah by remember { mutableStateOf<CollectionAyah?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(collection?.name ?: "") },
                navigationIcon = {
                    IconButton({ nav.back() }) {
                        Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), stringResource(Res.string.back), tint = AppTheme.colors.onSurface)
                    }
                },
                actions = {
                    IconButton({ renaming = true }) {
                        Icon(Lucide.Pencil, stringResource(Res.string.rename_collection), tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(22.dp))
                    }
                    IconButton({ confirmingDelete = true }) {
                        Icon(Lucide.FolderMinus, stringResource(Res.string.delete), tint = AppTheme.colors.error, modifier = Modifier.size(22.dp))
                    }
                },
            )
        },
    ) { padding ->
        val ayahs = ayahsState
        if (ayahs == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                StateView.Loading()
            }
        } else if (ayahs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                StateView(
                    title = stringResource(Res.string.collection_empty_hint),
                    message = stringResource(Res.string.collection_details_empty_message),
                    icon = { Icon(Lucide.BookOpen, null, tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(40.dp)) },
                )
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(ayahs.size, key = { ayahs[it].id }) { i ->
                    val item = ayahs[i]
                    CollectionAyahItem(
                        item = item,
                        index = i,
                        total = ayahs.size,
                        onClick = { nav.navigate(AppRoute.QuranReader(item.surah, item.ayah)) },
                        onLongClick = { actionAyah = item },
                    )
                }
            }
        }
    }

    if (renaming && collection != null) {
        RenameCollectionSheet(
            collection = collection,
            store = store,
            onDismiss = { renaming = false },
        )
    }

    if (confirmingDelete && collection != null) {
        DeleteCollectionConfirmSheet(
            collection = collection,
            ayahCount = ayahsState?.size ?: 0,
            onConfirm = {
                store.removeCollection(collection.id)
                confirmingDelete = false
                nav.back()
            },
            onDismiss = { confirmingDelete = false },
        )
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

@Composable
private fun RenameCollectionSheet(collection: Collection, store: CollectionStore, onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    var name by remember(collection.id) { mutableStateOf(collection.name) }

    AppBottomSheet(onDismiss = onDismiss, title = stringResource(Res.string.rename_collection)) {
        AppTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = stringResource(Res.string.collection_name_placeholder),
            trailing = {
                IconButton(onClick = {
                    if (name.isNotBlank()) {
                        store.renameCollection(collection.id, name)
                        onDismiss()
                    }
                }) { Icon(Lucide.Check, null, tint = colors.primary) }
            },
        )
    }
}

@Composable
private fun DeleteCollectionConfirmSheet(collection: Collection, ayahCount: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    AppBottomSheet(
        onDismiss = onDismiss,
        footer = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppButton(
                    stringResource(Res.string.cancel),
                    onClick = onDismiss,
                    variant = AppButtonVariant.Outline,
                    modifier = Modifier.weight(1f),
                )
                AppButton(
                    stringResource(Res.string.delete),
                    onClick = onConfirm,
                    variant = AppButtonVariant.Error,
                    modifier = Modifier.weight(1f),
                )
            }
        },
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(56.dp).clip(CircleShape).background(colors.error.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) { Icon(Lucide.FolderMinus, null, tint = colors.error, modifier = Modifier.size(26.dp)) }
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(Res.string.delete_collection_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(Res.string.delete_collection_message, ayahCount, collection.name),
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
