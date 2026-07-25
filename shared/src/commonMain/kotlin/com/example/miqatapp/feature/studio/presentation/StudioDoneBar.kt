package com.example.miqatapp.feature.studio.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Maximize
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Share
import com.example.miqatapp.config.theme.AppTheme

// Preview/done bar (shown when not editing): edit · save-to-gallery · share · export sizes.
@Composable
internal fun BoxScope.StudioDoneBar(
    onEdit: () -> Unit,
    onSaveToGallery: () -> Unit,
    onShare: () -> Unit,
    onExport: () -> Unit,
) {
    val colors = AppTheme.colors
    Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp)) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            StudioButton(Lucide.Pencil, onClick = onEdit)
            Spacer(Modifier.size(16.dp))
            StudioButton(Lucide.Download, onClick = onSaveToGallery)
            Spacer(Modifier.size(16.dp))
            StudioButton(
                Lucide.Share,
                size = 64.dp,
                iconSize = 28.dp,
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                onClick = onShare,
            )
            Spacer(Modifier.size(16.dp))
            StudioButton(Lucide.Maximize, onClick = onExport)
        }
    }
}
