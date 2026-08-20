package com.kodeelite.nooreislam.core.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.composables.icons.lucide.LocateOff
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.AppTileVariant
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.location_is_off
import com.kodeelite.nooreislam.resources.location_is_off_sub
import org.jetbrains.compose.resources.stringResource

/**
 * The row while the device's location switch is off, null once it is on. Separate from the permission:
 * granting access to an app that cannot see any location still leaves nothing to read.
 * Re-checked on resume, since the switch lives in system settings.
 */
@Suppress("ComposableNaming")
@Composable
fun LocationServiceTile(variant: AppTileVariant = AppTileVariant.Warning): AppTileItem? {
    val geo = rememberGeoLocator()
    var enabled by remember { mutableStateOf(true) }
    LifecycleResumeEffect(Unit) {
        enabled = geo.servicesEnabled()
        onPauseOrDispose { }
    }
    if (enabled) return null

    return AppTileItem(
        title = stringResource(Res.string.location_is_off),
        subtitle = stringResource(Res.string.location_is_off_sub),
        variant = variant,
        leadingIcon = Lucide.LocateOff,
        onClick = { geo.requestLocationOn() },
    )
}
