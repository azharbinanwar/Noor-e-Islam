package com.kodeelite.nooreislam.feature.studio.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Round icon button used across the studio's top bar and done bar. While [isProcessing] it shows a spinner and ignores taps.
@Composable
internal fun StudioButton(
    icon: ImageVector,
    onClick: () -> Unit,
    size: Dp = 48.dp,
    iconSize: Dp = 22.dp,
    containerColor: Color = Color.Black.copy(alpha = 0.25f),
    contentColor: Color = Color.White,
    isProcessing: Boolean = false,
) {
    Box(
        Modifier.size(size).clip(CircleShape).background(containerColor).clickable(enabled = !isProcessing, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isProcessing) CircularProgressIndicator(Modifier.size(iconSize), color = contentColor, strokeWidth = 2.dp)
        else Icon(icon, null, tint = contentColor, modifier = Modifier.size(iconSize))
    }
}
