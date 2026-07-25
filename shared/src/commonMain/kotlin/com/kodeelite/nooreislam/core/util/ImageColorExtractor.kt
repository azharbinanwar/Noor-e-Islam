package com.kodeelite.nooreislam.core.util

import androidx.compose.ui.graphics.Color

/** Extracts a palette of colors from an image. */
expect object ImageColorExtractor {
    /**
     * Extracts dynamic colors from the provided image bytes.
     * Returns a list of colors (Vibrant, Muted, Dominant, etc).
     */
    suspend fun extractColors(imageBytes: ByteArray): List<Color>
}
