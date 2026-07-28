package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.feature.quran.data.Highlight
import com.kodeelite.nooreislam.feature.quran.data.HighlightColor
import com.kodeelite.nooreislam.feature.quran.data.HighlightsStore
import com.kodeelite.nooreislam.feature.quran.data.QuranRepository
import com.kodeelite.nooreislam.feature.quran.data.hue
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.copy_ayah
import com.kodeelite.nooreislam.resources.remove_highlight
import com.kodeelite.nooreislam.resources.share_to_studio
import com.kodeelite.nooreislam.resources.surah_number_ayah_number
import org.jetbrains.compose.resources.stringResource

@Composable
fun HighlightActionSheet(
    highlight: Highlight,
    store: HighlightsStore,
    onShareToStudio: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val clipboard = LocalClipboardManager.current
    val text by produceState("") {
        value = QuranRepository.ayah(highlight.surah, highlight.ayah)?.text ?: ""
    }

    AppBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(Res.string.surah_number_ayah_number, highlight.surah, highlight.ayah),
    ) {
        // 1. Color Picker (Optimized spacing to fit without scrolling)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HighlightColor.entries.forEach { c ->
                val selected = highlight.color == c
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(c.hue)
                        .border(
                            width = 2.5.dp,
                            color = if (selected) colors.onSurface else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable {
                            store.set(highlight.surah, highlight.ayah, c)
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Icon(
                            Lucide.Check,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // 2. Actions using AppTileGroup
        AppTileGroup(
            items = listOf(
                AppTileItem(
                    title = stringResource(Res.string.share_to_studio),
                    leadingIcon = Lucide.Image,
                    onClick = { onShareToStudio(); onDismiss() }
                ),
                AppTileItem(
                    title = stringResource(Res.string.copy_ayah),
                    leadingIcon = Lucide.Copy,
                    onClick = {
                        clipboard.setText(AnnotatedString(text))
                        onDismiss()
                    }
                ),
                AppTileItem(
                    title = stringResource(Res.string.remove_highlight),
                    leadingIcon = Lucide.Minus,
                    leadingColor = colors.error,
                    onClick = { store.set(highlight.surah, highlight.ayah, null); onDismiss() }
                )
            )
        )
    }
}
