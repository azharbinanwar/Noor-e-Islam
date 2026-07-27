package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.AlignJustify
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.ChevronsDown
import com.composables.icons.lucide.Languages
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Navigation
import com.composables.icons.lucide.Sun
import com.composables.icons.lucide.Type
import com.composables.icons.lucide.WholeWord
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.MiniStepper
import com.kodeelite.nooreislam.core.constants.defaults.QuranDefaults
import com.kodeelite.nooreislam.feature.quran.data.QuranFont
import com.kodeelite.nooreislam.feature.quran.data.QuranTheme
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.auto_scroll
import com.kodeelite.nooreislam.resources.content
import com.kodeelite.nooreislam.resources.jump_to
import com.kodeelite.nooreislam.resources.keep_screen_on
import com.kodeelite.nooreislam.resources.line_spacing
import com.kodeelite.nooreislam.resources.reading
import com.kodeelite.nooreislam.resources.reading_settings
import com.kodeelite.nooreislam.resources.script
import com.kodeelite.nooreislam.resources.tafsir_source
import com.kodeelite.nooreislam.resources.text_size
import com.kodeelite.nooreislam.resources.theme
import com.kodeelite.nooreislam.resources.translation
import com.kodeelite.nooreislam.resources.word_by_word
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource

// global reader/display settings — opened from the app-bar icon (not ayah-specific).
// ponytail: all rows listed as reminders; the ones without a feature yet are placeholders (onClick = {}).
@Composable
fun ReaderSettingsSheet(
    fontSize: Int,
    onFontChange: (Int) -> Unit,
    font: QuranFont,
    onFontSelect: (QuranFont) -> Unit,
    theme: QuranTheme,
    onThemeSelect: (QuranTheme) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    AppBottomSheet(onDismiss = onDismiss, title = stringResource(Res.string.reading_settings)) {
        AppTileGroup(
            title = "Display", // ponytail: keep simple for now or add Res.string.display_label if needed
            items = listOf(
                AppTileItem(
                    title = stringResource(Res.string.text_size),
                    leadingIcon = Lucide.Type,
                    trailing = {
                        MiniStepper(
                            value = fontSize,
                            suffix = "sp",
                            onChange = onFontChange,
                            min = QuranDefaults.MIN_FONT_SP,
                            max = QuranDefaults.MAX_FONT_SP
                        )
                    },
                ),
                AppTileItem(
                    title = stringResource(Res.string.line_spacing),
                    subtitle = "placeholder",
                    leadingIcon = Lucide.AlignJustify,
                    onClick = {}),
            ),
        )
        Text(
            stringResource(Res.string.theme),
            color = colors.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 6.dp)
        )
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuranTheme.entries.forEach { t -> ThemeChip(t, selected = t == theme, onClick = { onThemeSelect(t) }) }
        }
        Spacer(Modifier.size(12.dp))
        Text(
            stringResource(Res.string.script),
            color = colors.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuranFont.entries.forEach { f -> FontChip(f, selected = f == font, onClick = { onFontSelect(f) }) }
        }
        Spacer(Modifier.size(12.dp))
        AppTileGroup(
            title = stringResource(Res.string.content),
            items = listOf(
                AppTileItem(
                    title = stringResource(Res.string.translation),
                    subtitle = "on/off + pick — placeholder",
                    leadingIcon = Lucide.Languages,
                    onClick = {}),
                AppTileItem(title = stringResource(Res.string.tafsir_source), subtitle = "placeholder", leadingIcon = Lucide.BookOpen, onClick = {}),
                AppTileItem(
                    title = stringResource(Res.string.word_by_word),
                    subtitle = "on/off — placeholder",
                    leadingIcon = Lucide.WholeWord,
                    onClick = {}),
            ),
        )
        AppTileGroup(
            title = stringResource(Res.string.reading),
            items = listOf(
                AppTileItem(
                    title = stringResource(Res.string.auto_scroll),
                    subtitle = "play/pause + speed — placeholder",
                    leadingIcon = Lucide.ChevronsDown,
                    onClick = {}),
                AppTileItem(title = stringResource(Res.string.keep_screen_on), subtitle = "placeholder", leadingIcon = Lucide.Sun, onClick = {}),
                AppTileItem(title = stringResource(Res.string.jump_to), subtitle = "placeholder", leadingIcon = Lucide.Navigation, onClick = {}),
            ),
        )
    }
}

// mini reading-page preview painted in the theme's own colors — pick by look
@Composable
private fun ThemeChip(theme: QuranTheme, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    // System has no fixed colors — preview it in the current app palette
    val app = theme == QuranTheme.System
    val bg = if (app) colors.background else theme.background
    val tx = if (app) colors.onBackground else theme.onBackground
    val ac = if (app) colors.primary else theme.primary
    Column(
        Modifier.width(122.dp).clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, if (selected) colors.primary else colors.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick).padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(11.dp)).background(bg).padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Bar(ac, 0.5f, Modifier.align(Alignment.CenterHorizontally))       // surah name
            Bar(tx.copy(alpha = 0.82f), 1f)
            Bar(tx.copy(alpha = 0.82f), 1f)
            Bar(tx.copy(alpha = 0.82f), 0.6f, Modifier.align(Alignment.CenterHorizontally))
            Text(
                "ع",
                fontFamily = FontFamily(Font(QuranDefaults.FONT.res)),
                color = ac,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        Spacer(Modifier.size(6.dp))
        Text(theme.label, color = if (selected) colors.primary else colors.onSurfaceVariant, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
private fun Bar(color: Color, widthFraction: Float, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(widthFraction).height(5.dp).clip(RoundedCornerShape(3.dp)).background(color))
}

// live preview chip: the same Arabic sample in each font so the user picks by look, not name
@Composable
private fun FontChip(font: QuranFont, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val fam = FontFamily(Font(font.res))
    Column(
        Modifier.width(104.dp).clip(RoundedCornerShape(12.dp))
            .background(if (selected) colors.primary.copy(alpha = 0.12f) else Color.Transparent)
            .border(1.dp, if (selected) colors.primary else colors.outlineVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // fixed-size box so different font metrics can't make chips uneven
        Box(Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
            Text(font.sample, fontFamily = fam, fontSize = 20.sp, color = colors.onBackground, maxLines = 1)
        }
        Spacer(Modifier.size(3.dp))
        Text(font.label, color = if (selected) colors.primary else colors.onSurfaceVariant, fontSize = 11.sp, maxLines = 1)
    }
}
