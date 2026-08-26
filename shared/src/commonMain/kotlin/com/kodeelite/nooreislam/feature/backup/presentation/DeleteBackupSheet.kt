package com.kodeelite.nooreislam.feature.backup.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Dot
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trash2
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppButtonVariant
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.backup_delete_body
import com.kodeelite.nooreislam.resources.backup_delete_bullet_next
import com.kodeelite.nooreislam.resources.backup_delete_bullet_phone
import com.kodeelite.nooreislam.resources.backup_delete_bullet_removed
import com.kodeelite.nooreislam.resources.backup_delete_bullet_undo
import com.kodeelite.nooreislam.resources.backup_delete_title
import com.kodeelite.nooreislam.resources.cancel
import com.kodeelite.nooreislam.resources.delete
import org.jetbrains.compose.resources.stringResource

/** Removing the Drive copy is final; say what goes and what stays before the red button. */
@Composable
fun DeleteBackupSheet(account: String, onDelete: () -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    AppBottomSheet(
        onDismiss = onDismiss,
        footer = {
            AppButton(stringResource(Res.string.delete), onDelete, Modifier.fillMaxWidth(), variant = AppButtonVariant.Error)
            Spacer(Modifier.height(8.dp))
            AppButton(stringResource(Res.string.cancel), onDismiss, Modifier.fillMaxWidth(), variant = AppButtonVariant.Text)
        },
    ) {
        StateView(
            title = stringResource(Res.string.backup_delete_title),
            padding = 0.dp,
            message = stringResource(Res.string.backup_delete_body, account),
            icon = {
                Box(
                    Modifier.size(72.dp).clip(CircleShape).background(c.error.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Lucide.Trash2, null, tint = c.error, modifier = Modifier.size(34.dp))
                }
            },
        )
        Column(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp)) {
            listOf(
                Res.string.backup_delete_bullet_removed,
                Res.string.backup_delete_bullet_undo,
                Res.string.backup_delete_bullet_phone,
                Res.string.backup_delete_bullet_next,
            ).forEach { line ->
                Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
                    Icon(Lucide.Dot, null, tint = c.error, modifier = Modifier.padding(top = 2.dp).size(16.dp))
                    Text(stringResource(line), fontSize = 14.sp, color = c.onSurface, modifier = Modifier.padding(start = 10.dp))
                }
            }
        }
    }
}
