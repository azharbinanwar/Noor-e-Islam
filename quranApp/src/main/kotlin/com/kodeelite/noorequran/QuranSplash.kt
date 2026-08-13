package com.kodeelite.noorequran

import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

// The system splash can't be relied on for this: Android caps its icon animation at 1000ms, and some
// OEM skins (Vivo's OriginOS among them) never render the animated icon at all — only the background.
// Drawing it here instead means every launch gets the full animation, at whatever length we choose.
// The system splash stays a flat green of the same colour, so the hand-off is invisible.

private const val VIEWPORT_W = 1845f
private const val VIEWPORT_H = 2104f

private const val MOSQUE_PIVOT_X = 921f
private const val MOSQUE_PIVOT_Y = 880f

val SplashGreen = Color(0xFF1E7D55)

// how long the whole thing is on screen, including the fade-out. The fade starts as the mosque's
// overshoot settles (~1340ms) — no dead time holding a finished frame.
private const val SPLASH_TOTAL_MS = 1600L
private const val FADE_OUT_DELAY_MS = 1300
private const val FADE_OUT_MS = 300

private val PATH_MOSQUE =
    "M910.462 0C842.772 23.2908 794.13 87.5201 794.13 163.114C794.134 258.363 871.344 335.572 966.588 335.572C1042.19 335.572 1106.42 286.93 1129.7 219.236C1124.42 323.508 1042.63 407.573 939.355 416.467V482.929C963.64 489.983 979.193 502.868 990.776 519.163C1003.95 537.689 1012.09 560.932 1022.34 583.518C1032.73 606.398 1045.6 629.57 1068.51 649.847C1079.34 659.426 1092.49 668.42 1108.79 676.43C1444.24 747.901 1692.73 1006.64 1692.73 1314.9C1692.73 1332.82 1691.88 1350.58 1690.23 1368.14C1315.57 1370.51 1027.63 1613.45 921.928 1749.83C815.991 1613.14 527.877 1369.4 152.166 1368.12C150.517 1350.57 149.671 1332.81 149.671 1314.9C149.673 1002.04 405.65 740.168 748.711 673.342C762.271 666.134 773.507 658.219 782.969 649.847C805.888 629.569 818.762 606.398 829.143 583.518C839.393 560.932 847.529 537.688 860.7 519.163C872.287 502.868 887.841 489.983 912.126 482.929V417.051C801.042 412.3 712.439 320.757 712.438 208.505C712.438 96.8062 800.159 5.59095 910.462 0Z"
private val PATH_SWOOSH =
    "M1837.83 1379.71C1392.09 1306.7 1041.12 1596.03 921.927 1749.83C802.732 1596.03 452.908 1306.7 7.17276 1379.71C0.242784 1388.16 -7.79597 1407.09 15.4887 1415.2C44.5945 1425.34 572.657 1354.36 921.927 1775.18C1271.2 1354.36 1800.41 1425.34 1829.51 1415.2C1852.8 1407.09 1844.76 1388.16 1837.83 1379.71Z"
private val PATH_WING_R1 =
    "M1827.88 1503.81C1342.23 1430.8 1040.64 1726.9 950.55 1884.07C933.918 1916.52 954.708 1907.73 967.182 1899.28C1253.25 1643.75 1640.77 1603.52 1798.78 1615.35C1858.65 1582.91 1843.13 1527.47 1827.88 1503.81Z"
private val PATH_WING_L1 =
    "M22.1722 1503.81C507.824 1430.8 809.416 1726.9 899.505 1884.07C916.137 1916.52 895.347 1907.73 882.873 1899.28C596.804 1643.75 209.281 1603.52 51.2781 1615.35C-8.59674 1582.91 6.92633 1527.47 22.1722 1503.81Z"
private val PATH_WING_R2 =
    "M1827.88 1700.14C1342.23 1627.13 1040.64 1923.22 950.55 2080.39C933.918 2112.84 954.708 2104.05 967.182 2095.6C1253.25 1840.07 1640.77 1799.85 1798.78 1811.68C1858.65 1779.23 1843.13 1723.8 1827.88 1700.14Z"
private val PATH_WING_L2 =
    "M22.1722 1700.14C507.824 1627.13 809.416 1923.22 899.505 2080.39C916.137 2112.84 895.347 2104.05 882.873 2095.6C596.804 1840.07 209.281 1799.85 51.2781 1811.68C-8.59674 1779.23 6.92633 1723.8 22.1722 1700.14Z"

private fun svgPath(data: String): Path = PathParser().parsePathString(data).toPath()

/**
 * "Bloom Upward": the book's wing strokes light up bottom-to-top, the swoosh completes the open
 * Quran, then the mosque pops in above it. [onFinished] fires once it has faded out.
 */
@Composable
fun QuranSplash(onFinished: () -> Unit) {
    val mosque = remember { svgPath(PATH_MOSQUE) }
    val swoosh = remember { svgPath(PATH_SWOOSH) }
    val wingR1 = remember { svgPath(PATH_WING_R1) }
    val wingL1 = remember { svgPath(PATH_WING_L1) }
    val wingR2 = remember { svgPath(PATH_WING_R2) }
    val wingL2 = remember { svgPath(PATH_WING_L2) }

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        started = true
        delay(SPLASH_TOTAL_MS.milliseconds)
        onFinished()
    }

    val target = if (started) 1f else 0f
    fun fade(delayMs: Int, durationMs: Int = 340) =
        tween<Float>(durationMillis = durationMs, delayMillis = delayMs, easing = FastOutSlowInEasing)

    // bottom-to-top: the lowest strokes land first
    val aWingL2 by animateFloatAsState(target, fade(0), label = "wingL2")
    val aWingR2 by animateFloatAsState(target, fade(90), label = "wingR2")
    val aWingL1 by animateFloatAsState(target, fade(240), label = "wingL1")
    val aWingR1 by animateFloatAsState(target, fade(330), label = "wingR1")
    val aSwoosh by animateFloatAsState(target, fade(470, 380), label = "swoosh")
    val aMosque by animateFloatAsState(target, fade(720, 400), label = "mosque")

    // overshoot so the mosque settles with a little weight instead of just appearing
    val mosqueScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.6f,
        animationSpec = tween(620, delayMillis = 720, easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)),
        label = "mosqueScale",
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
        Canvas(Modifier.size(150.dp, 171.dp)) {
            val s = min(size.width / VIEWPORT_W, size.height / VIEWPORT_H)
            translate(
                left = (size.width - VIEWPORT_W * s) / 2f,
                top = (size.height - VIEWPORT_H * s) / 2f,
            ) {
                scale(s, s, pivot = Offset.Zero) {
                    white(wingL2, aWingL2)
                    white(wingR2, aWingR2)
                    white(wingL1, aWingL1)
                    white(wingR1, aWingR1)
                    white(swoosh, aSwoosh)
                    scale(mosqueScale, mosqueScale, pivot = Offset(MOSQUE_PIVOT_X, MOSQUE_PIVOT_Y)) {
                        white(mosque, aMosque)
                    }
                }
            }
        }
    }
}

private fun DrawScope.white(path: Path, alpha: Float) {
    if (alpha > 0f) drawPath(path, Color.White, alpha = alpha)
}
