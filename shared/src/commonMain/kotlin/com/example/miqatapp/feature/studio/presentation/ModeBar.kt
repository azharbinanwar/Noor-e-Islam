package com.example.miqatapp.feature.studio.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miqatapp.config.theme.AppTheme

// Bottom footer of the studio panel: fixed-width chips for each StudioMode; tap to switch action.
@Composable
internal fun ModeBar(current: StudioMode, onSelect: (StudioMode) -> Unit) {
    val colors = AppTheme.colors
    LazyRow(
        Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
    ) {
        items(StudioMode.entries) { m ->
            val isSelected = m == current
            Column(
                Modifier.width(64.dp).clip(RoundedCornerShape(12.dp)).clickable { onSelect(m) }.padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    m.icon,
                    null,
                    tint = if (isSelected) colors.primary else colors.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    m.label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = if (isSelected) colors.primary else colors.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
    }
}
