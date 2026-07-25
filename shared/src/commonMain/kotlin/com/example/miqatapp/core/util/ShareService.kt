package com.example.miqatapp.core.util

/** Native sharing for text and images. */
expect object ShareService {
    fun shareText(text: String)

    // Image sharing: raw bytes + an optional caption (ayah text, reference, app mention) sent alongside.
    fun shareImage(byteArray: ByteArray, fileName: String, caption: String)
}
