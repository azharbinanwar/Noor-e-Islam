package com.kodeelite.nooreislam.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.kodeelite.nooreislam.config.theme.AppTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/** A message that shows for a moment and leaves on its own. Android has toasts; iOS has nothing, so this. */
class NoticeState {
    var current by mutableStateOf<Notice?>(null)
        private set

    fun show(
        title: String,
        message: String? = null,
        icon: ImageVector? = null,
        variant: AppTileVariant = AppTileVariant.Normal,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        dismissible: Boolean = false,
    ) {
        current = Notice(title, message, icon, variant, actionLabel, onAction, dismissible, stamp++)
    }

    fun dismiss() {
        current = null
    }

    private var stamp = 0
}

/** [stamp] makes an identical message shown twice a new value, so the timer restarts. */
class Notice(
    val title: String,
    val message: String?,
    val icon: ImageVector?,
    val variant: AppTileVariant,
    val actionLabel: String?,
    val onAction: (() -> Unit)?,
    val dismissible: Boolean,
    val stamp: Int,
)

/** Draws whatever [state] is holding. Put it once, at the top of the app, above every screen. */
@Composable
fun BoxScope.AppNotice(state: NoticeState, millis: Long = 3000) {
    val notice = state.current
    val c = AppTheme.colors
    // kept after the state clears, so the exit animation still has something to draw
    var last by remember { mutableStateOf(notice) }
    if (notice != null) last = notice

    LaunchedEffect(notice?.stamp) {
        if (notice != null) {
            delay(millis.milliseconds)
            state.dismiss()
        }
    }

    AnimatedVisibility(
        visible = notice != null,
        modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp),
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
    ) {
        val shown = last ?: return@AnimatedVisibility
        val accent = shown.variant.accentOf(c)
        // opaque: the tint is mixed into the card colour, never laid over the screen as alpha
        val bg = if (shown.variant == AppTileVariant.Normal) c.cardColor else lerp(c.cardColor, accent, 0.14f)
        // a hairline instead of elevation: a Compose shadow draws at full strength from the first
        // frame, so it lands after the slide rather than travelling with it
        val edge = if (shown.variant == AppTileVariant.Normal) c.outlineVariant.copy(alpha = 0.5f) else accent.copy(alpha = 0.35f)
        Row(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(bg)
                .border(1.dp, edge, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (shown.icon != null) Box(
                Modifier.size(36.dp).clip(CircleShape).background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(shown.icon, null, tint = accent, modifier = Modifier.size(19.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    shown.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = c.onSurface,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (shown.message != null) Text(
                    shown.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (shown.actionLabel != null && shown.onAction != null) AppButton(
                shown.actionLabel,
                onClick = { state.dismiss(); shown.onAction.invoke() },
                size = AppButtonSize.Small,
                variant = AppButtonVariant.Text,
            )
            if (shown.dismissible) Icon(
                Lucide.X,
                null,
                tint = c.onSurfaceVariant,
                modifier = Modifier.clip(CircleShape).clickable { state.dismiss() }.size(18.dp),
            )
        }
    }
}

/** Reach the host's notice from any screen. */
val LocalNotice = staticCompositionLocalOf<NoticeState> { error("LocalNotice not provided") }
