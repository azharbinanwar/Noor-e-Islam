package com.example.miqatapp.core.util

/** Native sharing for text and images. */
expect object ShareService {
    fun shareText(text: String)

    // Image sharing: takes raw bytes. UI capture is handled by the platform view host.
    fun shareImage(byteArray: ByteArray, fileName: String)
}
