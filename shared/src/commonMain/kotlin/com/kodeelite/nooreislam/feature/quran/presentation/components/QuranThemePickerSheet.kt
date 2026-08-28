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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.AlignCenter
import com.composables.icons.lucide.AlignJustify
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Type
import com.composables.icons.lucide.UnfoldVertical
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppIconOption
import com.kodeelite.nooreislam.core.components.AppIconToggle
import com.kodeelite.nooreislam.config.theme.LocalBaseAppColors
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.MiniStepper
import com.kodeelite.nooreislam.core.constants.defaults.QuranDefaults
import com.kodeelite.nooreislam.feature.quran.data.QuranFont
import com.kodeelite.nooreislam.feature.quran.data.QuranScript
import com.kodeelite.nooreislam.feature.quran.data.QuranStore
import com.kodeelite.nooreislam.feature.quran.data.QuranTheme
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.alignment
import com.kodeelite.nooreislam.resources.font
import com.kodeelite.nooreislam.resources.line_spacing
import com.kodeelite.nooreislam.resources.reading_settings
import com.kodeelite.nooreislam.resources.script
import com.kodeelite.nooreislam.resources.text_size
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

// everything appearance-related in one sheet — theme color, text size, line spacing, script — same
// entry point from both the reader and the index screen's app bar. Content/Reading stay in
// ReaderSettingsSheet, which only opens from the reader itself.
@Composable
fun QuranThemePickerSheet(onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    val store = koinInject<QuranStore>()
    val fontSize by store.fontSize.collectAsState()
    val lineSpacing by store.lineSpacing.collectAsState()
    val font by store.font.collectAsState()
    val script by store.script.collectAsState()
    val theme by store.theme.collectAsState()
    val justify by store.justifyText.collectAsState()
    AppBottomSheet(onDismiss = onDismiss, title = stringResource(Res.string.reading_settings), scrimAlpha = 0f) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuranTheme.entries.forEach { t -> ThemeChip(t, selected = t == theme, onClick = { store.setTheme(t) }) }
        }
        Spacer(Modifier.size(12.dp))
        // script sits above font because it is the parent choice: it changes the letters, the font
        // only their shape. The fonts below dim to whichever script is not active, which is the whole
        // explanation of why picking IndoPak leaves one choice instead of five.
        Text(
            stringResource(Res.string.script),
            color = colors.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuranScript.entries.forEach { s -> ScriptChip(s, selected = s == script, onClick = { store.setScript(s) }) }
        }
        // one line under the row rather than inside each chip: it only has to describe the one that
        // is selected, and it has the width to say something a reader can recognise themselves in
        Text(
            script.hint,
            color = colors.onSurfaceVariant,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 4.dp, top = 6.dp)
        )
        Spacer(Modifier.size(12.dp))
        Text(
            stringResource(Res.string.font),
            color = colors.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuranFont.entries.forEach { f ->
                FontChip(f, selected = f == font, dimmed = f.script != script, onClick = { store.setFont(f) })
            }
        }
        // both scripts get a line, so the sheet's height is the same either way: one that came and went
        // re-measured the sheet on every switch and jumped under the finger that did it
        Text(
            script.fontHint,
            color = colors.onSurfaceVariant,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 4.dp, top = 6.dp)
        )
        Spacer(Modifier.size(12.dp))
        AppTileGroup(
            items = listOf(
                AppTileItem(
                    title = stringResource(Res.string.text_size),
                    leadingIcon = Lucide.Type,
                    trailing = {
                        MiniStepper(
                            value = fontSize,
                            suffix = "sp",
                            onChange = { store.setFontSize(it) },
                            min = QuranDefaults.MIN_FONT_SP,
                            max = QuranDefaults.MAX_FONT_SP
                        )
                    },
                ),
                AppTileItem(
                    title = stringResource(Res.string.line_spacing),
                    leadingIcon = Lucide.UnfoldVertical,
                    trailing = {
                        MiniStepper(
                            value = lineSpacing,
                            suffix = "%",
                            onChange = { store.setLineSpacing(it) },
                            min = QuranDefaults.MIN_LINE_SPACING_PERCENT,
                            max = QuranDefaults.MAX_LINE_SPACING_PERCENT,
                            step = QuranDefaults.LINE_SPACING_STEP_PERCENT,
                        )
                    },
                ),
                AppTileItem(
                    title = stringResource(Res.string.alignment),
                    leadingIcon = Lucide.AlignJustify,
                    trailing = {
                        AppIconToggle(
                            options = listOf(
                                AppIconOption(Lucide.AlignCenter, false),
                                AppIconOption(Lucide.AlignJustify, true),
                            ),
                            selected = justify,
                            onPick = { store.setJustifyText(it) },
                        )
                    },
                ),
            ),
        )
    }
}

// mini reading-page preview painted in the theme's own colors — pick by look
@Composable
private fun ThemeChip(theme: QuranTheme, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    // System has no fixed colors — preview the TRUE app palette (LocalBaseAppColors), not AppTheme.colors,
    // which QuranThemeHost has already overridden to whatever reading theme is currently active
    val app = theme == QuranTheme.System
    val base = LocalBaseAppColors.current
    val bg = if (app) base.background else theme.background
    val tx = if (app) base.onBackground else theme.onBackground
    val ac = if (app) base.primary else theme.primary
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

// each script drawn in its own font, showing the one phrase everyone knows in both spellings
@Composable
private fun ScriptChip(script: QuranScript, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val fam = FontFamily(Font(script.fonts.first().res))
    Column(
        Modifier.width(126.dp).clip(RoundedCornerShape(12.dp))
            .background(if (selected) colors.primary.copy(alpha = 0.12f) else Color.Transparent)
            .border(1.dp, if (selected) colors.primary else colors.outlineVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
            Text(script.sample, fontFamily = fam, fontSize = 20.sp, color = colors.onBackground, maxLines = 1)
        }
        Spacer(Modifier.size(3.dp))
        Text(script.label, color = if (selected) colors.primary else colors.onSurfaceVariant, fontSize = 11.sp, maxLines = 1)
    }
}

// live preview chip: the same Arabic sample in each font so the user picks by look, not name.
// A dimmed chip belongs to the other script and stays tappable — picking it carries the script over.
@Composable
private fun FontChip(font: QuranFont, selected: Boolean, dimmed: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val fam = FontFamily(Font(font.res))
    Column(
        Modifier.width(104.dp).alpha(if (dimmed) 0.4f else 1f).clip(RoundedCornerShape(12.dp))
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
