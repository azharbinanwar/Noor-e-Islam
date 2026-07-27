package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.feature.quran.data.Ayah
import com.kodeelite.nooreislam.feature.quran.data.HighlightColor
import com.kodeelite.nooreislam.feature.quran.data.HighlightRepository
import com.kodeelite.nooreislam.feature.quran.data.hue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

// long-press → floating vertical color strip on the right; auto-dismiss after 2s (reset on each pick), tap-away closes.
@Composable
fun HighlightQuickPicker(ayah: Ayah, onDismiss: () -> Unit) {
    val repo = koinInject<HighlightRepository>()
    val scope = rememberCoroutineScope()
    val current by repo.colorOf(ayah.surah, ayah.ayah).collectAsState(null)
    var tick by remember { mutableStateOf(0) } // bump to restart the 2s countdown
    LaunchedEffect(tick) { delay(2000); onDismiss() }

    // no full-screen catcher — the page stays scrollable; the strip just auto-dismisses on the timer
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.align(Alignment.CenterEnd).padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // reserved slot so the color dots don't jump when the remove dot appears/disappears
            Box(Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                if (current != null) Dot(
                    AppTheme.colors.surfaceVariant,
                    ring = false,
                    icon = Lucide.Minus,
                    iconTint = AppTheme.colors.onSurfaceVariant
                ) {
                    scope.launch { repo.remove(ayah.surah, ayah.ayah) }; onDismiss()
                }
            }
            HighlightColor.entries.forEachIndexed { i, c ->
                // slide in from the right, staggered, with a bouncy spring
                val anim = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    delay(i * 55L)
                    anim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                }
                Dot(
                    c.hue, ring = c == current, icon = Lucide.Check.takeIf { c == current }, iconTint = Color.White,
                    modifier = Modifier.graphicsLayer { translationX = (1f - anim.value) * 90f; alpha = anim.value.coerceIn(0f, 1f) },
                ) { scope.launch { repo.set(ayah.surah, ayah.ayah, c) }; tick++ }
            }
        }
    }
}

@Composable
private fun Dot(color: Color, ring: Boolean, icon: ImageVector?, iconTint: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.size(38.dp).clip(CircleShape).background(color)
            // always outlined so a dot never merges with the page; selected gets a bolder ring
            .border(if (ring) 2.5.dp else 1.5.dp, if (ring) AppTheme.colors.onSurface else AppTheme.colors.outline, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
    }
}
