package com.kodeelite.nooreislam.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kodeelite.nooreislam.config.theme.AppTheme

/** [label] is the icon's content description — nothing here is spelled out on screen. */
class AppIconOption<T>(
    val icon: ImageVector,
    val value: T,
    val label: String? = null,
)

/** Inline row of icon squares, one chosen. Lives in a tile's trailing slot, not in a sheet. */
@Composable
fun <T> AppIconToggle(
    options: List<AppIconOption<T>>,
    selected: T,
    onPick: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    val shape = RoundedCornerShape(10.dp)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val on = option.value == selected
            Box(
                Modifier.size(34.dp).clip(shape)
                    .background(if (on) c.primary.copy(alpha = 0.1f) else c.onSurface.copy(alpha = 0.05f))
                    .border(1.5.dp, if (on) c.primary else Color.Transparent, shape)
                    .clickable(enabled = !on) { onPick(option.value) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    option.icon,
                    option.label,
                    tint = if (on) c.primary else c.onSurfaceVariant,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
    }
}
