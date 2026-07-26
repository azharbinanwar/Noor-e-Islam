package com.kodeelite.nooreislam.feature.studio.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.ArrowDownLeft
import com.composables.icons.lucide.ArrowDownRight
import com.composables.icons.lucide.ArrowUpLeft
import com.composables.icons.lucide.ArrowUpRight
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.core.locale.tr
import kotlinx.serialization.Serializable

// Studio model enums. Labels are plain for now; a localization pass comes later.
// @Serializable so they persist by name inside a saved StudioConfig.

// Corners are start/end-relative (placement uses Alignment.TopStart/End), so the icon mirrors in RTL to match.
@Serializable
enum class LogoCorner(private val ltrIcon: ImageVector, private val rtlIcon: ImageVector) {
    TopLeft(Lucide.ArrowUpLeft, Lucide.ArrowUpRight),
    TopRight(Lucide.ArrowUpRight, Lucide.ArrowUpLeft),
    BottomLeft(Lucide.ArrowDownLeft, Lucide.ArrowDownRight),
    BottomRight(Lucide.ArrowDownRight, Lucide.ArrowDownLeft),
    ;

    val icon: ImageVector @Composable get() = tr(ltrIcon, rtlIcon)

    companion object {
        val DEFAULT = BottomRight
    }
}

@Serializable
enum class StudioAspectRatio(val label: String, val ratio: Float?) {
    Full("Full", null),         // fills the screen
    Original("Original", null), // canvas takes the loaded image's own ratio (resolved at runtime)
    Story("9:16", 9f / 16f),
    Post("4:5", 4f / 5f),
    Square("1:1", 1f),
    ;

    companion object {
        val DEFAULT = Full
    }
}

@Serializable
enum class CanvasPattern {
    None, Grain, Geometric;

    companion object {
        val DEFAULT = None
    }
}
