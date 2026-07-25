package com.kodeelite.nooreislam.feature.studio.presentation.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme

// Reset to default + save current as a creation.
@Composable
fun PresetsPanel(onReset: () -> Unit, onSave: () -> Unit) {
    val colors = AppTheme.colors
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(containerColor = colors.onSurface.copy(0.1f), contentColor = colors.onSurface),
        ) { Text("Reset to Default", fontSize = 11.sp) }
        Button(onClick = onSave, colors = ButtonDefaults.buttonColors(containerColor = colors.primary)) {
            Text("Save current as creation", fontSize = 11.sp)
        }
    }
}
