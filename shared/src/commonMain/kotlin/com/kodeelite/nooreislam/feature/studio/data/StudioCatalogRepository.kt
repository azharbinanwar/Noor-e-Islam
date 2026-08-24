package com.kodeelite.nooreislam.feature.studio.data

import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import com.kodeelite.nooreislam.core.assets.AssetStore
import com.kodeelite.nooreislam.core.constants.AssetDirs
import com.kodeelite.nooreislam.core.constants.PrefConst
import com.kodeelite.nooreislam.core.network.ApiClient
import com.kodeelite.nooreislam.core.network.ApiResult
import com.kodeelite.nooreislam.core.network.dataOrNull
import com.kodeelite.nooreislam.core.network.getResult
import com.kodeelite.nooreislam.core.platform.imageContext
import com.kodeelite.nooreislam.core.prefs.PrefsService
import kotlinx.serialization.json.Json

/**
 * The single door to the server's studio catalog. Offline-first: the last good manifest is kept
 * in prefs and seeds [ImageStore] on startup; [refresh] replaces both when the network answers.
 * A failed refresh changes nothing — the bundled list or the cached one keeps working.
 */
class StudioCatalogRepository(private val client: ApiClient) {

    private val json = Json { ignoreUnknownKeys = true }

    init {
        cached()?.let(::push)
    }

    suspend fun refresh(): ApiResult<List<CatalogImage>> =
        client.getResult<List<CatalogImage>>("studio/images").also { result ->
            result.dataOrNull()?.let { fresh ->
                PrefsService.putString(PrefConst.STUDIO_IMAGES, json.encodeToString(fresh))
                push(fresh)
                AssetStore.cleanUp(AssetDirs.STUDIO_IMAGES, ImageStore.catalog.map { it.fileName }.toSet())
                warmThumbs()
            }
        }

    /** Thumbs land in Coil's disk cache at sync time, so the picker has previews offline. */
    private fun warmThumbs() {
        val context = imageContext()
        val loader = SingletonImageLoader.get(context)
        ImageStore.catalog.forEach { image ->
            loader.enqueue(ImageRequest.Builder(context).data(image.thumbUrl).build())
        }
    }

    private fun cached(): List<CatalogImage>? =
        PrefsService.getStringOrNull(PrefConst.STUDIO_IMAGES)?.let {
            runCatching { json.decodeFromString<List<CatalogImage>>(it) }.getOrNull()
        }

    private fun push(images: List<CatalogImage>) =
        ImageStore.update(images.map { it.toStudioImage() })
}
