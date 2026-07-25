package com.example.miqatapp.core.util

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette

actual object ImageColorExtractor {
    actual suspend fun extractColors(imageBytes: ByteArray): List<Color> {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size) ?: return emptyList()
        val palette = Palette.from(bitmap).generate()
        
        val colors = mutableListOf<Color>()
        palette.vibrantSwatch?.let { colors.add(Color(it.rgb)) }
        palette.darkVibrantSwatch?.let { colors.add(Color(it.rgb)) }
        palette.lightVibrantSwatch?.let { colors.add(Color(it.rgb)) }
        palette.mutedSwatch?.let { colors.add(Color(it.rgb)) }
        palette.darkMutedSwatch?.let { colors.add(Color(it.rgb)) }
        palette.lightMutedSwatch?.let { colors.add(Color(it.rgb)) }
        palette.dominantSwatch?.let { colors.add(Color(it.rgb)) }
        
        return colors.distinct()
    }
}
