package com.kodeelite.nooreislam.feature.tracker.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pause
import com.kodeelite.nooreislam.core.components.AppSwitch
import com.kodeelite.nooreislam.core.components.AppTile
import com.kodeelite.nooreislam.core.components.AppTileVariant
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.tracker.data.ExemptionStore
import com.kodeelite.nooreislam.feature.tracker.data.TrackerStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.prayer_exemption
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Same switch as Settings, on Home as a quick action. */
@Composable
fun ExemptionControl() {
    val exemption = koinInject<ExemptionStore>()
    val on by exemption.on.collectAsState()
    val running by exemption.running.collectAsState()
    val streakOn by SettingsStore.streakEnabled.collectAsState()
    val history by koinInject<TrackerStore>().history.collectAsState()
    var asking by remember { mutableStateOf(false) }
    var ending by remember { mutableStateOf(false) }
    // each end of it is a decision: when it began, and which prayer is owed again
    fun toggle(next: Boolean) = if (next) asking = true else ending = true

    // one row, so no group shell — its bottom space would double the column's own gap.
    // A running exemption colours the row rather than moving it: she already knows it is on,
    // and a tile that changes place is harder to find than one that changes colour.
    AppTile(
        leadingIcon = Lucide.Pause,
        variant = if (on) AppTileVariant.Warning else AppTileVariant.Normal,
        title = stringResource(Res.string.prayer_exemption),
        subtitle = exemptionSubtitle(),
        trailing = { AppSwitch(on, ::toggle) },
        onClick = { toggle(!on) },
    )

    if (asking) ExemptionStartSheet(
        onStart = exemption::start,
        onDismiss = { asking = false },
    )

    running?.takeIf { ending }?.let { period ->
        ExemptionEndSheet(
            period = period,
            askPrayer = streakOn || history.isNotEmpty(),
            onEnd = { exemption.end(it) },
            onDismiss = { ending = false },
        )
    }
}
