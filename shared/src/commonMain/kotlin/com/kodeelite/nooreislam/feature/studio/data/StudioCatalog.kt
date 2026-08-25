package com.kodeelite.nooreislam.feature.studio.data

import androidx.compose.ui.graphics.Color
import com.kodeelite.nooreislam.core.constants.AppConst
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
        url = absolute(url),
        colors = colors.mapNotNull(::parseHex).ifEmpty { listOf(Color.Black) },
        onColors = onColors.mapNotNull(::parseHex).ifEmpty { listOf(Color.White) },
        thumbUrl = absolute(thumbUrl),
        sizeKb = sizeKb,
    )

    companion object {
        // media paths are root-relative, so they need the host without the /api/v1 suffix
        private val origin = Regex("^https?://[^/]+").find(AppConst.API_BASE_URL)?.value.orEmpty()

        fun absolute(path: String): String =
            if (path.startsWith("http")) path else origin + path

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
