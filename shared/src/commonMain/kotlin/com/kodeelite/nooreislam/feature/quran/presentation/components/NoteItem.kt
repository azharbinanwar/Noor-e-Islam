package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.StickyNote
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppTile
import com.kodeelite.nooreislam.core.components.TilePosition
import com.kodeelite.nooreislam.core.util.toSurahKey
import com.kodeelite.nooreislam.feature.quran.data.Note
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.quran_surah_name
import org.jetbrains.compose.resources.Font

// a saved note item
@Composable
fun NoteItem(note: Note, position: TilePosition, onClick: () -> Unit) {
    val colors = AppTheme.colors
    AppTile(
        title = "Surah ${note.surah} · Ayah ${note.ayah}",
        subtitle = note.text,
        leading = {
            Box(
                Modifier.size(38.dp).clip(CircleShape).background(colors.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Icon(Lucide.StickyNote, null, tint = colors.primary, modifier = Modifier.size(20.dp)) }
        },
        trailing = {
            Text(
                note.surah.toSurahKey(),
                fontFamily = FontFamily(Font(Res.font.quran_surah_name)),
                color = colors.primary,
                fontSize = 28.sp
            )
        },
        position = position,
        onClick = onClick,
    )
}
