package com.kodeelite.nooreislam.feature.notifications.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.composables.icons.lucide.BatteryCharging
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.focus.rememberFocusSetup
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.notif_battery_needed
import com.kodeelite.nooreislam.resources.notif_battery_sub
import com.kodeelite.nooreislam.resources.notif_needs_attention
import org.jetbrains.compose.resources.stringResource

// Red tile while the user has this app background-restricted — the state that actually kills
// reminders. Not the AOSP exemption whitelist: Vivo's settings never set that flag, so a banner
// keyed on it could never hide there. Re-checked on resume because the fix lives in system settings.
@Composable
fun BatteryNeedsAttention() {
    val c = AppTheme.colors
    val setup = rememberFocusSetup()
    var unrestricted by remember { mutableStateOf(true) }
    LifecycleResumeEffect(Unit) {
        unrestricted = !setup.supported || setup.batteryUnrestricted() || !setup.backgroundRestricted()
        onPauseOrDispose { }
    }

    if (unrestricted) return

    AppTileGroup(
        modifier = Modifier.fillMaxWidth(),
        title = stringResource(Res.string.notif_needs_attention),
        items = listOf(
            AppTileItem(
                title = stringResource(Res.string.notif_battery_needed),
                subtitle = stringResource(Res.string.notif_battery_sub),
                leadingIcon = Lucide.BatteryCharging, leadingColor = c.error,
                onClick = { setup.requestBatteryUnrestricted() },
            )
        ),
    )
    Spacer(Modifier.height(12.dp))
}
