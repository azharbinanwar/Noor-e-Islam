package com.kodeelite.nooreislam.feature.studio.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.kodeelite.nooreislam.core.datetime.HijriMonth
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.datetime.format
import com.kodeelite.nooreislam.core.util.toArabicIndic
import com.kodeelite.nooreislam.core.util.toSurahKey
import com.kodeelite.nooreislam.feature.quran.data.Ayah
import com.kodeelite.nooreislam.feature.quran.data.QuranSymbols
import com.kodeelite.nooreislam.feature.studio.data.LogoCorner
import com.kodeelite.nooreislam.feature.studio.data.ImageStore
import com.kodeelite.nooreislam.feature.studio.data.StudioConfig
import com.kodeelite.nooreislam.feature.studio.data.SurahPlacement
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.hijri_era
import com.kodeelite.nooreislam.resources.noor_e_islam_logo
import com.kodeelite.nooreislam.resources.quran_juz
import com.kodeelite.nooreislam.resources.quran_label_arabic
import com.kodeelite.nooreislam.resources.quran_surah_name
import com.kodeelite.nooreislam.resources.tanzil_hafs
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
    // locked font for the surah:ayah reference so the digits always render (Arabic-Indic), like the reader's ayah marker
    val refFont = FontFamily(Font(Res.font.tanzil_hafs))
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
                    // the picked image is always downloaded — render the local file
                    model = ImageStore.byUrl(config.bgImageUrl)?.let(ImageStore::source) ?: config.bgImageUrl,
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
                painter = painterResource(Res.drawable.noor_e_islam_logo),
                contentDescription = null,
                modifier = Modifier.align(align).padding(16.dp).size(24.dp).graphicsLayer { alpha = 0.25f },
                tint = config.textColor
            )
        }

        // AYAH CARD
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    // grow the card to its content height (even past the frame) so tall text isn't clipped at a locked height; only the frame edge crops
                    .wrapContentHeight(unbounded = true)
                    // translation via graphicsLayer = absolute screen pixels (offset {} mirrors X in RTL), so the drag matches the finger in Arabic too
                    .graphicsLayer {
                        translationX = config.cardOffsetX; translationY = config.cardOffsetY; scaleX = config.cardScale; scaleY = config.cardScale
                    }
                    // pan + pinch share ONE gesture stream so they never fight (two detectors compete and drop the pinch)
                    .pointerInput(isEditing) {
                        if (!isEditing) return@pointerInput
                        detectTransformGestures { _, pan, zoom, _ ->
                            val c = liveConfig.value
                            onUpdate(
                                c.copy(
                                    cardScale = (c.cardScale * zoom).coerceIn(0.5f, 2.5f),
                                    cardOffsetX = c.cardOffsetX + pan.x,
                                    cardOffsetY = c.cardOffsetY + pan.y,
                                )
                            )
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
                    // the size glides between the auto-fit steps, so growing or moving the selection
                    // reflows smoothly instead of snapping the whole card to a new layout
                    val ayahFontSize by animateFloatAsState(config.fontSize, label = "ayahFontSize")

                    if (config.showBismillah) {
                        Text(
                            text = QuranSymbols.BASMALAH,
                            fontFamily = FontFamily(Font(Res.font.quran_juz)),
                            fontSize = (ayahFontSize * 1.5f).coerceAtMost(28f).sp,
                            color = config.textColor
                        )
                        Spacer(Modifier.size(12.dp))
                    }

                    // the font carries the spelling: picking Nastaleeq shows the IndoPak text, a Tanzil
                    // face the Tanzil. Past one ayah, each ends with its ornate number as the mushaf
                    // separates them — drawn in the reference font's own span, the way the reader does:
                    // the body face drops the digits inside the brackets on iOS
                    val annotatedAyah = buildAnnotatedString {
                        var wordIndex = 0
                        config.ayahs.forEachIndexed { a, ayah ->
                            val words = ayah.textIn(config.fontFamily.script).split(" ").filter { it.isNotBlank() }
                            words.forEachIndexed { w, word ->
                                val emphasized = config.emphasizedWords.contains(wordIndex)
                                withStyle(
                                    SpanStyle(
                                        color = if (emphasized) config.emphasisColor else config.textColor,
                                        fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal
                                    )
                                ) { append(word) }
                                wordIndex++
                                if (w < words.size - 1) append(" ")
                            }
                            if (config.ayahs.size > 1) {
                                withStyle(SpanStyle(fontFamily = refFont, color = config.textColor)) {
                                    append(" " + QuranSymbols.ayahNumber(ayah.ayah.toArabicIndic()))
                                }
                            }
                            if (a < config.ayahs.size - 1) append(" ")
                        }
                    }

                    // RTL like the reader's page, so a trailing waqf mark stays at the end of the
                    // line instead of bidi floating it to the start
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(
                            text = annotatedAyah,
                            fontSize = ayahFontSize.sp,
                            textAlign = config.textAlign,
                            fontFamily = FontFamily(Font(config.fontFamily.res)),
                            lineHeight = (ayahFontSize * config.lineHeight).sp,
                            style = TextStyle(
                                shadow = if (config.textShadowAlpha > 0f) Shadow(
                                    color = Color.Black.copy(alpha = config.textShadowAlpha),
                                    blurRadius = 8f
                                ) else null
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

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
                        // lock LTR so the reference stays on the left and the surah name on the right in both languages
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = QuranSymbols.ltrLock("(${config.ayahs.first().surah.toArabicIndic()}:${ayahRangeArabic(config.ayahs)})"),
                                    fontFamily = refFont,
                                    color = config.textColor.copy(alpha = 0.7f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    // declared at the engine, not with control characters iOS ignores
                                    style = TextStyle(textDirection = TextDirection.Ltr)
                                )
                                Text(
                                    text = config.ayahs.first().surah.toSurahKey(),
                                    fontFamily = surahFont,
                                    color = config.textColor.copy(alpha = 0.85f),
                                    fontSize = 32.sp
                                )
                            }
                        }
                    }

                    if (config.surahPlacement == SurahPlacement.Top) {
                        // address (٢:٥) on the left, القرآن on the right — locked LTR so it never flips between languages
                        Spacer(Modifier.size(16.dp))
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = QuranSymbols.ltrLock(
                                    "(${config.ayahs.first().surah.toArabicIndic()}:${ayahRangeArabic(config.ayahs)})"
                                ) + " ${stringResource(Res.string.quran_label_arabic)}",
                                fontFamily = refFont,
                                color = config.textColor.copy(alpha = 0.75f),
                                fontSize = 18.sp,
                                style = TextStyle(textDirection = TextDirection.Ltr)
                            )
                        }
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

// 95:6 for one ayah, 95:4-6 for a contiguous range — a share reference, not a list
private fun ayahRangeArabic(ayahs: List<Ayah>): String {
    val first = ayahs.first().ayah
    val last = ayahs.last().ayah
    return if (first == last) first.toArabicIndic() else "${first.toArabicIndic()}-${last.toArabicIndic()}"
}
