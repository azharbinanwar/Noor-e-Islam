package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppCard
import com.kodeelite.nooreislam.core.locale.tr
import com.kodeelite.nooreislam.feature.quran.data.MushafSign
import com.kodeelite.nooreislam.feature.quran.data.QuranStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.tanzil_scheherazade
import org.jetbrains.compose.resources.Font
import org.koin.compose.koinInject

// How tall the mark's own ink should render, the same for every sign so none looks bigger than another.
private const val TARGET_INK_SP = 20f

// The font size needed to hit that gets extreme for the tiniest marks (madda is 0.055em), so it is capped.
// Those few render a little under target rather than at a size the text rasteriser handles badly.
private const val MIN_GLYPH_SP = 24f
private const val MAX_GLYPH_SP = 240f

// Distance from a line box's vertical centre down to its baseline, in em, for tanzil_scheherazade
// (ascent 1.011em, descent 0.677em). Turns a mark's ink centre into the nudge that centres it in the tile.
private const val BASELINE_BELOW_LINE_CENTRE_EM = 0.167f

/** One mark: the glyph, what it is called, what it asks of the reader, and where to see it. */
@Composable
fun MushafSignCard(sign: MushafSign, onOpen: () -> Unit) {
    val colors = AppTheme.colors
    val font by koinInject<QuranStore>().font.collectAsState()
    val mushaf = FontFamily(Font(font.res))
    // the specimen is pinned to one face so the measured scale and offset stay true; the example below
    // still renders in whichever script the reader chose.
    val specimen = FontFamily(Font(Res.font.tanzil_scheherazade))
    // size so every mark shows the same height of ink, then nudge so that ink lands on the tile's centre
    val glyphSp = (TARGET_INK_SP / sign.inkHeightEm).coerceIn(MIN_GLYPH_SP, MAX_GLYPH_SP)
    val nudgeEm = sign.inkCentreEm - BASELINE_BELOW_LINE_CENTRE_EM

    AppCard(padding = 0.dp) {
        Row(
            Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(colors.primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    sign.glyph,
                    fontFamily = specimen,
                    fontSize = glyphSp.sp,
                    color = colors.primary,
                    // unbounded so a tall line box is never cropped by the tile, then nudged down by the
                    // measured distance from the line-box centre to the mark's own ink centre
                    modifier = Modifier
                        .wrapContentSize(unbounded = true)
                        .graphicsLayer { translationY = nudgeEm * glyphSp.sp.toPx() },
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        sign.label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                    )
                    Text(sign.arabicName, fontFamily = mushaf, fontSize = 15.sp, color = colors.primary)
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    sign.meaning,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    lineHeight = 18.sp,
                )
            }
        }

        val example = sign.example
        if (example == null) {
            Spacer(Modifier.height(14.dp))
        } else {
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = colors.onSurfaceVariant.copy(alpha = 0.12f))

            Row(
                Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                        example.text,
                        modifier = Modifier.weight(1f),
                        fontFamily = mushaf,
                        fontSize = 18.sp,
                        lineHeight = 34.sp,
                        color = colors.onSurface,
                        textAlign = TextAlign.Start,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(example.reference, style = MaterialTheme.typography.labelMedium, color = colors.primary)
                Icon(
                    tr(Lucide.ChevronRight, Lucide.ChevronLeft),
                    null,
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
