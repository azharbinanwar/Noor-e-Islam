package com.kodeelite.nooreislam.core.components.colorpicker

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color

// Session-scoped recently-picked custom colors (newest first). In-memory only — cleared on process death.
object RecentColors {
    private const val MAX = 10
    private val items = mutableStateListOf<Color>()
    val colors: List<Color> get() = items

    fun add(color: Color) {
        val c = color.copy(alpha = 1f)
        items.removeAll { it.value == c.value }   // move an existing one back to the front
        items.add(0, c)
        while (items.size > MAX) items.removeAt(items.lastIndex)
    }
}
