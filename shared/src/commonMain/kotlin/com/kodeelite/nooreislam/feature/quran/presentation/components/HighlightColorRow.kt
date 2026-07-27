package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.feature.quran.data.HighlightColor
import com.kodeelite.nooreislam.feature.quran.data.hue

// row of preset highlight swatches; the selected one gets a ring + check
@Composable
fun HighlightColorRow(selected: HighlightColor?, modifier: Modifier = Modifier, onPick: (HighlightColor) -> Unit) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        HighlightColor.entries.forEach { c ->
            val isSelected = c == selected
            Box(
                Modifier.size(34.dp).clip(CircleShape).background(c.hue)
                    .then(if (isSelected) Modifier.border(2.5.dp, AppTheme.colors.onSurface, CircleShape) else Modifier)
                    .clickable { onPick(c) },
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) Icon(Lucide.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}
