package com.kodeelite.nooreislam.feature.focus.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.VolumeX
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppButtonVariant
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.focus_dnd_sheet_body
import com.kodeelite.nooreislam.resources.focus_dnd_sheet_note
import com.kodeelite.nooreislam.resources.focus_dnd_sheet_title
import com.kodeelite.nooreislam.resources.focus_dnd_use_vibrate
import com.kodeelite.nooreislam.resources.open_settings
import org.jetbrains.compose.resources.stringResource

/** Why Silent needs Do Not Disturb access, before the user is handed to the settings page. */
@Composable
fun DndAccessSheet(onOpenSettings: () -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    AppBottomSheet(
        onDismiss = onDismiss,
        footer = {
            AppButton(stringResource(Res.string.open_settings), onOpenSettings, Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            AppButton(
                stringResource(Res.string.focus_dnd_use_vibrate),
                onDismiss,
                Modifier.fillMaxWidth(),
                variant = AppButtonVariant.Text,
            )
        },
    ) {
        StateView(
            title = stringResource(Res.string.focus_dnd_sheet_title),
            padding = 0.dp,
            message = stringResource(Res.string.focus_dnd_sheet_body),
            icon = {
                Box(
                    Modifier.size(72.dp).clip(CircleShape).background(c.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Lucide.VolumeX, null, tint = c.primary, modifier = Modifier.size(34.dp))
                }
            },
        )
        Text(
            stringResource(Res.string.focus_dnd_sheet_note),
            modifier = Modifier.fillMaxWidth(),
            fontSize = 13.sp,
            color = c.onSurfaceVariant.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
        )
    }
}
