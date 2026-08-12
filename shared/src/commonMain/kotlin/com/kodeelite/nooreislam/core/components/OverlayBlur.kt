package com.kodeelite.nooreislam.core.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp

/** Counts open modal overlays (bottom sheets) so whatever hosts the app can blur behind them. */
class OverlayState {
    var sheetCount by mutableStateOf(0)
    var drawerGesturesEnabled by mutableStateOf(true) // a screen can switch off edge-swipe-to-open
}

val LocalOverlay = staticCompositionLocalOf<OverlayState> { error("LocalOverlay not provided") }

/**
 * Blurs the app while a sheet is open, so a sheet reads as a floating panel in every edition.
 * A sheet can't do this itself — it draws in its own overlay above the app and can't reach the
 * content behind it — so it raises [OverlayState.sheetCount] and whatever hosts the app blurs.
 * [alsoWhen] lets a host add its own state, e.g. the drawer being open.
 */
@Composable
fun Modifier.overlayBlur(alsoWhen: Boolean = false): Modifier {
    val overlay = LocalOverlay.current
    val radius by animateDpAsState(if (alsoWhen || overlay.sheetCount > 0) 18.dp else 0.dp, label = "blur")
    return this.blur(radius)
}

/**
 * Hosts the app content where there's no drawer to host it. [AppDrawer] blurs inside its own content
 * slot so the panel stays sharp; editions without a drawer use this instead.
 */
@Composable
fun AppContentHost(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().overlayBlur()) { content() }
}
