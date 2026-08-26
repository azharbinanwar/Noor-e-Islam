package com.kodeelite.nooreislam.feature.studio.data

import androidx.compose.ui.graphics.Color
import com.kodeelite.nooreislam.core.util.asFileSize
import com.kodeelite.nooreislam.core.assets.AssetStore
import com.kodeelite.nooreislam.core.constants.AssetDirs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A studio background image: a stable [id], its [url] source, and two suggested-color lists sampled
 * from the photo — [colors] (base tones) and [onColors] (readable colors to sit on top). Pickers show
 * these so text / card colors stay matched to the image.
 */
data class StudioImage(
    val id: String,
    val url: String,
    val colors: List<Color>,    // base tones, accent-first
    val onColors: List<Color>,  // readable colors for text over the image / card
    val thumbUrl: String = url, // tiny webp for pickers; the full file only moves on download
    val sizeKb: Int = 0,        // full file weight, shown beside the download gate
) {
    val accent: Color get() = colors.first()

    /** "840 KB" or "2.4 MB", for the picker's download label. */
    val sizeLabel: String get() = sizeKb.asFileSize()

    /** On-disk name: the stable id plus the server file's own extension. */
    val fileName: String get() = "$id.${url.substringAfterLast('.', "png").substringBefore('?')}"
}

/**
 * Owns the background-image catalog and where each image loads from — local file first.
 * The catalog is server-fed — [StudioCatalogRepository] seeds it from the cached manifest and
 * refreshes it over the network. Empty until the first sync; every reader copes with that.
 */
object ImageStore {

    private val _catalog = MutableStateFlow<List<StudioImage>>(emptyList())
    val catalogFlow: StateFlow<List<StudioImage>> = _catalog.asStateFlow()

    val catalog: List<StudioImage> get() = _catalog.value

    /** The opening background: the first image, in manifest order, that is already on disk. */
    val default: StudioImage? get() = catalog.firstOrNull(::isDownloaded)

    fun update(images: List<StudioImage>) {
        _catalog.value = images
    }

    fun byUrl(url: String?): StudioImage? = url?.let { u -> catalog.firstOrNull { it.url == u } }

    /** Source to load for [id]: the downloaded file when present, the remote url otherwise. */
    fun resolve(id: String): String? = catalog.firstOrNull { it.id == id }?.let(::source)

    fun source(image: StudioImage): String =
        if (isDownloaded(image)) AssetStore.pathOf(AssetDirs.STUDIO_IMAGES, image.fileName)
        else image.url

    fun isDownloaded(image: StudioImage): Boolean =
        AssetStore.exists(AssetDirs.STUDIO_IMAGES, image.fileName)
}
