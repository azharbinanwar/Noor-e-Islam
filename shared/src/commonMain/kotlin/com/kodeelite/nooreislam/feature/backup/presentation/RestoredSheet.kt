package com.kodeelite.nooreislam.feature.backup.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.backup_restored_body
import com.kodeelite.nooreislam.resources.backup_restored_title
import com.kodeelite.nooreislam.resources.restart_now
import org.jetbrains.compose.resources.stringResource

/** The data is in place; the restart is hers to trigger, so it never reads as a crash. */
@Composable
fun RestoredSheet(onRestart: () -> Unit) {
    val c = AppTheme.colors
    AppBottomSheet(
        onDismiss = onRestart, // there is no going back once the files are swapped
        footer = { AppButton(stringResource(Res.string.restart_now), onRestart, Modifier.fillMaxWidth()) },
    ) {
        StateView(
            title = stringResource(Res.string.backup_restored_title),
            padding = 0.dp,
            message = stringResource(Res.string.backup_restored_body),
            icon = {
                Box(
                    Modifier.size(72.dp).clip(CircleShape).background(c.success.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Lucide.Check, null, tint = c.success, modifier = Modifier.size(34.dp))
                }
            },
        )
    }
}
