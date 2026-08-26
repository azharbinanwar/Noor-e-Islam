package com.kodeelite.nooreislam.feature.backup.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.Unlink
import com.composables.icons.lucide.UserRound
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.backup_delete_from_drive
import com.kodeelite.nooreislam.resources.backup_delete_hint
import com.kodeelite.nooreislam.resources.disconnect
import com.kodeelite.nooreislam.resources.disconnect_hint
import com.kodeelite.nooreislam.resources.one_account_note
import com.kodeelite.nooreislam.resources.use_another_account
import org.jetbrains.compose.resources.stringResource

/** An account is connected: swap it or forget it. Disconnecting keeps the file on Drive. */
@Composable
fun BackupAccountSheet(account: String, hasBackup: Boolean, onSwitch: () -> Unit, onDisconnect: () -> Unit, onDelete: () -> Unit, onDismiss: () -> Unit) {
    AppBottomSheet(onDismiss = onDismiss, title = account, subtitle = stringResource(Res.string.one_account_note)) {
        AppTileGroup(
            modifier = Modifier.padding(top = 4.dp),
            items = listOf(
                AppTileItem(leadingIcon = Lucide.UserRound, title = stringResource(Res.string.use_another_account), onClick = onSwitch),
                AppTileItem(
                    leadingIcon = Lucide.Unlink,
                    title = stringResource(Res.string.disconnect),
                    subtitle = stringResource(Res.string.disconnect_hint),
                    onClick = onDisconnect,
                ),
                // only when Drive actually holds one; otherwise there is nothing to warn about
                if (hasBackup) AppTileItem(
                    leadingIcon = Lucide.Trash2,
                    leadingColor = AppTheme.colors.error,
                    title = stringResource(Res.string.backup_delete_from_drive),
                    subtitle = stringResource(Res.string.backup_delete_hint),
                    onClick = onDelete,
                ) else null,
            ),
        )
    }
}
