package com.kodeelite.nooreislam.core.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.VolumeX
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.AppTileVariant
import com.kodeelite.nooreislam.core.focus.rememberFocusSetup
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.focus_dnd_needed
import com.kodeelite.nooreislam.resources.focus_dnd_sub
import org.jetbrains.compose.resources.stringResource

/** The row while Do Not Disturb access is missing, null once granted. Only Silent needs it. */
@Composable
fun DndPermissionTile(variant: AppTileVariant = AppTileVariant.Error): AppTileItem? {
    val setup = rememberFocusSetup()
    var granted by remember { mutableStateOf(true) }
    LifecycleResumeEffect(Unit) {
        granted = !setup.supported || setup.hasSilenceAccess()
        onPauseOrDispose { }
    }
    if (granted) return null

    return AppTileItem(
        title = stringResource(Res.string.focus_dnd_needed),
        subtitle = stringResource(Res.string.focus_dnd_sub),
        variant = variant,
        leadingIcon = Lucide.VolumeX,
        onClick = { setup.requestSilenceAccess() },
    )
}
