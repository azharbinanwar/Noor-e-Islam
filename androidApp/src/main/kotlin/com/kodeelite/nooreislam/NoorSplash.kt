package com.kodeelite.nooreislam

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.min

// The system splash can't be relied on for this: Android caps its icon animation at 1000ms, and some
// OEM skins (Vivo's OriginOS among them) never render the animated icon at all — only the background.
// Drawing it here instead means every launch gets the full animation, at whatever length we choose.
// The system splash stays a flat green of the same colour, so the hand-off is invisible.

private const val VIEWPORT = 799f
private const val CENTER = 399.5f
private const val MOSQUE_RISE = 96f // viewport px the mosque rises from

val SplashGreen = Color(0xFF1E7D55)

// how long the whole thing is on screen, including the fade-out. The fade starts as the mosque
// settles (~1000ms) — no dead time holding a finished frame.
private const val SPLASH_TOTAL_MS = 1350L
private const val FADE_OUT_DELAY_MS = 1050
private const val FADE_OUT_MS = 300

private val PATH_RING_1 =
    "M399.5 0C620.138 0 799 178.862 799 399.5C799 620.138 620.138 799 399.5 799C178.862 799 0 620.138 0 399.5C0 178.862 178.862 0 399.5 0ZM399.2 41.3604C201.924 41.3604 42.0001 201.284 42 398.56C42.0001 595.836 201.924 755.76 399.2 755.76C596.476 755.76 756.4 595.836 756.4 398.56C756.4 201.284 596.476 41.3605 399.2 41.3604Z"
private val PATH_RING_2 =
    "M399.2 42C596.476 42.0002 756.4 201.923 756.4 399.199C756.4 596.475 596.476 756.399 399.2 756.399C201.924 756.399 42.0001 596.475 42 399.199C42.0001 201.923 201.924 42 399.2 42ZM398.9 85.2402C224.986 85.2402 84 226.226 84 400.14C84.0003 574.055 224.986 715.04 398.9 715.04C572.815 715.04 713.8 574.055 713.8 400.14C713.8 226.226 572.815 85.2407 398.9 85.2402Z"
private val PATH_RING_3 =
    "M398.9 84C572.815 84.0004 713.8 224.986 713.8 398.899C713.8 572.814 572.815 713.8 398.9 713.8C224.986 713.8 84.0003 572.814 84 398.899C84 224.985 224.986 84 398.9 84ZM398.6 125.359C248.047 125.36 126 247.406 126 397.959C126 548.511 248.047 670.559 398.6 670.56C549.152 670.56 671.2 548.511 671.2 397.959C671.2 247.406 549.151 125.359 398.6 125.359Z"
private val PATH_RING_4 =
    "M399.6 127C550.152 127 672.2 249.047 672.2 399.6C672.2 550.152 550.152 672.2 399.6 672.2C249.047 672.2 127 550.152 127 399.6C127 249.047 249.047 127 399.6 127ZM398.66 170.24C271.469 170.24 168.36 273.349 168.36 400.54C168.36 527.731 271.469 630.84 398.66 630.84C525.851 630.84 628.96 527.731 628.96 400.54C628.96 273.349 525.851 170.24 398.66 170.24Z"
private val PATH_MOSQUE =
    "M397.575 262C383.555 266.824 373.48 280.127 373.48 295.784C373.481 315.512 389.473 331.504 409.2 331.504C424.858 331.504 438.161 321.429 442.984 307.408C441.89 329.005 424.95 346.417 403.56 348.259V362.024C408.59 363.485 411.811 366.154 414.21 369.529C416.938 373.366 418.625 378.18 420.748 382.858C422.899 387.597 425.565 392.396 430.311 396.596C432.554 398.58 435.277 400.443 438.652 402.102C508.131 416.905 559.599 470.496 559.6 534.342C559.6 547.195 557.511 559.632 553.612 571.421C512.725 608.542 458.435 631.163 398.86 631.163C340.592 631.163 287.378 609.524 246.816 573.843C242.385 561.34 240 548.079 240 534.342C240 469.542 293.018 415.304 364.073 401.463C366.882 399.97 369.209 398.33 371.169 396.596C375.916 392.396 378.582 387.597 380.732 382.858C382.855 378.18 384.541 373.366 387.269 369.529C389.669 366.154 392.89 363.485 397.92 362.024V348.38C374.912 347.396 356.561 328.435 356.561 305.185C356.561 282.05 374.729 263.158 397.575 262Z"

private fun ringPath(data: String): Path =
    PathParser().parsePathString(data).toPath().apply { fillType = PathFillType.EvenOdd }

/**
 * "Rings First": the four rings ripple outward from the center building the frame, then the
 * mosque rises up into its place. [onFinished] fires once it has faded out.
 */
@Composable
fun NoorSplash(onFinished: () -> Unit) {
    val ring1 = remember { ringPath(PATH_RING_1) }
    val ring2 = remember { ringPath(PATH_RING_2) }
    val ring3 = remember { ringPath(PATH_RING_3) }
    val ring4 = remember { ringPath(PATH_RING_4) }
    val mosque = remember { PathParser().parsePathString(PATH_MOSQUE).toPath() }

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        started = true
        delay(SPLASH_TOTAL_MS)
        onFinished()
    }

    val target = if (started) 1f else 0f
    fun ripple(delayMs: Int) =
        tween<Float>(durationMillis = 350, delayMillis = delayMs, easing = FastOutSlowInEasing)

    // inner ring first, rippling outward
    val p4 by animateFloatAsState(target, ripple(0), label = "ring4")
    val p3 by animateFloatAsState(target, ripple(100), label = "ring3")
    val p2 by animateFloatAsState(target, ripple(200), label = "ring2")
    val p1 by animateFloatAsState(target, ripple(300), label = "ring1")
    val pMosque by animateFloatAsState(
        target,
        tween(500, delayMillis = 500, easing = FastOutSlowInEasing),
        label = "mosque",
    )
    val fadeOut by animateFloatAsState(
        targetValue = if (started) 0f else 1f,
        animationSpec = tween(FADE_OUT_MS, delayMillis = FADE_OUT_DELAY_MS),
        label = "fadeOut",
    )

    Box(
        Modifier.fillMaxSize().alpha(fadeOut).background(SplashGreen),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(160.dp)) {
            val s = min(size.width, size.height) / VIEWPORT
            translate(
                left = (size.width - VIEWPORT * s) / 2f,
                top = (size.height - VIEWPORT * s) / 2f,
            ) {
                scale(s, s, pivot = Offset.Zero) {
                    ring(ring1, p1, 0.6f)
                    ring(ring2, p2, 0.8f)
                    ring(ring3, p3, 0.9f)
                    ring(ring4, p4, 1f)
                    if (pMosque > 0f) translate(top = MOSQUE_RISE * (1f - pMosque)) {
                        drawPath(mosque, Color.White, alpha = pMosque)
                    }
                }
            }
        }
    }
}

// each ring scales out from the center (0.55 → 1) while fading up to its own resting opacity
private fun DrawScope.ring(path: Path, progress: Float, maxAlpha: Float) {
    if (progress <= 0f) return
    val ringScale = 0.55f + 0.45f * progress
    scale(ringScale, ringScale, pivot = Offset(CENTER, CENTER)) {
        drawPath(path, Color.White, alpha = maxAlpha * progress)
    }
}
