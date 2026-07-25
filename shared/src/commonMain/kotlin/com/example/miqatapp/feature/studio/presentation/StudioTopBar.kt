package com.example.miqatapp.feature.studio.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.GalleryThumbnails
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Redo
import com.composables.icons.lucide.Undo
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.locale.tr

// Editing top bar: back · undo/redo · save · gallery · done.
@Composable
internal fun StudioTopBar(
    savedHint: Boolean,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit,
    onGallery: () -> Unit,
    onDone: () -> Unit,
) {
    val colors = AppTheme.colors
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StudioButton(tr(Lucide.ChevronLeft, Lucide.ChevronRight), onClick = onBack)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            StudioButton(Lucide.Undo, size = 38.dp, iconSize = 18.dp, onClick = onUndo)
            StudioButton(Lucide.Redo, size = 38.dp, iconSize = 18.dp, onClick = onRedo)
            Spacer(Modifier.size(4.dp))
            StudioButton(if (savedHint) Lucide.Check else Lucide.Bookmark, onClick = onSave)
            StudioButton(Lucide.GalleryThumbnails, onClick = onGallery)
            StudioButton(Lucide.Check, containerColor = colors.primary, contentColor = colors.onPrimary, onClick = onDone)
        }
    }
}
