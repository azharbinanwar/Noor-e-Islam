package com.kodeelite.nooreislam.core.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.composables.icons.lucide.BatteryCharging
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.AppTileVariant
import com.kodeelite.nooreislam.core.focus.rememberFocusSetup
import com.kodeelite.nooreislam.core.platform.Platform
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.notif_battery_needed
import com.kodeelite.nooreislam.resources.notif_battery_sub
import org.jetbrains.compose.resources.stringResource

/**
 * The row while the system may freeze this app in the background. Two signals: [strict] keys on the
 * user having actively restricted it in App info, which is rare but fatal; the default also fires when
 * the app simply isn't on the battery exemption list, which is the common case and only a reliability
 * risk. Re-checked on resume, since the fix lives in system settings.
 */
@Suppress("ComposableNaming")
@Composable
fun BatteryPermissionTile(
    variant: AppTileVariant = AppTileVariant.Warning,
    strict: Boolean = false,
    title: String = stringResource(Res.string.notif_battery_needed),
    subtitle: String = stringResource(Res.string.notif_battery_sub),
): AppTileItem? {
    val setup = rememberFocusSetup()
    val steps = LocalBatterySteps.current
    var unrestricted by remember { mutableStateOf(true) }
    LifecycleResumeEffect(Unit) {
        unrestricted = !Platform.canControlDnd ||
                if (strict) !setup.backgroundRestricted() else setup.batteryUnrestricted()
        onPauseOrDispose { }
    }
    if (unrestricted) return null

    return AppTileItem(
        title = title,
        subtitle = subtitle,
        variant = variant,
        leadingIcon = Lucide.BatteryCharging,
        // a phone that asks in its own dialog needs no explaining; the rest get the steps first
        // stock Android asks in a dialog and is done; every skinned phone opens its own page, and the
        // intent resolves on both, so the maker is the only thing that separates them
        onClick = { if (needsBatterySteps(setup.phoneMaker())) steps.ask() else setup.requestBatteryUnrestricted() },
    )
}
