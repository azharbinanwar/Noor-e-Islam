package com.kodeelite.nooreislam.core.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.composables.icons.lucide.BellOff
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.AppTileVariant
import com.kodeelite.nooreislam.core.displayName
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.notif_permission_denied_msg
import com.kodeelite.nooreislam.resources.notif_permission_denied_title
import com.kodeelite.nooreislam.resources.notif_permission_needed
import com.kodeelite.nooreislam.resources.notif_permission_sub
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

/** The row while notifications are blocked, null once granted. Polls, so Settings clears it. */
@Composable
fun NotificationPermissionTile(variant: AppTileVariant = AppTileVariant.Error): AppTileItem? {
    val perms = rememberPermissionService()
    val scope = rememberCoroutineScope()
    var showDenied by remember { mutableStateOf(false) }
    var tick by remember { mutableStateOf(0) }
    var granted by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1500.milliseconds); tick++
        }
    }
    LaunchedEffect(tick) { granted = perms.status(AppPermission.Notifications) == PermissionStatus.Granted }

    if (showDenied) NotificationDeniedSheet(
        onOpenSettings = { showDenied = false; perms.openAppSettings() },
        onDismiss = { showDenied = false },
    )
    if (granted) return null

    return AppTileItem(
        title = stringResource(Res.string.notif_permission_needed),
        subtitle = stringResource(Res.string.notif_permission_sub),
        variant = variant,
        leadingIcon = Lucide.BellOff,
        onClick = {
            scope.launch {
                if (perms.request(AppPermission.Notifications) == PermissionStatus.DeniedPermanently) showDenied = true
                tick++
            }
        },
    )
}
