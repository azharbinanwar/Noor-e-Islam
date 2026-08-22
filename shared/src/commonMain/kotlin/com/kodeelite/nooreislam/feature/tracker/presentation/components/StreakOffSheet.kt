package com.kodeelite.nooreislam.feature.tracker.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.SquareCheck
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppButtonVariant
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.cancel
import com.kodeelite.nooreislam.resources.everything_comes_back_when_you_switch_it_on
import com.kodeelite.nooreislam.resources.it_will_not_pause_anything
import com.kodeelite.nooreislam.resources.no_marking_no_streak_no_percentage
import com.kodeelite.nooreislam.resources.nothing_you_logged_is_deleted
import com.kodeelite.nooreislam.resources.pause_with_an_exemption
import com.kodeelite.nooreislam.resources.streak_off_does_not_pause_body
import com.kodeelite.nooreislam.resources.the_tracker_goes_away
import com.kodeelite.nooreislam.resources.turn_off_the_streak_q
import com.kodeelite.nooreislam.resources.turn_the_streak_off
import com.kodeelite.nooreislam.resources.your_prayers_are_kept
import org.jetbrains.compose.resources.stringResource

/**
 * Shown when the streak is switched off. Someone reaching for that switch during her period wants
 * the exemption, so offer it first — and say plainly that the switch keeps her prayers either way.
 */
@Composable
fun StreakOffSheet(
    // one already running means the offer is moot — she is asking for something else
    exempt: Boolean,
    onExemption: () -> Unit,
    onTurnOff: () -> Unit,
    onDismiss: () -> Unit,
) {
    val c = AppTheme.colors

    AppBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(Res.string.turn_off_the_streak_q),
        subtitle = stringResource(Res.string.nothing_you_logged_is_deleted),
        footer = {
            if (!exempt) {
                AppButton(
                    stringResource(Res.string.pause_with_an_exemption),
                    onClick = { onExemption(); onDismiss() },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
            }
            AppButton(
                stringResource(Res.string.turn_the_streak_off),
                onClick = { onTurnOff(); onDismiss() },
                variant = if (exempt) AppButtonVariant.Primary else AppButtonVariant.Outline,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            AppButton(
                stringResource(Res.string.cancel),
                onClick = onDismiss,
                variant = AppButtonVariant.Text,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        InfoLine(
            Lucide.SquareCheck,
            stringResource(Res.string.the_tracker_goes_away),
            stringResource(Res.string.no_marking_no_streak_no_percentage),
        )
        InfoLine(
            Lucide.CircleCheck,
            stringResource(Res.string.your_prayers_are_kept),
            stringResource(Res.string.everything_comes_back_when_you_switch_it_on),
        )
        if (!exempt) {
            InfoLine(
                Lucide.Pause,
                stringResource(Res.string.it_will_not_pause_anything),
                stringResource(Res.string.streak_off_does_not_pause_body),
            )
        }
    }
}

// information, not a control: a tile would invite a tap that does nothing
@Composable
private fun InfoLine(icon: ImageVector, title: String, body: String) {
    val c = AppTheme.colors
    Row(
        Modifier.fillMaxWidth().padding(bottom = 14.dp),
        // centred against the whole item, title and body together, not just the first line
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // the glyph fills its box, so its left edge lands on the sheet's text margin rather than
        // being inset by the box's own padding
        Icon(icon, null, tint = c.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                title,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = c.onSurface,
            )
            Text(body, fontSize = 12.5.sp, color = c.onSurfaceVariant, lineHeight = 18.sp)
        }
    }
}
