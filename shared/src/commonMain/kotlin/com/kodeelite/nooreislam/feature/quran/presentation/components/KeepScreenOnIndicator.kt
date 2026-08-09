package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Sun
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppSwitch
import com.kodeelite.nooreislam.core.components.AppTile
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.keep_screen_on
import com.kodeelite.nooreislam.resources.screen_stays_on_hint
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import org.jetbrains.compose.resources.stringResource

// The intro pill sits exactly where AutoScrollControl's own center pill already is (same
// alignment/padding), so it simply covers it for a few seconds on landing, then gets out of the way,
// revealing what was always underneath — AutoScrollControl itself is never touched. The collapsed icon
// then parks at bottom-start, its own permanent spot, deliberately separate from the reader's
// bottom-end controls-collapse tab — sharing one spot would read as a single toggle button flipping
// between two states, when these are two unrelated controls.
@Composable
fun BoxScope.KeepScreenOnIndicator(checked: Boolean, onToggle: () -> Unit) {
    var collapsed by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(2500.milliseconds)
        collapsed = true
    }
    val colors = AppTheme.colors

    AnimatedVisibility(
        visible = !collapsed,
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
        enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it },
        exit = fadeOut(tween(220)) + slideOutVertically(tween(220)) { it },
    ) {
        Row(
            Modifier.clip(RoundedCornerShape(24.dp)).background(colors.cardColor)
                .clickable { showSheet = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Lucide.Sun, null, tint = colors.primary, modifier = Modifier.size(18.dp))
            Text(stringResource(Res.string.screen_stays_on_hint), color = colors.primary, fontSize = 13.sp)
        }
    }
    AnimatedVisibility(
        visible = collapsed,
        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
        enter = fadeIn(tween(220)),
        exit = fadeOut(tween(220)),
    ) {
        Box(
            Modifier.size(44.dp).clip(CircleShape).background(colors.cardColor).clickable { showSheet = true },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Lucide.Sun, null, tint = colors.primary, modifier = Modifier.size(18.dp))
        }
    }

    if (showSheet) {
        AppBottomSheet(onDismiss = { showSheet = false }, title = stringResource(Res.string.keep_screen_on), scrimAlpha = 0f) {
            AppTile(
                title = stringResource(Res.string.keep_screen_on),
                leadingIcon = Lucide.Sun,
                trailing = { AppSwitch(checked = checked, onCheckedChange = { onToggle() }) },
            )
        }
    }
}
