package com.kodeelite.nooreislam.core.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.kodeelite.nooreislam.core.focus.rememberFocusSetup

/** How a tile asks for the steps sheet; the sheet itself lives in [BatteryStepsHost], which is in the tree. */
fun interface BatterySteps {
    fun ask()
}

val LocalBatterySteps = staticCompositionLocalOf { BatterySteps { } }

/**
 * Tiles are built inside a list, where a sheet would never reach the composition. The host holds it
 * once, high in the tree, and any tile can raise it.
 */
@Composable
fun BatteryStepsHost(content: @Composable () -> Unit) {
    val setup = rememberFocusSetup()
    var showing by remember { mutableStateOf(false) }
    CompositionLocalProvider(LocalBatterySteps provides BatterySteps { showing = true }) {
        content()
    }
    if (showing) BatteryStepsSheet(
        maker = setup.phoneMaker(),
        onOpenSettings = { showing = false; setup.requestBatteryUnrestricted() },
        onDismiss = { showing = false },
    )
}
