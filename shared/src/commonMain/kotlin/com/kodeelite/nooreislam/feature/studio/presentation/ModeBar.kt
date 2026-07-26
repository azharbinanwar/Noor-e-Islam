package com.kodeelite.nooreislam.feature.studio.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kodeelite.nooreislam.core.components.ActionPosition
import com.kodeelite.nooreislam.core.components.AppAction

// Bottom footer of the studio panel: one chip per StudioMode, lazily rendered; tap to switch action.
@Composable
internal fun ModeBar(current: StudioMode, onSelect: (StudioMode) -> Unit) {
    val modes = StudioMode.entries
    LazyRow(
        Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
    ) {
        itemsIndexed(modes) { i, m ->
            AppAction(
                label = m.label,
                icon = m.icon,
                modifier = Modifier.width(70.4.dp),
                selected = m == current,
                position = ActionPosition.at(i, modes.size),
                onClick = { onSelect(m) },
            )
        }
    }
}
