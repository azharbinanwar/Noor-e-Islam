package com.kodeelite.nooreislam.core.util

import android.content.ContentValues
import android.provider.MediaStore
import com.kodeelite.nooreislam.core.platform.AppCtx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual object GalleryService {
    // Writes to Pictures/<folderName> via MediaStore, which needs no permission from API 29 (our minSdk).
    actual suspend fun saveImage(bytes: ByteArray, fileName: String, folderName: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = AppCtx.context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$folderName")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return@runCatching false
            resolver.openOutputStream(uri)?.use { it.write(bytes) } ?: return@runCatching false
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            true
        }.getOrDefault(false)
    }
}
