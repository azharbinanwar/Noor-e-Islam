package com.kodeelite.nooreislam.feature.backup.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.CloudUpload
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.core.enums.BackupNetwork
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.backup_over
import com.kodeelite.nooreislam.resources.backup_over_body
import org.jetbrains.compose.resources.stringResource

/** Which connection the automatic backup may use. Two named rows, so neither can read as "off". */
@Composable
fun BackupNetworkSheet(current: BackupNetwork, onSelect: (BackupNetwork) -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    AppBottomSheet(onDismiss = onDismiss) {
        StateView(
            title = stringResource(Res.string.backup_over),
            padding = 0.dp,
            message = stringResource(Res.string.backup_over_body),
            icon = {
                Box(
                    Modifier.size(72.dp).clip(CircleShape).background(c.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Lucide.CloudUpload, null, tint = c.primary, modifier = Modifier.size(34.dp))
                }
            },
        )
        AppTileGroup(
            modifier = Modifier.padding(top = 16.dp),
            items = BackupNetwork.entries.map { n ->
                val selected = n == current
                AppTileItem(
                    leadingIcon = n.icon,
                    title = n.label(),
                    selected = selected,
                    trailing = if (selected) {
                        { Icon(Lucide.Check, null, tint = c.primary) }
                    } else null,
                    onClick = { onSelect(n) },
                )
            },
        )
    }
}
