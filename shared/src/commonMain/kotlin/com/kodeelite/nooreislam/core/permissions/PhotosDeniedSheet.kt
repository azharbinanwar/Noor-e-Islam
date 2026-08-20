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
import com.composables.icons.lucide.Images
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
import com.kodeelite.nooreislam.resources.allow_x_to_add_photos_in_settings
import com.kodeelite.nooreislam.resources.photos_access_blocked
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.stringResource

/** Photo access is blocked in system settings — explain, then route there. */
@Composable
fun PhotosDeniedSheet(onOpenSettings: () -> Unit, onDismiss: () -> Unit) {
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
            title = stringResource(Res.string.photos_access_blocked),
            padding = 0.dp,
            message = stringResource(Res.string.allow_x_to_add_photos_in_settings, koinInject<AppEdition>().displayName()),
            icon = {
                Box(
                    Modifier.size(72.dp).clip(CircleShape).background(c.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Lucide.Images, null, tint = c.primary, modifier = Modifier.size(34.dp))
                }
            },
        )
    }
}
