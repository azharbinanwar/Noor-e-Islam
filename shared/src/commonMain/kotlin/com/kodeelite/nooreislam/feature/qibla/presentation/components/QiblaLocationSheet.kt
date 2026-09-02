package com.kodeelite.nooreislam.feature.qibla.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.LocateOff
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.MapPinOff
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppButtonVariant
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.feature.qibla.store.QiblaGate
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.open_settings
import com.kodeelite.nooreislam.resources.qibla_allow_location
import com.kodeelite.nooreislam.resources.qibla_continue_with
import com.kodeelite.nooreislam.resources.qibla_location_blocked
import com.kodeelite.nooreislam.resources.qibla_location_blocked_sub
import com.kodeelite.nooreislam.resources.qibla_location_off
import com.kodeelite.nooreislam.resources.qibla_location_off_sub
import com.kodeelite.nooreislam.resources.qibla_needs_location
import com.kodeelite.nooreislam.resources.qibla_needs_location_sub
import com.kodeelite.nooreislam.resources.qibla_turn_on_location
import org.jetbrains.compose.resources.stringResource

/** The sheet while the compass has no position. Its primary action is whatever is actually blocking. */
@Composable
fun QiblaLocationSheet(
    gate: QiblaGate,
    placeName: String,
    onPrimary: () -> Unit,
    onUsePlace: () -> Unit,
    onDismiss: () -> Unit,
) {
    val c = AppTheme.colors
    val icon: ImageVector = when (gate) {
        QiblaGate.Ask -> Lucide.MapPin
        QiblaGate.ServiceOff -> Lucide.LocateOff
        QiblaGate.Blocked -> Lucide.MapPinOff
    }
    val title = when (gate) {
        QiblaGate.Ask -> Res.string.qibla_needs_location
        QiblaGate.ServiceOff -> Res.string.qibla_location_off
        QiblaGate.Blocked -> Res.string.qibla_location_blocked
    }
    val message = when (gate) {
        QiblaGate.Ask -> Res.string.qibla_needs_location_sub
        QiblaGate.ServiceOff -> Res.string.qibla_location_off_sub
        QiblaGate.Blocked -> Res.string.qibla_location_blocked_sub
    }
    val action = when (gate) {
        QiblaGate.Ask -> Res.string.qibla_allow_location
        QiblaGate.ServiceOff -> Res.string.qibla_turn_on_location
        QiblaGate.Blocked -> Res.string.open_settings
    }

    AppBottomSheet(
        onDismiss = onDismiss,
        footer = {
            AppButton(stringResource(action), onPrimary, Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            AppButton(
                stringResource(Res.string.qibla_continue_with, placeName),
                onUsePlace,
                Modifier.fillMaxWidth(),
                variant = AppButtonVariant.Text,
            )
        },
    ) {
        StateView(
            title = stringResource(title),
            padding = 0.dp,
            message = stringResource(message),
            icon = {
                Box(
                    Modifier.size(72.dp).clip(CircleShape).background(c.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, null, tint = c.primary, modifier = Modifier.size(34.dp))
                }
            },
        )
    }
}
