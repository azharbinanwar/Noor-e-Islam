package com.kodeelite.nooreislam.feature.tracker.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pause
import com.kodeelite.nooreislam.core.components.AppSwitch
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.datetime.formatted
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.tracker.data.ExemptionStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.alerts_paused_until
import com.kodeelite.nooreislam.resources.alerts_paused_until_you_turn_this_off
import com.kodeelite.nooreislam.resources.phone_not_silenced_until
import com.kodeelite.nooreislam.resources.phone_not_silenced_until_you_turn_this_off
import com.kodeelite.nooreislam.resources.prayer_exemption
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * Why this screen has gone quiet, and the one way out. Null unless an exemption is running and it is
 * this screen's feature that it paused — so a screen that was never paused says nothing.
 * [forFocus] picks the wording; the switch always ends the exemption, not the screen it sits on.
 */
@Composable
fun ExemptionPausedTile(forFocus: Boolean): AppTileItem? {
    val exemption = koinInject<ExemptionStore>()
    val running by exemption.running.collectAsState()
    val period = running ?: return null
    if (if (forFocus) !period.pauseFocus else !period.pauseAlerts) return null

    val dateFormat by SettingsStore.gregorianDateFormat.collectAsState()
    // the last exempt day is stored, so normal life resumes the morning after
    val resumes = period.endDate?.plus(1, DateTimeUnit.DAY)?.formatted(dateFormat)

    return AppTileItem(
        leadingIcon = Lucide.Pause,
        title = stringResource(Res.string.prayer_exemption),
        subtitle = when {
            resumes != null && forFocus -> stringResource(Res.string.phone_not_silenced_until, resumes)
            resumes != null -> stringResource(Res.string.alerts_paused_until, resumes)
            forFocus -> stringResource(Res.string.phone_not_silenced_until_you_turn_this_off)
            else -> stringResource(Res.string.alerts_paused_until_you_turn_this_off)
        },
        trailing = { AppSwitch(true, { exemption.end() }) },
        onClick = { exemption.end() },
    )
}
