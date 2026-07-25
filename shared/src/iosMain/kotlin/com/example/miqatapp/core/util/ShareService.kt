package com.example.miqatapp.core.util

import platform.UIKit.*
import platform.Foundation.*
import kotlinx.cinterop.*

actual object ShareService {
    actual fun shareText(text: String) {
        val activityItems = listOf(text)
        val activityController = UIActivityViewController(activityItems, null)
        
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            activityController, true, null
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun shareImage(byteArray: ByteArray, fileName: String) {
        val data = byteArray.toNSData()
        val image = UIImage.imageWithData(data) ?: return
        
        val activityItems = listOf(image)
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
