package com.kodeelite.nooreislam.feature.backup.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RotateCcw
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppButtonVariant
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.backup_restore_body
import com.kodeelite.nooreislam.resources.backup_restore_body_quran
import com.kodeelite.nooreislam.resources.backup_restore_from_drive
import com.kodeelite.nooreislam.resources.cancel
import com.kodeelite.nooreislam.resources.restore
import org.jetbrains.compose.resources.stringResource

/** Restore replaces this phone's data; say so once, plainly, before doing it. */
@Composable
fun RestoreConfirmSheet(quran: Boolean, backupDate: String, onRestore: () -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    AppBottomSheet(
        onDismiss = onDismiss,
        footer = {
            AppButton(stringResource(Res.string.restore), onRestore, Modifier.fillMaxWidth(), variant = AppButtonVariant.Error)
            Spacer(Modifier.height(8.dp))
            AppButton(stringResource(Res.string.cancel), onDismiss, Modifier.fillMaxWidth(), variant = AppButtonVariant.Text)
        },
    ) {
        StateView(
            title = stringResource(Res.string.backup_restore_from_drive),
            padding = 0.dp,
            message = stringResource(if (quran) Res.string.backup_restore_body_quran else Res.string.backup_restore_body, backupDate),
            icon = {
                Box(
                    Modifier.size(72.dp).clip(CircleShape).background(c.error.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Lucide.RotateCcw, null, tint = c.error, modifier = Modifier.size(34.dp))
                }
            },
        )
    }
}
