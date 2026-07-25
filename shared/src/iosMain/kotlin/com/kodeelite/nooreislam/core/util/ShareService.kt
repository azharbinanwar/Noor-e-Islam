package com.kodeelite.nooreislam.core.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.data
import platform.Foundation.dataWithBytes
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage

actual object ShareService {
    actual fun shareText(text: String) {
        val activityItems = listOf(text)
        val activityController = UIActivityViewController(activityItems, null)

        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            activityController, true, null
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun shareImage(byteArray: ByteArray, fileName: String, caption: String) {
        val data = byteArray.toNSData()
        val image = UIImage.imageWithData(data) ?: return

        val activityItems = if (caption.isNotBlank()) listOf(image, caption) else listOf(image)
        val activityController = UIActivityViewController(activityItems, null)

        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            activityController, true, null
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun ByteArray.toNSData(): NSData {
        if (isEmpty()) return NSData.data()
        return usePinned {
            NSData.dataWithBytes(it.addressOf(0), size.toULong())
        }
    }
}
