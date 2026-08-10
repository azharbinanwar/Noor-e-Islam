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
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.permissions.AppPermission
import com.kodeelite.nooreislam.core.permissions.PermissionStatus
import com.kodeelite.nooreislam.core.permissions.rememberPermissionService
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.notif_exact_alarm_needed
import com.kodeelite.nooreislam.resources.notif_exact_alarm_sub
import com.kodeelite.nooreislam.resources.notif_needs_attention
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

// Red tile when exact-alarm access isn't granted. Checked once on cold start — canScheduleExactAlarms()
// is only guaranteed accurate for a fresh process (Android kills the app when this is revoked).
@Composable
fun ExactAlarmNeedsAttention() {
    val c = AppTheme.colors
    val perms = rememberPermissionService()
    val scope = rememberCoroutineScope()
    var granted by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { granted = perms.status(AppPermission.ExactAlarm) == PermissionStatus.Granted }

    if (granted) return

    AppTileGroup(
        modifier = Modifier.fillMaxWidth(),
        title = stringResource(Res.string.notif_needs_attention),
        items = listOf(
            AppTileItem(
                title = stringResource(Res.string.notif_exact_alarm_needed),
                subtitle = stringResource(Res.string.notif_exact_alarm_sub),
                leadingIcon = Lucide.Clock, leadingColor = c.error,
                onClick = {
                    scope.launch {
                        perms.request(AppPermission.ExactAlarm)
                        granted = perms.status(AppPermission.ExactAlarm) == PermissionStatus.Granted
                    }
                },
            )
        ),
    )
    Spacer(Modifier.height(12.dp))
}
