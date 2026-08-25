package com.kodeelite.nooreislam.feature.studio.data

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

/**
 * One entry of `/studio/images` — the only studio asset the server owns; gradients and
 * templates are the app's own. Sizes ride along for the later download manager.
 */
@Serializable
data class CatalogImage(
    val id: String,
    val colors: List<String> = emptyList(),     // hex, accent first — same contract as StudioImage
    val onColors: List<String> = emptyList(),
    val url: String,                            // root-relative: /api/media/…
    val thumbUrl: String,
    val hash: String,
    val sizeKb: Int = 0,
    val thumbKb: Int = 0,
    val width: Int = 0,
    val height: Int = 0,
) {
    fun toStudioImage(): StudioImage = StudioImage(
        id = id,
        url = url,
        colors = colors.mapNotNull(::parseHex).ifEmpty { listOf(Color.Black) },
        onColors = onColors.mapNotNull(::parseHex).ifEmpty { listOf(Color.White) },
        thumbUrl = thumbUrl,
        sizeKb = sizeKb,
    )

    companion object {
        private fun parseHex(hex: String): Color? {
            val digits = hex.removePrefix("#")
            val value = digits.toLongOrNull(16) ?: return null
            return when (digits.length) {
                6 -> Color(0xFF000000L or value)
                8 -> Color(value)
                else -> null
            }
        }
    }
}
