package com.kodeelite.nooreislam.feature.notifications.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.BellOff
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.displayName
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.permissions.AppPermission
import com.kodeelite.nooreislam.core.permissions.PermissionDeniedSheet
import com.kodeelite.nooreislam.core.permissions.PermissionStatus
import com.kodeelite.nooreislam.core.permissions.rememberPermissionService
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.notif_needs_attention
import com.kodeelite.nooreislam.resources.notif_permission_denied_msg
import com.kodeelite.nooreislam.resources.notif_permission_denied_title
import com.kodeelite.nooreislam.resources.notif_permission_needed
import com.kodeelite.nooreislam.resources.notif_permission_sub
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

// Red tile at the top when notifications aren't granted. Polls so it clears on return from Settings.
@Composable
fun NotificationsNeedsAttention() {
    val c = AppTheme.colors
    val perms = rememberPermissionService()
    val scope = rememberCoroutineScope()
    var showDenied by remember { mutableStateOf(false) }
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1500.milliseconds); tick++
        }
    }
    var granted by remember { mutableStateOf(true) }
    LaunchedEffect(tick) { granted = perms.status(AppPermission.Notifications) == PermissionStatus.Granted }

    if (showDenied) PermissionDeniedSheet(
        title = stringResource(Res.string.notif_permission_denied_title),
        message = stringResource(Res.string.notif_permission_denied_msg, koinInject<AppEdition>().displayName()),
        onOpenSettings = { showDenied = false; perms.openAppSettings() },
        onDismiss = { showDenied = false },
    )
    if (granted) return

    AppTileGroup(
        modifier = Modifier.fillMaxWidth(),
        title = stringResource(Res.string.notif_needs_attention),
        items = listOf(
            AppTileItem(
                title = stringResource(Res.string.notif_permission_needed),
                subtitle = stringResource(Res.string.notif_permission_sub),
                leadingIcon = Lucide.BellOff, leadingColor = c.error,
                onClick = {
                    scope.launch {
                        if (perms.request(AppPermission.Notifications) == PermissionStatus.DeniedPermanently) showDenied = true
                        tick++
                    }
                },
            )
        ),
    )
    Spacer(Modifier.height(12.dp))
}
