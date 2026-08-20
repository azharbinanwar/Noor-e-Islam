package com.kodeelite.nooreislam.feature.focus.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileVariant
import com.kodeelite.nooreislam.core.focus.SilenceMode
import com.kodeelite.nooreislam.core.focus.rememberFocusSetup
import com.kodeelite.nooreislam.core.permissions.BatteryPermissionTile
import com.kodeelite.nooreislam.core.permissions.DndPermissionTile
import com.kodeelite.nooreislam.core.permissions.NotificationPermissionTile
import com.kodeelite.nooreislam.core.store.PrayerFocusStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.focus_allow_background
import com.kodeelite.nooreislam.resources.focus_allow_background_sub
import com.kodeelite.nooreislam.resources.focus_needs_attention
import org.jetbrains.compose.resources.stringResource

// Everything applicable at once, so nothing is discovered one fix at a time. Error is what the feature
// cannot work without; Warning is what keeps it reliable. Do Not Disturb only becomes a requirement
// once a prayer is set to Silent — before that it is a heads-up.
@Composable
fun FocusNeedsAttention() {
    if (!rememberFocusSetup().supported) return
    val configs by PrayerFocusStore.configs.collectAsState()
    val anySilent = configs.values.any { it.enabled && it.mode == SilenceMode.Silent }

    AppTileGroup(
        modifier = Modifier.fillMaxWidth(),
        title = stringResource(Res.string.focus_needs_attention),
        variant = AppTileVariant.Warning,
        items = listOf(
            NotificationPermissionTile(AppTileVariant.Error),
            DndPermissionTile(if (anySilent) AppTileVariant.Error else AppTileVariant.Warning),
            BatteryPermissionTile(
                AppTileVariant.Warning,
                title = stringResource(Res.string.focus_allow_background),
                subtitle = stringResource(Res.string.focus_allow_background_sub),
            ),
        ),
    )
}
