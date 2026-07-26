package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppTile
import com.kodeelite.nooreislam.core.components.TilePosition
import com.kodeelite.nooreislam.core.util.toJuzKey
import com.kodeelite.nooreislam.feature.quran.data.Juz
import com.kodeelite.nooreislam.feature.quran.data.Surah
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.expand
import com.kodeelite.nooreislam.resources.juz_number
import com.kodeelite.nooreislam.resources.juz_start_summary
import com.kodeelite.nooreislam.resources.quran_juz
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource

// self-contained juz: collapsed shows one tile; tapping the chevron folds its surahs in beneath.
// Owns its expanded state and reads juz.surahs, so the screen just hands it the juz.
@Composable
fun JuzRow(juz: Juz, onOpen: () -> Unit, onOpenSurah: (Surah) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val nameFont = FontFamily(Font(Res.font.quran_juz))
    Column(Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        AppTile(
            title = stringResource(Res.string.juz_number, juz.number),
            subtitle = stringResource(Res.string.juz_start_summary, juz.startsAt.surah, juz.startsAt.ayah),
            leading = { NumberBadge(juz.number) },
            trailing = { JuzName(juz.number, nameFont, expanded) { expanded = !expanded } },
            position = if (expanded) TilePosition.First else TilePosition.Single,
            onClick = onOpen,
        )
        if (expanded) juz.surahs.forEachIndexed { i, s ->
            // +1: the juz tile above is the group's first item, so the surahs are its middle/last members
            SurahItem(s, TilePosition.at(i + 1, juz.surahs.size + 1)) { onOpenSurah(s) }
        }
    }
}

// ornate juz name + the expand chevron (its own tap target, so the tile tap can open instead)
@Composable
private fun JuzName(number: Int, nameFont: FontFamily, expanded: Boolean, onToggle: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(number.toJuzKey(), fontFamily = nameFont, color = AppTheme.colors.primary, fontSize = 18.sp)
        Spacer(Modifier.size(6.dp))
        Icon(
            Lucide.ChevronDown, stringResource(Res.string.expand), tint = AppTheme.colors.onSurfaceVariant,
            modifier = Modifier.clip(CircleShape).clickable(onClick = onToggle).padding(4.dp).size(20.dp).rotate(if (expanded) 180f else 0f),
        )
    }
}
