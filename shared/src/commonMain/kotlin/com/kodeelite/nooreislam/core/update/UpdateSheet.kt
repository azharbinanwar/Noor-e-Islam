package com.kodeelite.nooreislam.core.update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowDownToLine
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Sparkles
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppButtonVariant
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.a_new_version_is_available
import com.kodeelite.nooreislam.resources.not_now
import com.kodeelite.nooreislam.resources.update
import com.kodeelite.nooreislam.resources.update_now_for_the_latest_fixes
import com.kodeelite.nooreislam.resources.update_row_improvements
import com.kodeelite.nooreislam.resources.update_row_improvements_sub
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** A newer version is on the store — same shape as the permission sheets: badge, why, rows, act. */
@Composable
fun UpdateSheet(onUpdate: () -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    AppBottomSheet(
        onDismiss = onDismiss,
        footer = {
            AppButton(stringResource(Res.string.update), onUpdate, Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            AppButton(stringResource(Res.string.not_now), onDismiss, Modifier.fillMaxWidth(), variant = AppButtonVariant.Text)
        },
    ) {
        StateView(
            title = stringResource(Res.string.a_new_version_is_available),
            padding = 0.dp,
            message = stringResource(Res.string.update_now_for_the_latest_fixes),
            icon = {
                Box(
                    Modifier.size(72.dp).clip(CircleShape).background(c.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Lucide.ArrowDownToLine, null, tint = c.primary, modifier = Modifier.size(34.dp))
                }
            },
        )
        Column(
            Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp)
                .clip(RoundedCornerShape(16.dp)).background(c.surfaceVariant).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // one honest row: the app cannot know what an update holds, so it never promises features
            UpdateRow(Lucide.Sparkles, Res.string.update_row_improvements, Res.string.update_row_improvements_sub)
        }
    }
}

@Composable
private fun UpdateRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: StringResource, subtitle: StringResource) {
    val c = AppTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(24.dp).clip(CircleShape).background(c.primary.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = c.primary, modifier = Modifier.size(13.dp))
        }
        Column(Modifier.padding(start = 12.dp)) {
            Text(stringResource(title), fontSize = 15.sp, fontWeight = FontWeight.Medium, color = c.onSurface)
            Text(stringResource(subtitle), fontSize = 12.sp, color = c.onSurfaceVariant)
        }
    }
}
