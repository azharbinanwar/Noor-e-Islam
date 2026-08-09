package com.kodeelite.nooreislam.core.util

/** Saves an image to the device photo gallery. Returns true on success (permission granted + written). */
expect object GalleryService {
    // folderName: Android files under Pictures/<folderName>; iOS has no named-folder equivalent, ignores it
    suspend fun saveImage(bytes: ByteArray, fileName: String, folderName: String): Boolean
}
