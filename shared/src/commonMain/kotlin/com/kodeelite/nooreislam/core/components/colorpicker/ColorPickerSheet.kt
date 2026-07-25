package com.kodeelite.nooreislam.core.components.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet

/**
 * Modal "pick and confirm" wrapper over [AppBottomSheet]. Commits on Done via [onConfirm];
 * optional [presets] show as quick-tap swatches above the picker.
 */
@Composable
fun ColorPickerSheet(
    initialColor: Color,
    onConfirm: (Color) -> Unit,
    onDismiss: () -> Unit,
    showAlpha: Boolean = false,
    presets: List<Color> = emptyList(),
) {
    val colors = AppTheme.colors
    var current by remember { mutableStateOf(initialColor) }
    AppBottomSheet(
        onDismiss = onDismiss,
        title = "Pick a color",
        footer = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(onClick = { onConfirm(current); onDismiss() }, modifier = Modifier.weight(1f)) { Text("Done") }
            }
        },
    ) {
        if (presets.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                presets.forEach { p ->
                    Box(
                        Modifier.size(30.dp).clip(CircleShape).background(p)
                            .border(1.dp, colors.onSurface.copy(alpha = 0.15f), CircleShape)
                            .clickable { current = p }
                    )
                }
            }
        }
        ColorPicker(current, onColorChange = { current = it }, showAlpha = showAlpha)
    }
}
