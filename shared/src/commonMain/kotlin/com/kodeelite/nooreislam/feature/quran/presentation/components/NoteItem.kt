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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.ExpandableText
import com.kodeelite.nooreislam.core.components.TilePosition
import com.kodeelite.nooreislam.core.components.shapeFor
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.util.toSurahKey
import com.kodeelite.nooreislam.feature.quran.data.Note
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.quran_surah_name
import com.kodeelite.nooreislam.resources.surah_number_ayah_number
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource

// a saved note item — same card family as BookmarkItem/HighlightItem
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteItem(note: Note, position: TilePosition, onClick: () -> Unit, onLongClick: () -> Unit = {}) {
    val colors = AppTheme.colors
    val timestamp = remember(note.createdAt) { Now.formattedDateTime(note.createdAt) }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shapeFor(position))
                .background(colors.cardColor)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .height(IntrinsicSize.Min)
        ) {
            // Decorative background glyph (Aligned to Right/End)
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = note.surah.toSurahKey(),
                    fontFamily = FontFamily(Font(Res.font.quran_surah_name)),
                    color = colors.primary.copy(alpha = 0.18f),
                    fontSize = 72.sp
                )
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Note Text Body — follows whatever direction the note's own text is written in
                CompositionLocalProvider(LocalLayoutDirection provides if (isRtlText(note.text)) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                    ExpandableText(
                        text = note.text,
                        style = MaterialTheme.typography.titleMedium.copy(lineHeight = 22.sp),
                        color = colors.onSurface,
                        collapsedMaxLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Footnote Row (citation + timestamp)
                Row {
                    Text(
                        text = stringResource(Res.string.surah_number_ayah_number, note.surah, note.ayah),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "  •  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant.copy(alpha = 0.5f)
                    )
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

// first strong-direction character decides the note's own reading direction
// covers Hebrew/Arabic/Syriac/NKo/Samaritan/Mandaic (U+0590-08FF) and Arabic presentation forms (U+FB1D-FEFF)
private fun isRtlText(text: String): Boolean {
    val c = text.firstOrNull { !it.isWhitespace() } ?: return false
    return c.code in 0x0590..0x08FF || c.code in 0xFB1D..0xFEFF
}
