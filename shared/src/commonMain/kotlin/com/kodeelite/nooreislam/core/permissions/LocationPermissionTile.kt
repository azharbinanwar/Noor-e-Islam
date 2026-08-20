package com.kodeelite.nooreislam.core.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPinOff
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.AppTileVariant
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.location_permission_needed
import com.kodeelite.nooreislam.resources.location_permission_rationale
import com.kodeelite.nooreislam.resources.location_permission_sub
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

/** The row while location is refused, null once granted. Polls, so Settings clears it. */
@Suppress("ComposableNaming")
@Composable
fun LocationPermissionTile(variant: AppTileVariant = AppTileVariant.Warning): AppTileItem? {
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
    LaunchedEffect(tick) { granted = perms.status(AppPermission.Location) == PermissionStatus.Granted }

    if (showDenied) LocationDeniedSheet(
        onOpenSettings = { showDenied = false; perms.openAppSettings() },
        onDismiss = { showDenied = false },
    )
    if (granted) return null

    return AppTileItem(
        title = stringResource(Res.string.location_permission_needed),
        subtitle = stringResource(Res.string.location_permission_sub),
        variant = variant,
        leadingIcon = Lucide.MapPinOff,
        onClick = {
            scope.launch {
                if (perms.request(AppPermission.Location) == PermissionStatus.DeniedPermanently) showDenied = true
                tick++
            }
        },
    )
}
