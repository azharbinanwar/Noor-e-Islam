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
import com.composables.icons.lucide.CloudDownload
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppButtonVariant
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.backup_found
import com.kodeelite.nooreislam.resources.backup_found_body
import com.kodeelite.nooreislam.resources.not_now
import com.kodeelite.nooreislam.resources.restore
import org.jetbrains.compose.resources.stringResource

/** The account already has a backup and this phone has none: offer it once, right after connecting. */
@Composable
fun BackupFoundSheet(date: String, size: String, onRestore: () -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    AppBottomSheet(
        onDismiss = onDismiss,
        footer = {
            AppButton(stringResource(Res.string.restore), onRestore, Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            AppButton(stringResource(Res.string.not_now), onDismiss, Modifier.fillMaxWidth(), variant = AppButtonVariant.Text)
        },
    ) {
        StateView(
            title = stringResource(Res.string.backup_found),
            padding = 0.dp,
            message = stringResource(Res.string.backup_found_body, date, size),
            icon = {
                Box(
                    Modifier.size(72.dp).clip(CircleShape).background(c.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Lucide.CloudDownload, null, tint = c.primary, modifier = Modifier.size(34.dp))
                }
            },
        )
    }
}
