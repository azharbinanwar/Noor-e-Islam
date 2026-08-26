package com.kodeelite.nooreislam.core.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.composables.icons.lucide.BatteryCharging
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppButtonVariant
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.battery_steps_title
import com.kodeelite.nooreislam.resources.battery_steps_why
import com.kodeelite.nooreislam.resources.not_now
import com.kodeelite.nooreislam.resources.continue_label
import org.jetbrains.compose.resources.stringResource

/**
 * Settings will open on a screen whose wording is the phone maker's, not Android's, and the option that
 * matters is rarely the first one. Say what to look for before she gets there.
 */
@Composable
fun BatteryStepsSheet(maker: String, onOpenSettings: () -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    AppBottomSheet(
        onDismiss = onDismiss,
        footer = {
            AppButton(stringResource(Res.string.continue_label), onOpenSettings, Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            AppButton(stringResource(Res.string.not_now), onDismiss, Modifier.fillMaxWidth(), variant = AppButtonVariant.Text)
        },
    ) {
        StateView(
            title = stringResource(Res.string.battery_steps_title),
            padding = 0.dp,
            message = stringResource(Res.string.battery_steps_why),
            icon = {
                Box(
                    Modifier.size(72.dp).clip(CircleShape).background(c.warning.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Lucide.BatteryCharging, null, tint = c.warning, modifier = Modifier.size(34.dp))
                }
            },
        )
        Column(
            Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp)
                .clip(RoundedCornerShape(16.dp)).background(c.surfaceVariant).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            batterySteps(maker).forEachIndexed { i, step ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(24.dp).clip(CircleShape).background(c.warning.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("${i + 1}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.warning)
                    }
                    Text(stringResource(step), fontSize = 15.sp, color = c.onSurface, modifier = Modifier.padding(start = 12.dp))
                }
            }
        }
    }
}
