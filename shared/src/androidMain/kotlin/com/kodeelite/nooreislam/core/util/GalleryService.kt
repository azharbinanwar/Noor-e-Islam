package com.kodeelite.nooreislam.core.util

import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import com.kodeelite.nooreislam.core.platform.AppCtx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual object GalleryService {
    // API 29+ writes to Pictures/<folderName> via MediaStore (no permission). Pre-29 falls back to a plain insert,
    // which needs WRITE_EXTERNAL_STORAGE (declared maxSdkVersion=28 in the manifest).
    actual suspend fun saveImage(bytes: ByteArray, fileName: String, folderName: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = AppCtx.context.contentResolver
            val q = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$folderName")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return@runCatching false
            resolver.openOutputStream(uri)?.use { it.write(bytes) } ?: return@runCatching false
            if (q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            true
        }.getOrDefault(false)
    }
}
