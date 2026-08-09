package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.ChevronsDown
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Plus
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.locale.tr
import com.kodeelite.nooreislam.feature.quran.data.QuranStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.auto_scroll
import com.kodeelite.nooreislam.resources.pause
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

// floating auto-scroll control: a center "Auto scroll" pill while off; once turned on it grows into
// [-][icon+text][+], still centered — only then does it shrink to a small trailing-edge tab after a couple
// idle seconds (freeing the screen while reading continues on its own). Tapping the tab re-expands it back
// to center. While off, a manual drag just hides the pill for the duration of that drag.
@Composable
fun BoxScope.AutoScrollControl(
    isScrollInProgress: Boolean,
    onToggle: () -> Unit,
    onSpeedDown: () -> Unit,
    onSpeedUp: () -> Unit,
) {
    val store = koinInject<QuranStore>()
    val enabled by store.autoScrollEnabled.collectAsState()
    val speed by store.autoScrollSpeed.collectAsState()
    var collapsed by remember { mutableStateOf(false) }
    var tick by remember { mutableStateOf(0) } // bumped on every interaction to restart the collapse timer
    LaunchedEffect(enabled) { if (enabled) collapsed = false }
    LaunchedEffect(enabled, collapsed, tick) {
        if (enabled && !collapsed) {
            delay(2500.milliseconds)
            collapsed = true
        }
    }

    // briefly swaps the pill's label for the speed value after +/- so the tap has visible feedback
    var showSpeedHint by remember { mutableStateOf(false) }
    LaunchedEffect(showSpeedHint) {
        if (showSpeedHint) {
            delay(1200.milliseconds)
            showSpeedHint = false
        }
    }

    val manualScrolling = !enabled && isScrollInProgress
    AnimatedVisibility(
        visible = !manualScrolling && (!enabled || !collapsed),
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
        enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it },
        exit = fadeOut(tween(220)) + slideOutVertically(tween(220)) { it },
    ) {
        CenterPill(
            running = enabled,
            speed = speed,
            showSpeedHint = showSpeedHint,
            onToggle = { onToggle(); tick++ },
            onSpeedDown = { onSpeedDown(); tick++; showSpeedHint = true },
            onSpeedUp = { onSpeedUp(); tick++; showSpeedHint = true },
        )
    }
    AnimatedVisibility(
        visible = enabled && collapsed,
        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it },
        exit = fadeOut(tween(220)) + slideOutVertically(tween(220)) { it },
    ) {
        val colors = AppTheme.colors
        DockButton(onClick = { collapsed = false; tick++ }, shape = CircleShape) {
            Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), null, tint = colors.primary)
        }
    }
}

@Composable
private fun CenterPill(
    running: Boolean,
    speed: Int,
    showSpeedHint: Boolean,
    onToggle: () -> Unit,
    onSpeedDown: () -> Unit,
    onSpeedUp: () -> Unit,
) {
    val colors = AppTheme.colors
    // measured once from the real "Pause" string (locale-correct — Arabic runs much wider than English) and
    // then held as a floor, so the speed hint ("Nx") can never shrink the pill smaller than "Pause" was
    var pauseTextWidthPx by remember { mutableStateOf(0) }
    val pauseTextWidth = with(LocalDensity.current) { pauseTextWidthPx.toDp() }
    Row(verticalAlignment = Alignment.CenterVertically) {
        AnimatedVisibility(visible = running, enter = fadeIn(tween(220)), exit = fadeOut(tween(220))) {
            Box(Modifier.padding(end = 10.dp)) {
                DockButton(onSpeedDown, CircleShape) { Icon(Lucide.Minus, null, tint = colors.onSurface) }
            }
        }
        Row(
            // animates the pill's own width smoothly whenever the label/icon inside changes size —
            // covers "Auto scroll" <-> "Pause" <-> "Nx" the same way, no manual width bookkeeping
            Modifier.clip(RoundedCornerShape(24.dp)).background(colors.cardColor).clickable(onClick = onToggle)
                .animateContentSize(tween(220))
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(if (running) Lucide.Pause else Lucide.ChevronsDown, null, tint = colors.primary)
            Text(
                if (showSpeedHint) "${speed}x" else if (running) stringResource(Res.string.pause) else stringResource(Res.string.auto_scroll),
                color = colors.primary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .widthIn(min = pauseTextWidth)
                    .onSizeChanged { if (running && !showSpeedHint) pauseTextWidthPx = it.width },
            )
        }
        AnimatedVisibility(visible = running, enter = fadeIn(tween(220)), exit = fadeOut(tween(220))) {
            Box(Modifier.padding(start = 10.dp)) {
                DockButton(onSpeedUp, CircleShape) { Icon(Lucide.Plus, null, tint = colors.onSurface) }
            }
        }
    }
}

@Composable
private fun DockButton(onClick: () -> Unit, shape: RoundedCornerShape, content: @Composable () -> Unit) {
    val colors = AppTheme.colors
    Box(Modifier.size(44.dp).clip(shape).background(colors.cardColor), contentAlignment = Alignment.Center) {
        IconButton(onClick, content = content)
    }
}
