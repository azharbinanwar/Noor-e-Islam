package com.kodeelite.nooreislam.feature.focus.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.BellOff
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.VolumeX
import com.composables.icons.lucide.Zap
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.focus.SilenceMode
import com.kodeelite.nooreislam.core.focus.rememberFocusSetup
import com.kodeelite.nooreislam.core.permissions.AppPermission
import com.kodeelite.nooreislam.core.permissions.PermissionDeniedSheet
import com.kodeelite.nooreislam.core.permissions.PermissionStatus
import com.kodeelite.nooreislam.core.permissions.rememberPermissionService
import com.kodeelite.nooreislam.core.store.PrayerFocusStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.focus_allow_background
import com.kodeelite.nooreislam.resources.focus_allow_background_sub
import com.kodeelite.nooreislam.resources.focus_dnd_needed
import com.kodeelite.nooreislam.resources.focus_dnd_sub
import com.kodeelite.nooreislam.resources.focus_needs_attention
import com.kodeelite.nooreislam.resources.focus_notif_denied_msg
import com.kodeelite.nooreislam.resources.focus_notif_denied_title
import com.kodeelite.nooreislam.resources.focus_notif_needed
import com.kodeelite.nooreislam.resources.focus_notif_sub
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

// Warning rows for anything missing (notifications + DND required, red) plus an optional battery nudge.
// Self-contained: reads the permission/setup state and polls so rows refresh on return from Settings.
// Renders nothing on platforms without the feature, or once everything's granted.
@Composable
fun FocusNeedsAttention() {
    val setup = rememberFocusSetup()
    if (!setup.supported) return
    val c = AppTheme.colors
    val perms = rememberPermissionService()
    val scope = rememberCoroutineScope()
    val configs by PrayerFocusStore.configs.collectAsState()
    var showNotifDenied by remember { mutableStateOf(false) }
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1500.milliseconds); tick++
        }
    }
    val battOk = remember(tick) { setup.batteryUnrestricted() }
    val dndOk = remember(tick) { setup.hasSilenceAccess() }
    var notifOk by remember { mutableStateOf(true) }
    LaunchedEffect(tick) { notifOk = perms.status(AppPermission.Notifications) == PermissionStatus.Granted }

    if (showNotifDenied) PermissionDeniedSheet(
        title = stringResource(Res.string.focus_notif_denied_title),
        message = stringResource(Res.string.focus_notif_denied_msg),
        onOpenSettings = { showNotifDenied = false; perms.openAppSettings() },
        onDismiss = { showNotifDenied = false },
    )

    val anySilent = configs.values.any { it.enabled && it.mode == SilenceMode.Silent }
    val warnings = buildList {
        if (!notifOk) add(
            AppTileItem(
                title = stringResource(Res.string.focus_notif_needed),
                subtitle = stringResource(Res.string.focus_notif_sub),
                leadingIcon = Lucide.BellOff, leadingColor = c.error,
                onClick = {
                    scope.launch {
                        if (perms.request(AppPermission.Notifications) == PermissionStatus.DeniedPermanently) showNotifDenied = true
                        tick++
                    }
                },
            )
        )
        if (anySilent && !dndOk) add(
            AppTileItem(
                title = stringResource(Res.string.focus_dnd_needed),
                subtitle = stringResource(Res.string.focus_dnd_sub),
                leadingIcon = Lucide.VolumeX, leadingColor = c.error,
                onClick = { setup.requestSilenceAccess() },
            )
        )
    }
    if (warnings.isNotEmpty()) {
        AppTileGroup(modifier = Modifier.fillMaxWidth(), title = stringResource(Res.string.focus_needs_attention), items = warnings)
        Spacer(Modifier.height(12.dp))
    }
    // Optional (feature works without it, flag is unreliable on some OEMs) -> plain nudge, not red.
    if (!battOk) {
        AppTileGroup(
            modifier = Modifier.fillMaxWidth(),
            items = listOf(
                AppTileItem(
                    title = stringResource(Res.string.focus_allow_background),
                    subtitle = stringResource(Res.string.focus_allow_background_sub),
                    leadingIcon = Lucide.Zap,
                    onClick = { setup.requestBatteryUnrestricted() },
                )
            ),
        )
        Spacer(Modifier.height(12.dp))
    }
}
