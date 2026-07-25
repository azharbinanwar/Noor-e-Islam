package com.example.miqatapp.core.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.data
import platform.Foundation.dataWithBytes
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIImage
import kotlin.coroutines.resume

actual object GalleryService {
    // performChanges triggers the add-only photo permission prompt on first use.
    // Requires NSPhotoLibraryAddUsageDescription in Info.plist.
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun saveImage(bytes: ByteArray, fileName: String): Boolean {
        val image = UIImage.imageWithData(bytes.toNSData()) ?: return false
        return suspendCancellableCoroutine { cont ->
            PHPhotoLibrary.sharedPhotoLibrary().performChanges({
                PHAssetChangeRequest.creationRequestForAssetFromImage(image)
            }, { success, _ -> cont.resume(success) })
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun ByteArray.toNSData(): NSData {
        if (isEmpty()) return NSData.data()
        return usePinned { NSData.dataWithBytes(it.addressOf(0), size.toULong()) }
    }
}
