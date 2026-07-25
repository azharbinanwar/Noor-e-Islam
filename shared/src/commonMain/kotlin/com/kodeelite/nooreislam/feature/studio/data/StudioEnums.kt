package com.kodeelite.nooreislam.feature.studio.data

import kotlinx.serialization.Serializable

// Studio model enums. Labels are plain for now; a localization pass comes later.
// @Serializable so they persist by name inside a saved StudioConfig.

@Serializable
enum class LogoCorner { TopLeft, TopRight, BottomLeft, BottomRight }

@Serializable
enum class StudioAspectRatio(val label: String, val ratio: Float?) {
    Full("Full", null),         // fills the screen
    Original("Original", null), // canvas takes the loaded image's own ratio (resolved at runtime)
    Story("9:16", 9f / 16f),
    Post("4:5", 4f / 5f),
    Square("1:1", 1f),
}

@Serializable
enum class CanvasPattern { None, Grain, Geometric }
