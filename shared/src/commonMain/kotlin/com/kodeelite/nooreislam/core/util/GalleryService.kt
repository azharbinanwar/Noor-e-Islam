package com.kodeelite.nooreislam.core.util

/** Saves an image to the device photo gallery. Returns true on success (permission granted + written). */
expect object GalleryService {
    suspend fun saveImage(bytes: ByteArray, fileName: String): Boolean
}
