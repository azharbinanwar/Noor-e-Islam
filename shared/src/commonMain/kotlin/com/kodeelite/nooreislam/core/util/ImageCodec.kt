package com.kodeelite.nooreislam.core.util

import androidx.compose.ui.graphics.ImageBitmap

/** Encode a captured composable bitmap to PNG bytes for sharing / saving. */
expect fun ImageBitmap.toPngBytes(): ByteArray
