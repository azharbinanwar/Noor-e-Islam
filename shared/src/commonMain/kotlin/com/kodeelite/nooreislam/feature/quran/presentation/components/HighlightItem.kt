package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.TilePosition
import com.kodeelite.nooreislam.core.constants.defaults.QuranDefaults
import com.kodeelite.nooreislam.core.util.toSurahKey
import com.kodeelite.nooreislam.feature.quran.data.Highlight
import com.kodeelite.nooreislam.feature.quran.data.QuranRepository
import com.kodeelite.nooreislam.feature.quran.data.hue
import com.kodeelite.nooreislam.feature.quran.data.tint
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.quran_surah_name
import com.kodeelite.nooreislam.resources.surah_number_ayah_number
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource

/**
 * Highlight Item: A premium, model-driven card for highlighted ayahs.
 * - Locked to LTR structure with RTL Arabic text.
 * - Subtle background color wash based on the highlight color.
 * - Decorative Surah glyph.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HighlightItem(highlight: Highlight, index: Int, total: Int, onLongClick: () -> Unit, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val hue = highlight.color.hue
    // Use the professional tint logic for a consistent "wash" feel
    val wash = highlight.color.tint(colors.background).copy(alpha = 0.12f)

    val text by produceState("") {
        value = QuranRepository.ayah(highlight.surah, highlight.ayah)?.text ?: ""
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shapeFor(TilePosition.at(index, total)))
                .background(colors.cardColor)
                .background(wash) // Layered tint for depth
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .height(IntrinsicSize.Min)
        ) {
            // Decorative background glyph (Faded, aligned to End)
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = highlight.surah.toSurahKey(),
                    fontFamily = FontFamily(Font(Res.font.quran_surah_name)),
                    color = hue.copy(alpha = 0.18f),
                    fontSize = 72.sp
                )
            }

            // Content Column
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Citation Badge
                Box(
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(hue.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.surah_number_ayah_number, highlight.surah, highlight.ayah),
                        style = MaterialTheme.typography.labelSmall,
                        color = hue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Ayah Text Body (Forced RTL)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily(Font(QuranDefaults.FONT.res)),
                        color = colors.onSurface,
                        textAlign = TextAlign.Start,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = 32.sp
                    )
                }
            }
        }
    }
}

private fun shapeFor(pos: TilePosition): Shape {
    val r = 16.dp
    val m = 4.dp
    return when (pos) {
        TilePosition.Single -> RoundedCornerShape(r)
        TilePosition.First -> RoundedCornerShape(topStart = r, topEnd = r, bottomStart = m, bottomEnd = m)
        TilePosition.Middle -> RoundedCornerShape(m)
        TilePosition.Last -> RoundedCornerShape(topStart = m, topEnd = m, bottomStart = r, bottomEnd = r)
    }
}
