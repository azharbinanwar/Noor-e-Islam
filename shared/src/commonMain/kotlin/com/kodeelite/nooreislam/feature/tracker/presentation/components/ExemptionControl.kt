package com.kodeelite.nooreislam.feature.tracker.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pause
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppSwitch
import com.kodeelite.nooreislam.core.components.AppTile
import com.kodeelite.nooreislam.feature.tracker.data.ExemptionStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.prayer_exemption
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Same switch as Settings, on Home as a quick action. */
@Composable
fun ExemptionControl(atTop: Boolean = false) {
    val exemption = koinInject<ExemptionStore>()
    val on by exemption.on.collectAsState()
    var asking by remember { mutableStateOf(false) }
    // turning it on is a decision with settings behind it; turning it off is just off
    fun toggle(next: Boolean) = if (next) asking = true else exemption.end()

    // one row, so no group shell — its bottom space would double the column's own gap
    AppTile(
        leadingIcon = Lucide.Pause,
        leadingColor = AppTheme.colors.primary,
        title = stringResource(Res.string.prayer_exemption),
        subtitle = exemptionSubtitle(),
        // home shows it twice: leading while one runs, trailing when none does. Each place
        // hides rather than opts out, so the tile collapses instead of being unmounted.
        visible = atTop == on,
        trailing = { AppSwitch(on, ::toggle) },
        onClick = { toggle(!on) },
    )

    if (asking) ExemptionStartSheet(
        onStart = exemption::start,
        onDismiss = { asking = false },
    )
}
