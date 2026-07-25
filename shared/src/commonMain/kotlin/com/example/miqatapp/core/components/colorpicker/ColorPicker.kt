package com.example.miqatapp.core.components.colorpicker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miqatapp.config.theme.AppTheme

/**
 * Generic, stateless HSV color picker: saturation-value square + hue bar (+ optional alpha bar) + preview.
 * Pure value + callback, so it drops into any layout. Fires [onColorChange] live while dragging.
 */
@Composable
fun ColorPicker(
    color: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
    showAlpha: Boolean = false,
) {
    var hsv by remember { mutableStateOf(HsvColor.from(color)) }
    // re-seed if the caller pushes a genuinely different color (e.g. a preset tap)
    LaunchedEffect(color) { if (hsv.toColor() != color) hsv = HsvColor.from(color) }
    fun set(next: HsvColor) { hsv = next; onColorChange(next.toColor()) }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SaturationValuePanel(hsv, Modifier.fillMaxWidth().height(180.dp)) { s, v -> set(hsv.copy(saturation = s, value = v)) }
        HueBar(hsv.hue, Modifier.fillMaxWidth().height(22.dp)) { h -> set(hsv.copy(hue = h)) }
        if (showAlpha) AlphaBar(hsv, Modifier.fillMaxWidth().height(22.dp)) { a -> set(hsv.copy(alpha = a)) }
        PreviewRow(hsv.toColor())
    }
}

@Composable
private fun SaturationValuePanel(hsv: HsvColor, modifier: Modifier, onChange: (Float, Float) -> Unit) {
    val hueColor = Color.hsv(hsv.hue, 1f, 1f)
    Box(
        modifier.clip(RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectTapGestures { onChange((it.x / size.width).coerceIn(0f, 1f), (1f - it.y / size.height).coerceIn(0f, 1f)) }
            }
            .pointerInput(Unit) {
                detectDragGestures { c, _ -> onChange((c.position.x / size.width).coerceIn(0f, 1f), (1f - c.position.y / size.height).coerceIn(0f, 1f)) }
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.horizontalGradient(listOf(Color.White, hueColor)))
            drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
            val cx = hsv.saturation * size.width
            val cy = (1f - hsv.value) * size.height
            drawCircle(Color.Black.copy(alpha = 0.4f), radius = 9.dp.toPx(), center = Offset(cx, cy), style = Stroke(width = 3.dp.toPx()))
            drawCircle(Color.White, radius = 8.dp.toPx(), center = Offset(cx, cy), style = Stroke(width = 2.dp.toPx()))
        }
    }
}

@Composable
private fun HueBar(hue: Float, modifier: Modifier, onChange: (Float) -> Unit) {
    val hues = remember { List(7) { Color.hsv((it * 60f) % 360f, 1f, 1f) } }
    Box(
        modifier.clip(RoundedCornerShape(50))
            .pointerInput(Unit) { detectTapGestures { onChange((it.x / size.width * 360f).coerceIn(0f, 360f)) } }
            .pointerInput(Unit) { detectDragGestures { c, _ -> onChange((c.position.x / size.width * 360f).coerceIn(0f, 360f)) } }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.horizontalGradient(hues))
            val r = size.height / 2f
            val x = ((hue / 360f) * size.width).coerceIn(r, size.width - r)
            drawCircle(Color.White, radius = r, center = Offset(x, r), style = Stroke(width = 3.dp.toPx()))
        }
    }
}

@Composable
private fun AlphaBar(hsv: HsvColor, modifier: Modifier, onChange: (Float) -> Unit) {
    val opaque = hsv.copy(alpha = 1f).toColor()
    Box(
        modifier.clip(RoundedCornerShape(50))
            .pointerInput(Unit) { detectTapGestures { onChange((it.x / size.width).coerceIn(0f, 1f)) } }
            .pointerInput(Unit) { detectDragGestures { c, _ -> onChange((c.position.x / size.width).coerceIn(0f, 1f)) } }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val cell = size.height / 2f
            var i = 0
            var x = 0f
            while (x < size.width) {
                var j = 0
                var y = 0f
                while (y < size.height) {
                    drawRect(if ((i + j) % 2 == 0) Color(0xFFBDBDBD) else Color.White, topLeft = Offset(x, y), size = Size(cell, cell))
                    j++; y += cell
                }
                i++; x += cell
            }
            drawRect(Brush.horizontalGradient(listOf(Color.Transparent, opaque)))
            val r = size.height / 2f
            val tx = (hsv.alpha * size.width).coerceIn(r, size.width - r)
            drawCircle(Color.White, radius = r, center = Offset(tx, r), style = Stroke(width = 3.dp.toPx()))
        }
    }
}

@Composable
private fun PreviewRow(color: Color) {
    val colors = AppTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(color)
                .border(1.dp, colors.onSurface.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
        )
        Text(color.toHex(), color = colors.onSurface, fontSize = 13.sp)
    }
}
