package com.kodeelite.nooreislam.feature.studio.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.kodeelite.nooreislam.core.datetime.HijriMonth
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.datetime.format
import com.kodeelite.nooreislam.core.util.toArabicIndic
import com.kodeelite.nooreislam.core.util.toSurahKey
import com.kodeelite.nooreislam.feature.quran.data.QuranSymbols
import com.kodeelite.nooreislam.feature.studio.data.LogoCorner
import com.kodeelite.nooreislam.feature.studio.data.StudioConfig
import com.kodeelite.nooreislam.feature.studio.data.SurahPlacement
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.hijri_era
import com.kodeelite.nooreislam.resources.miqat_logo
import com.kodeelite.nooreislam.resources.quran_juz
import com.kodeelite.nooreislam.resources.quran_surah_name
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

// The rendered post: background (image / gradient / color) + vignette/overlay, watermark, and the ayah
// card (surah name, bismillah, ayah text, translation, dates). Pure render from [config]; [onUpdate]
// only fires from the card drag/zoom gestures while editing.
@Composable
fun DesignCanvas(
    config: StudioConfig,
    modifier: Modifier,
    isEditing: Boolean,
    onImageRatio: (Float) -> Unit = {},   // reports the loaded photo's width/height (for the "Original" layout)
    onUpdate: (StudioConfig) -> Unit,
) {
    val today = Now.date()          // generic time service — formatting handled centrally later
    val todayHijri = Now.hijri()
    val surahFont = FontFamily(Font(Res.font.quran_surah_name))
    val canvasShape = RectangleShape   // square canvas; clip still crops panned/zoomed images to the frame
    val liveConfig = rememberUpdatedState(config)   // gesture callbacks read the latest config
    var imgRatio by remember(config.bgImageUrl) { mutableStateOf<Float?>(null) }   // photo w/h once loaded
    val liveRatio = rememberUpdatedState(imgRatio)

    Box(
        modifier = modifier
            .clip(canvasShape)   // crop: panned/zoomed image stays inside the frame
            .background(config.bgColor)
            .pointerInput(isEditing, config.bgImageUrl != null) {
                if (!isEditing || config.bgImageUrl == null) return@pointerInput
                detectTransformGestures { _, pan, zoom, _ ->
                    val c = liveConfig.value
                    val newScale = (c.bgImageScale * zoom).coerceIn(1f, 4f)
                    val r = liveRatio.value
                    if (r == null) {
                        onUpdate(
                            c.copy(
                                bgImageScale = newScale,
                                bgImageOffsetX = c.bgImageOffsetX + pan.x,
                                bgImageOffsetY = c.bgImageOffsetY + pan.y
                            )
                        )
                        return@detectTransformGestures
                    }
                    // the photo always covers the frame (crop): clamp pan to the cropped-out overflow so no bg shows
                    val cw = size.width.toFloat()
                    val ch = size.height.toFloat()
                    val canvasR = cw / ch
                    val coverW = if (r > canvasR) ch * r else cw
                    val coverH = if (r > canvasR) ch else cw / r
                    val maxX = maxOf(0f, (coverW * newScale - cw) / 2f)
                    val maxY = maxOf(0f, (coverH * newScale - ch) / 2f)
                    onUpdate(
                        c.copy(
                            bgImageScale = newScale,
                            bgImageOffsetX = (c.bgImageOffsetX + pan.x).coerceIn(-maxX, maxX),
                            bgImageOffsetY = (c.bgImageOffsetY + pan.y).coerceIn(-maxY, maxY),
                        )
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // BACKGROUND
        if (config.bgImageUrl != null) {
            val density = LocalDensity.current
            // draw the photo at its cover size (overflow real, not cropped away) so panning reveals it; frame clips
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val r = imgRatio
                val cw = constraints.maxWidth.toFloat()
                val ch = constraints.maxHeight.toFloat()
                val coverMod = if (r != null && ch > 0f) {
                    val canvasR = cw / ch
                    val coverW = if (r >= canvasR) ch * r else cw
                    val coverH = if (r >= canvasR) ch else cw / r
                    // requiredSize (not size): force the cover dimensions, allowed to exceed the frame so overflow is real
                    with(density) { Modifier.requiredSize(coverW.toDp(), coverH.toDp()) }
                } else Modifier.fillMaxSize()
                AsyncImage(
                    model = config.bgImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    onSuccess = {
                        val sz = it.painter.intrinsicSize
                        if (sz.isSpecified && sz.height > 0f) {
                            val ratio = sz.width / sz.height
                            imgRatio = ratio
                            onImageRatio(ratio)
                        }
                    },
                    modifier = coverMod
                        .align(Alignment.Center)
                        .graphicsLayer {
                            scaleX = config.bgImageScale; scaleY = config.bgImageScale
                            translationX = config.bgImageOffsetX; translationY = config.bgImageOffsetY
                        }
                        .blur(config.blurRadius.dp)
                )
            }
        } else if (config.bgGradient != null) {
            val brush =
                if (config.bgGradient.isRadial) Brush.radialGradient(config.bgGradient.stops) else Brush.linearGradient(config.bgGradient.stops)
            Box(Modifier.fillMaxSize().blur(config.blurRadius.dp).background(brush))
        } else {
            Box(Modifier.fillMaxSize().blur(config.blurRadius.dp).background(config.bgColor))
        }

        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = config.vignetteIntensity)),
                    radius = 1000f + (config.vignetteSpread * 2000f)
                )
            )
        )
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = config.overlayAlpha)))

        // WATERMARK
        if (config.showWatermark) {
            val align = when (config.watermarkCorner) {
                LogoCorner.TopLeft -> Alignment.TopStart
                LogoCorner.TopRight -> Alignment.TopEnd
                LogoCorner.BottomLeft -> Alignment.BottomStart
                LogoCorner.BottomRight -> Alignment.BottomEnd
            }
            Icon(
                painter = painterResource(Res.drawable.miqat_logo),
                contentDescription = null,
                modifier = Modifier.align(align).padding(16.dp).size(24.dp).graphicsLayer { alpha = 0.25f },
                tint = config.textColor
            )
        }

        // AYAH CARD
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val state = rememberTransformableState { zoom, _, _ -> onUpdate(config.copy(cardScale = (config.cardScale * zoom).coerceIn(0.5f, 2.5f))) }
            Box(
                Modifier
                    .offset { IntOffset(config.cardOffsetX.roundToInt(), config.cardOffsetY.roundToInt()) }
                    .graphicsLayer { scaleX = config.cardScale; scaleY = config.cardScale }
                    .transformable(state = state)
                    .pointerInput(isEditing) {
                        if (!isEditing) return@pointerInput
                        detectDragGestures { change, drag ->
                            change.consume()
                            onUpdate(config.copy(cardOffsetX = config.cardOffsetX + drag.x, cardOffsetY = config.cardOffsetY + drag.y))
                        }
                    }
                    .padding(horizontal = 24.dp).clip(RoundedCornerShape(config.cardCornerRadius.dp)).background(config.cardColor)
                    .padding(config.cardPadding.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (config.surahPlacement == SurahPlacement.Top) {
                        // Top: only the surah name here; the "Al Quran (2:5)" reference sits at the bottom
                        Text(
                            text = config.ayahs.first().surah.toSurahKey(),
                            fontFamily = surahFont,
                            color = config.textColor.copy(alpha = 0.85f),
                            fontSize = 32.sp
                        )
                        Spacer(Modifier.size(12.dp))
                    }
                    if (config.showBismillah) {
                        Text(
                            text = QuranSymbols.BASMALAH,
                            fontFamily = FontFamily(Font(Res.font.quran_juz)),
                            fontSize = (config.fontSize * 1.5f).coerceAtMost(28f).sp,
                            color = config.textColor
                        )
                        Spacer(Modifier.size(12.dp))
                    }

                    val combinedAyahText = config.ayahs.joinToString(" ") { it.text }
                    val annotatedAyah = buildAnnotatedString {
                        val words = combinedAyahText.split(" ")
                        words.forEachIndexed { i, word ->
                            val emphasized = config.emphasizedWords.contains(i)
                            withStyle(
                                SpanStyle(
                                    color = if (emphasized) config.emphasisColor else config.textColor,
                                    fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal
                                )
                            ) { append(word) }
                            if (i < words.size - 1) append(" ")
                        }
                    }

                    Text(
                        text = annotatedAyah,
                        fontSize = config.fontSize.sp,
                        textAlign = config.textAlign,
                        fontFamily = FontFamily(Font(config.fontFamily.res)),
                        lineHeight = (config.fontSize * config.lineHeight).sp,
                        style = TextStyle(
                            shadow = if (config.textShadowAlpha > 0f) Shadow(
                                color = Color.Black.copy(alpha = config.textShadowAlpha),
                                blurRadius = 8f
                            ) else null
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (config.showTranslation) {
                        Spacer(Modifier.size(12.dp))
                        Text(
                            text = config.translationText,
                            color = config.textColor.copy(alpha = 0.8f),
                            fontSize = config.translationSize.sp,
                            textAlign = config.textAlign,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (config.surahPlacement == SurahPlacement.Bottom) {
                        Spacer(Modifier.size(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "(${config.ayahs.first().surah}:${config.ayahs.joinToString(",") { it.ayah.toString() }})",
                                color = config.textColor.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = config.ayahs.first().surah.toSurahKey(),
                                fontFamily = surahFont,
                                color = config.textColor.copy(alpha = 0.85f),
                                fontSize = 32.sp
                            )
                        }
                    }

                    if (config.surahPlacement == SurahPlacement.Top) {
                        // bottom reference in Arabic, ayah font, Arabic-Indic digits: القرآن (٢:٥)
                        Spacer(Modifier.size(16.dp))
                        Text(
                            text = "القرآن (${config.ayahs.first().surah.toArabicIndic()}:${config.ayahs.joinToString("،") { it.ayah.toArabicIndic() }})",
                            fontFamily = FontFamily(Font(config.fontFamily.res)),
                            color = config.textColor.copy(alpha = 0.75f),
                            fontSize = 18.sp
                        )
                    }

                    if (config.showHijri || config.showGregorian) {
                        Spacer(Modifier.size(6.dp))
                        val hijri = if (config.showHijri) "${todayHijri.day} ${HijriMonth.of(todayHijri.month).label()} ${todayHijri.year} ${
                            stringResource(Res.string.hijri_era)
                        }" else ""
                        val greg = if (config.showGregorian) today.format("d MMM yyyy") else ""
                        Text(
                            text = listOf(hijri, greg).filter { it.isNotEmpty() }.joinToString("  •  "),
                            color = config.textColor.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
