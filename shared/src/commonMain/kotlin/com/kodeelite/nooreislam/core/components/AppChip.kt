package com.kodeelite.nooreislam.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme

// rounded selectable chip; optional leading icon
@Composable
fun AppChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    // shown but not offered — dimmed and inert, so the reason stays visible
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    val tint = if (selected) colors.primary else colors.onSurfaceVariant
    val shape = RoundedCornerShape(50)
    Row(
        Modifier.alpha(if (enabled) 1f else 0.45f).clip(shape)
            // both states tint what is behind rather than naming a colour: a fixed fill matched
            // the bottom sheet's own surface and disappeared there, while reading fine on a screen
            .background(if (selected) colors.primary.copy(alpha = 0.14f) else colors.onSurface.copy(alpha = 0.06f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
        Text(label, color = tint, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}
