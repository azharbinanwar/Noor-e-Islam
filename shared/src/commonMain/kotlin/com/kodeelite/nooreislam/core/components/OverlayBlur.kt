package com.kodeelite.nooreislam.core.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.kodeelite.nooreislam.config.theme.AppTheme
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
    val target = if (AppTheme.colors.isDark) OverlayStyle.blurDark else OverlayStyle.blurLight
    val radius by animateDpAsState(if (alsoWhen || overlay.sheetCount > 0) target else 0.dp, label = "blur")
    return this.blur(radius)
}

/**
 * How much the app behind a sheet or the drawer is blurred and dimmed, per theme. Mutable only so the
 * Sandbox's sheet lab can try values live; the numbers here are the shipped ones.
 */
object OverlayStyle {
    var blurLight by mutableStateOf(5.dp)
    var blurDark by mutableStateOf(5.dp)
    var scrimLight by mutableStateOf(0.15f)
    var scrimDark by mutableStateOf(0.05f)

    val scrim: Float @Composable get() = if (AppTheme.colors.isDark) scrimDark else scrimLight
}

/**
 * Hosts the app content where there's no drawer to host it. [AppDrawer] blurs inside its own content
 * slot so the panel stays sharp; editions without a drawer use this instead.
 */
@Composable
fun AppContentHost(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().overlayBlur()) { content() }
}
