package com.kodeelite.nooreislam.feature.tracker.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pause
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppSwitch
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.tracker.data.TrackerStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.prayer_exemption
import com.kodeelite.nooreislam.resources.skip_the_days_prayer_is_not_owed
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Same switch as Settings, on Home as a quick action. */
@Composable
fun ExemptionControl() {
    val tracker = koinInject<TrackerStore>()
    val on by SettingsStore.trackExemption.collectAsState()

    AppTileGroup(
        items = listOf(
            AppTileItem(
                leadingIcon = Lucide.Pause,
                leadingColor = AppTheme.colors.primary,
                title = stringResource(Res.string.prayer_exemption),
                subtitle = stringResource(Res.string.skip_the_days_prayer_is_not_owed),
                trailing = { AppSwitch(on, tracker::setExemption) },
                onClick = { tracker.setExemption(!on) },
            )
        )
    )
}
