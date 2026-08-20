package com.kodeelite.nooreislam.core.permissions

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
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.MapPinOff
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppButtonVariant
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.cancel
import com.kodeelite.nooreislam.resources.open_settings
import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.displayName
import com.kodeelite.nooreislam.resources.location_permission_needed
import com.kodeelite.nooreislam.resources.location_permission_rationale
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Location is blocked in system settings — explain, then route there. */
@Composable
fun LocationDeniedSheet(onOpenSettings: () -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    AppBottomSheet(
        onDismiss = onDismiss,
        footer = {
            AppButton(stringResource(Res.string.open_settings), onOpenSettings, Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            AppButton(stringResource(Res.string.cancel), onDismiss, Modifier.fillMaxWidth(), variant = AppButtonVariant.Text)
        },
    ) {
        StateView(
            title = stringResource(Res.string.location_permission_needed),
            padding = 0.dp,
            message = stringResource(Res.string.location_permission_rationale, koinInject<AppEdition>().displayName()),
            icon = {
                Box(
                    Modifier.size(72.dp).clip(CircleShape).background(c.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Lucide.MapPinOff, null, tint = c.primary, modifier = Modifier.size(34.dp))
                }
            },
        )
    }
}
