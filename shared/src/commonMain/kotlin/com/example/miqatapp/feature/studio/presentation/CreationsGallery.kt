package com.example.miqatapp.feature.studio.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Trash2
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.datetime.Now
import com.example.miqatapp.feature.studio.data.StudioConfig
import com.example.miqatapp.feature.studio.data.StudioCreation

// Pinterest-style 2-column masonry of saved designs (varied heights from each design's aspect ratio).
// Non-lazy on purpose: it lives inside AppBottomSheet's scrolling body.
@Composable
fun ColumnScope.CreationsGrid(
    creations: List<StudioCreation>,
    draft: StudioConfig?,
    onSelect: (StudioConfig) -> Unit,
    onDelete: (Long) -> Unit,
    onResumeDraft: () -> Unit,
) {
    val colors = AppTheme.colors
    if (draft == null && creations.isEmpty()) {
        Text(
            "No designs saved yet",
            color = colors.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 40.dp),
        )
        return
    }
    if (draft != null) DraftCard(draft, onResumeDraft)
    if (creations.isEmpty()) return
    val now = remember(creations) { Now.epochMillis() }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            creations.filterIndexed { i, _ -> i % 2 == 0 }.forEach { CreationCard(it, now, onSelect, onDelete) }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            creations.filterIndexed { i, _ -> i % 2 == 1 }.forEach { CreationCard(it, now, onSelect, onDelete) }
        }
    }
}

// pinned resume-later card at the top of the sheet
@Composable
private fun DraftCard(config: StudioConfig, onResume: () -> Unit) {
    val colors = AppTheme.colors
    val ratio = config.aspectRatio.ratio ?: 0.64f
    Column(Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
        Text(
            "CONTINUE",
            color = colors.onSurfaceVariant.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
        )
        Box(
            Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceContainer).clickable { onResume() }
        ) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val scale = maxWidth / CANVAS_BASE_WIDTH
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    DesignCanvas(
                        config,
                        Modifier.requiredWidth(CANVAS_BASE_WIDTH).aspectRatio(ratio)
                            .graphicsLayer { scaleX = scale; scaleY = scale },
                        false,
                    ) {}
                }
            }
            Row(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f)).padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Lucide.Pencil, null, tint = Color.White, modifier = Modifier.size(14.dp))
                Text("Continue editing", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun CreationCard(item: StudioCreation, now: Long, onSelect: (StudioConfig) -> Unit, onDelete: (Long) -> Unit) {
    val colors = AppTheme.colors
    val ratio = item.config.aspectRatio.ratio ?: 0.64f
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(colors.surfaceContainer)
            .clickable { onSelect(item.config) }
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(ratio)) {
            val scale = maxWidth / CANVAS_BASE_WIDTH
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                DesignCanvas(
                    item.config,
                    Modifier.requiredWidth(CANVAS_BASE_WIDTH).aspectRatio(ratio)
                        .graphicsLayer { scaleX = scale; scaleY = scale },
                    false,
                ) {}
            }
        }
        // delete
        Box(
            Modifier.align(Alignment.TopEnd).padding(6.dp).size(28.dp).clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f)).clickable { onDelete(item.id) },
            contentAlignment = Alignment.Center,
        ) { Icon(Lucide.Trash2, null, tint = Color.White, modifier = Modifier.size(15.dp)) }
        // time
        Box(
            Modifier.align(Alignment.BottomStart).padding(6.dp).clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.45f)).padding(horizontal = 6.dp, vertical = 2.dp)
        ) { Text(relativeTime(item.createdAt, now), color = Color.White, fontSize = 9.sp) }
    }
}

// compact age label, locale-free (formatting pass comes with locale)
private fun relativeTime(then: Long, now: Long): String {
    val s = (now - then).coerceAtLeast(0) / 1000
    return when {
        s < 60 -> "just now"
        s < 3600 -> "${s / 60}m"
        s < 86_400 -> "${s / 3600}h"
        else -> "${s / 86_400}d"
    }
}
