package com.kodeelite.nooreislam.core.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.AppTileVariant
import com.kodeelite.nooreislam.feature.notifications.scheduler.NotificationScheduler
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.notif_exact_alarm_needed
import com.kodeelite.nooreislam.resources.notif_exact_alarm_sub
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * The row while exact alarms are refused, null once allowed. Checked once on cold start:
 * `canScheduleExactAlarms()` is only guaranteed accurate for a fresh process, since Android kills
 * the app when this is revoked. An app declaring USE_EXACT_ALARM is always granted, so this never shows.
 */
@Suppress("ComposableNaming")
@Composable
fun ExactAlarmPermissionTile(variant: AppTileVariant = AppTileVariant.Error): AppTileItem? {
    val perms = rememberPermissionService()
    val scope = rememberCoroutineScope()
    var granted by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { granted = perms.status(AppPermission.ExactAlarm) == PermissionStatus.Granted }
    if (granted) return null

    return AppTileItem(
        title = stringResource(Res.string.notif_exact_alarm_needed),
        subtitle = stringResource(Res.string.notif_exact_alarm_sub),
        variant = variant,
        leadingIcon = Lucide.Clock,
        onClick = {
            scope.launch {
                perms.request(AppPermission.ExactAlarm)
                granted = perms.status(AppPermission.ExactAlarm) == PermissionStatus.Granted
                // alarms armed while denied went out inexact — re-arm them as exact ones
                if (granted) NotificationScheduler.rebuildAsync()
            }
        },
    )
}
