package com.kodeelite.nooreislam.feature.studio.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme

/** One header action: what it says and what it does. All actions draw alike, in primary. */
data class StudioHeaderAction(val label: String, val onClick: () -> Unit)

/**
 * Panel section header: uppercase title on the left, actions on the right — "Reset | View all".
 * A light divider separates actions and only exists when there is more than one.
 */
@Composable
fun StudioSectionHeader(
    title: String,
    actions: List<StudioHeaderAction> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Row(
        modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            title.uppercase(),
            color = colors.onSurfaceVariant.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            actions.forEachIndexed { i, action ->
                if (i > 0) Text("|", color = colors.onSurfaceVariant.copy(alpha = 0.4f), fontSize = 11.sp)
                Text(
                    action.label,
                    color = colors.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable(onClick = action.onClick).padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
    }
}
