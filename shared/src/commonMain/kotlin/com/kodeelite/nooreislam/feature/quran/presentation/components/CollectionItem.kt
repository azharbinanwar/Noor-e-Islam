package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Folder
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppTile
import com.kodeelite.nooreislam.core.components.TilePosition
import com.kodeelite.nooreislam.feature.quran.data.Collection
import com.kodeelite.nooreislam.feature.quran.data.CollectionAyah
import com.kodeelite.nooreislam.feature.quran.data.CollectionStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.collection_item_count
import com.kodeelite.nooreislam.resources.expand
import org.jetbrains.compose.resources.stringResource

// self-contained collection: collapsed shows "Name (count)"; the chevron folds a quick ayah list
// in beneath (tap one to jump to the reader); tapping the tile itself opens the details screen.
@Composable
fun CollectionItem(
    collection: Collection,
    store: CollectionStore,
    onOpen: () -> Unit,
    onOpenAyah: (Int, Int) -> Unit,
    onLongClickAyah: (CollectionAyah) -> Unit = {},
) {
    var expanded by rememberSaveable(collection.id) { mutableStateOf(false) }
    val ayahs by store.ayahsIn(collection.id).collectAsState(initial = null)
    val count = ayahs?.size ?: 0

    Column(Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        AppTile(
            title = stringResource(Res.string.collection_item_count, collection.name, count),
            leading = {
                Box(
                    Modifier.size(38.dp).clip(CircleShape).background(AppTheme.colors.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) { Icon(Lucide.Folder, null, tint = AppTheme.colors.primary, modifier = Modifier.size(20.dp)) }
            },
            trailing = {
                Icon(
                    Lucide.ChevronDown, stringResource(Res.string.expand), tint = AppTheme.colors.onSurfaceVariant,
                    modifier = Modifier.clip(CircleShape).clickable { expanded = !expanded }.padding(4.dp).size(20.dp).rotate(if (expanded) 180f else 0f),
                )
            },
            position = if (expanded) TilePosition.First else TilePosition.Single,
            onClick = onOpen,
        )
        if (expanded) {
            val list = ayahs.orEmpty()
            list.forEachIndexed { i, item ->
                // +1: the collection tile above is the group's first item
                CollectionAyahItem(
                    item = item,
                    index = i + 1,
                    total = list.size + 1,
                    onClick = { onOpenAyah(item.surah, item.ayah) },
                    onLongClick = { onLongClickAyah(item) },
                )
            }
        }
    }
}
