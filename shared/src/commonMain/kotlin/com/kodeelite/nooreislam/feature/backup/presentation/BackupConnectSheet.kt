package com.kodeelite.nooreislam.feature.backup.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.CloudUpload
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppButtonVariant
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.backup_bullet_data
import com.kodeelite.nooreislam.resources.backup_bullet_private
import com.kodeelite.nooreislam.resources.backup_bullet_settings
import com.kodeelite.nooreislam.resources.backup_bullet_tracker
import com.kodeelite.nooreislam.resources.backup_connect_body
import com.kodeelite.nooreislam.resources.backup_connect_title
import com.kodeelite.nooreislam.resources.cancel
import com.kodeelite.nooreislam.resources.connect
import org.jetbrains.compose.resources.stringResource

/** No account yet: say what a backup holds, then connect. */
@Composable
fun BackupConnectSheet(quran: Boolean, onConnect: () -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    AppBottomSheet(
        onDismiss = onDismiss,
        footer = {
            AppButton(stringResource(Res.string.connect), onConnect, Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            AppButton(stringResource(Res.string.cancel), onDismiss, Modifier.fillMaxWidth(), variant = AppButtonVariant.Text)
        },
    ) {
        StateView(
            title = stringResource(Res.string.backup_connect_title),
            padding = 0.dp,
            message = stringResource(Res.string.backup_connect_body),
            icon = {
                Box(
                    Modifier.size(72.dp).clip(CircleShape).background(c.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Lucide.CloudUpload, null, tint = c.primary, modifier = Modifier.size(34.dp))
                }
            },
        )
        Column(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp)) {
            listOfNotNull(
                Res.string.backup_bullet_tracker.takeUnless { quran },
                Res.string.backup_bullet_data,
                Res.string.backup_bullet_settings,
                Res.string.backup_bullet_private,
            ).forEach { line ->
                Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
                    Icon(Lucide.Check, null, tint = c.primary, modifier = Modifier.padding(top = 2.dp).size(16.dp))
                    Text(stringResource(line), fontSize = 14.sp, color = c.onSurface, modifier = Modifier.padding(start = 10.dp))
                }
            }
        }
    }
}
