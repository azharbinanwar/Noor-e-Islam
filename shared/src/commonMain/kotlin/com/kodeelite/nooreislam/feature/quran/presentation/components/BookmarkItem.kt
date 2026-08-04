package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.kodeelite.nooreislam.core.components.shapeFor
import com.kodeelite.nooreislam.core.constants.defaults.QuranDefaults
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.util.toSurahKey
import com.kodeelite.nooreislam.feature.quran.data.Bookmark
import com.kodeelite.nooreislam.feature.quran.data.QuranRepository
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.quran_surah_name
import com.kodeelite.nooreislam.resources.surah_number_ayah_number
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource

/**
 * Bookmark Item: Locked to LTR card layout with RTL text handling.
 * Consistent with the premium HighlightItem design.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarkItem(bookmark: Bookmark, index: Int, total: Int, onLongClick: () -> Unit, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val timestamp = remember(bookmark.createdAt) { Now.formattedDateTime(bookmark.createdAt) }

    val text by produceState("") {
        value = QuranRepository.ayah(bookmark.surah, bookmark.ayah)?.text ?: ""
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shapeFor(TilePosition.at(index, total)))
                .background(colors.cardColor)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .height(IntrinsicSize.Min)
        ) {
            // Decorative background glyph (Aligned to Right/End)
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp, top = 6.dp)
            ) {
                Text(
                    text = bookmark.surah.toSurahKey(),
                    fontFamily = FontFamily(Font(Res.font.quran_surah_name)),
                    color = colors.primary.copy(alpha = 0.30f),
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
                // Ayah Text Body (Forced RTL)
                Spacer(Modifier.height(24.dp))
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

                Spacer(Modifier.height(10.dp))

                // Footnote Row (Bottom-Left)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Plain Citation (No background badge)
                    Text(
                        text = stringResource(Res.string.surah_number_ayah_number, bookmark.surah, bookmark.ayah),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    // Dot Separator
                    Text(
                        text = "  •  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant.copy(alpha = 0.5f)
                    )

                    // Timestamp
                    Text(
                        text = timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
