package com.kodeelite.nooreislam.feature.sandbox.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.update.UpdateSheet
import com.kodeelite.nooreislam.core.components.OverlayStyle
import androidx.compose.material3.Slider
import com.kodeelite.nooreislam.feature.backup.data.BackupStore
import org.koin.compose.koinInject
import kotlinx.datetime.minus
import kotlinx.datetime.DateTimeUnit
import kotlinx.coroutines.launch
import com.kodeelite.nooreislam.feature.tracker.data.TrackerRepository
import com.kodeelite.nooreislam.feature.quran.data.NotesStore
import com.kodeelite.nooreislam.feature.quran.data.BookmarksStore
import com.kodeelite.nooreislam.core.enums.TimeFormat
import com.kodeelite.nooreislam.core.datetime.Now
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Highlighter
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.Star
import com.kodeelite.nooreislam.config.theme.AppColors
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.config.theme.ThemeChoice
import com.kodeelite.nooreislam.config.theme.ThemeMode
import com.kodeelite.nooreislam.config.theme.darkAppColors
import com.kodeelite.nooreislam.config.theme.lightAppColors
import com.kodeelite.nooreislam.core.components.ActionPosition
import com.kodeelite.nooreislam.core.components.ActionWidth
import com.kodeelite.nooreislam.core.components.AppAction
import com.kodeelite.nooreislam.core.components.AppActionGroup
import com.kodeelite.nooreislam.core.components.AppActionItem
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppButtonSize
import com.kodeelite.nooreislam.core.components.AppButtonVariant
import com.kodeelite.nooreislam.core.components.AppChip
import com.kodeelite.nooreislam.core.components.AppTile
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.AppTileVariant
import com.kodeelite.nooreislam.core.components.LocalNotice
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.core.constants.defaults.QuranDefaults
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.core.datetime.format
import com.kodeelite.nooreislam.core.enums.AdhanRoundingStyle
import com.kodeelite.nooreislam.core.enums.CalculationMethod
import com.kodeelite.nooreislam.core.enums.HighLatRule
import com.kodeelite.nooreislam.core.enums.Madhab
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.MiqatTimeStatus
import com.kodeelite.nooreislam.core.enums.PrayerTrackerStatus
import com.kodeelite.nooreislam.core.enums.color
import com.kodeelite.nooreislam.core.enums.label
import com.kodeelite.nooreislam.core.enums.onColor
import com.kodeelite.nooreislam.feature.quran.data.HighlightColor
import com.kodeelite.nooreislam.feature.quran.data.tint
import com.kodeelite.nooreislam.feature.quran.presentation.components.HighlightColorRow
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.indopak_nastaleeq
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.em
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.Placeholder
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.offset

private enum class Section { Geo, Notices, Tiles, Formats, Light, Dark }

/**
 * Scratch page to eyeball every button and color in light + dark. One section at a time, opened by
 * its chip: drawing the lot on open was slow enough on device to read as a hang.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SandboxScreen() {
    var open by remember { mutableStateOf<Section?>(null) }
    Scaffold { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                val nav = LocalAppNavigator.current
                Column {
                    AppButton(
                        text = "Sky lab",
                        onClick = { nav.navigate(AppRoute.SkyLab) },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        variant = AppButtonVariant.Outline,
                    )
                    AppButton(
                        text = "Tracker lab",
                        onClick = { nav.navigate(AppRoute.TrackerLab) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        variant = AppButtonVariant.Outline,
                    )
                    BackupLab()
                    SheetLab()
                    UpdateSheetLab()
                    WaqfLab()
                }
            }
            item { ThemeSwitcher() }
            item {
                FlowRow(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Section.entries.forEach { s ->
                        AppChip(
                            label = s.name,
                            selected = open == s,
                            onClick = { open = if (open == s) null else s },
                        )
                    }
                }
            }
            when (open) {
                Section.Geo -> item { GeoShowcase() }
                Section.Notices -> item { NoticeShowcase() }
                Section.Tiles -> item { TileVariantShowcase() }
                Section.Formats -> item { FormatShowcase() }
                Section.Light -> panel("LIGHT", ThemeMode.LIGHT)
                Section.Dark -> panel("DARK", ThemeMode.DARK)
                null -> Unit
            }
        }
    }
}

/** The showcase under a forced theme mode, one section per item so only what shows gets built. */
private fun LazyListScope.panel(title: String, mode: ThemeMode) {
    section(mode, title) {}
    section(mode, "Miqat — All (chronological)") { MiqatAllShowcase() }
    section(mode, "Miqat — Groups") { MiqatGroupsShowcase() }
    section(mode, "Tracker Status") { TrackerStatusShowcase() }
    section(mode, "Time Status") { TimeStatusShowcase() }
    section(mode, "Calc Methods") { CalcMethodShowcase() }
    section(mode, "Config Enums") { ConfigEnumsShowcase() }
    section(mode, "Tiles") { TileShowcase() }
    section(mode, "Quick Actions (horizontal)") { ActionShowcase() }
    section(mode, "Highlight colors") { HighlightShowcase() }
    section(mode, "Buttons") { ButtonShowcase() }
    section(mode, "StateView") {
        StateView(
            title = "No prayers logged",
            message = "Start tracking your prayers today.",
            action = { AppButton("Track now", {}, size = AppButtonSize.Small) },
        )
    }
    section(mode, "Colors") {
        ColorPalette(if (mode == ThemeMode.DARK) darkAppColors() else lightAppColors())
    }
}

private fun LazyListScope.section(mode: ThemeMode, title: String, content: @Composable () -> Unit) = item {
    AppTheme(themeMode = mode) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle(title)
            content()
        }
    }
}

/** The theme switch that used to be a long-press anywhere. Writes the real setting, so it sticks. */
@Composable
private fun ThemeSwitcher() {
    val current by SettingsStore.theme.collectAsState()
    Row(
        Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ThemeChoice.entries.forEach { choice ->
            AppButton(
                text = choice.name,
                onClick = { SettingsStore.setTheme(choice) },
                modifier = Modifier.weight(1f),
                size = AppButtonSize.Small,
                variant = if (choice == current) AppButtonVariant.Primary else AppButtonVariant.Outline,
            )
        }
    }
}


/** Generic format(pattern) — one method, the pattern decides the output. */
@Composable
private fun FormatShowcase() {
    val sample = LocalDateTime(2026, 7, 12, 17, 8, 42)   // Sun 12 Jul 2026, 17:08:42
    val cases = listOf(
        "mm" to "just the minute",
        "HH" to "just the hour (24h)",
        "HH:mm" to "time",
        "h:mm a" to "time (12h)",
        "dd/MM/yyyy" to "date",
        "yyyy-MM-dd" to "date (ISO)",
        "HH:mm:ss" to "time w/ seconds",
        "dd/MM/yyyy HH:mm" to "date + time",
    )
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionTitle("Generic format(pattern)")
        Text("sample = $sample", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
        cases.forEach { (pattern, note) ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "\"$pattern\"",
                    modifier = Modifier.fillMaxWidth(0.42f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text("→ ${sample.format(pattern)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            }
            Text(note, fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f))
        }
    }
}

@Composable
private fun ButtonShowcase() {
    AppButtonVariant.entries.forEach { variant ->
        Text(variant.name, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AppButton("Large", {}, variant = variant, size = AppButtonSize.Large)
            AppButton("Med", {}, variant = variant, size = AppButtonSize.Medium)
            AppButton("Sm", {}, variant = variant, size = AppButtonSize.Small)
        }
    }
    Text("States", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        AppButton("Disabled", {}, enabled = false)
        AppButton("Loading", {}, isProcessing = true)
        AppButton("Icons", {}, leftIcon = { Text("★") }, rightIcon = { Text("→") })
    }
}

/** All 12 time points in day order — enum order IS the sort order. */
@Composable
private fun MiqatAllShowcase() {
    AppTileGroup(
        items = Miqat.entries.map { m ->
            AppTileItem(
                title = stringResource(m.labelRes),
                subtitle = m.category.name.lowercase() + if (m.isPrayer) " · tracked" else "",
                leading = {
                    Box(
                        Modifier.size(40.dp).background(m.color, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(m.icon, contentDescription = m.name, tint = m.onColor, modifier = Modifier.size(20.dp))
                    }
                },
                onClick = {},
            )
        },
    )
}

/** Same data sliced by category — what each screen would pick from. */
@Composable
private fun MiqatGroupsShowcase() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(
            "PRAYERS (Tracker, logging, notifications)" to Miqat.PRAYERS,
            "SOLAR (timeline extras: Sunrise/Ishraq/Zawal/Sunset)" to Miqat.SOLAR,
            "NIGHT (Midnight, Last third)" to Miqat.NIGHT,
            "RAMADAN (Imsak)" to Miqat.RAMADAN,
        ).forEach { (label, group) ->
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                group.forEach { m -> AccentChip(stringResource(m.labelRes), m.color, m.onColor, m.icon) }
            }
        }
    }
}

@Composable
private fun AccentChip(label: String, color: Color, onColor: Color, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(40.dp).background(color, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, tint = onColor, modifier = Modifier.size(20.dp))
        }
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun TrackerStatusShowcase() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PrayerTrackerStatus.entries.forEach { AccentChip(it.label, it.color, it.onColor, it.icon) }
    }
}

@Composable
private fun TimeStatusShowcase() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MiqatTimeStatus.entries.forEach { AccentChip(it.label, it.color, it.onColor, it.icon) }
    }
}

@Composable
private fun CalcMethodShowcase() {
    AppTileGroup(
        items = CalculationMethod.entries.map { m ->
            val isha = m.ishaAngle?.let { "Isha ${it}°" } ?: "Isha ${m.ishaIntervalMinutes} min"
            AppTileItem(title = m.label, subtitle = "${m.region} · Fajr ${m.fajrAngle}° · $isha")
        },
    )
}

@Composable
private fun ConfigEnumsShowcase() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val c = MaterialTheme.colorScheme.onBackground
        Text("Madhab: " + Madhab.entries.joinToString { it.label }, fontSize = 13.sp, color = c)
        Text("High-lat: " + HighLatRule.entries.joinToString { it.label }, fontSize = 13.sp, color = c)
        Text("Rounding: " + AdhanRoundingStyle.entries.joinToString { it.label }, fontSize = 13.sp, color = c)
    }
}

@Composable
private fun TileShowcase() {
    AppTileGroup(
        title = "General",
        items = listOf(
            AppTileItem(title = "Notifications", subtitle = "Before, at-time & Jamaat", onClick = {}),
            AppTileItem(title = "Calculation Method", subtitle = "Umm al-Qura", onClick = {}),
            AppTileItem(title = "Language", trailing = { Text("English") }, onClick = {}),
        ),
    )
    AppTileGroup(
        title = "Appearance",
        items = listOf(
            AppTileItem(title = "Theme", subtitle = "Dark", selected = true, onClick = {}),
            AppTileItem(title = "Primary Color", badge = { Text("New") }, onClick = {}),
        ),
    )
    AppTile(title = "About Miqat", subtitle = "Standalone tile", onClick = {})
}

/** The transient message that replaces a toast — Android has one, iOS has none, so this is both. */
@Composable
private fun NoticeShowcase() {
    val notice = LocalNotice.current
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Notice")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppButton(
                "Saved",
                onClick = {
                    notice.show(
                        title = "Saved to gallery",
                        icon = Lucide.Download,
                        variant = AppTileVariant.Success,
                        actionLabel = "Undo",
                        onAction = {},
                    )
                },
                modifier = Modifier.weight(1f),
                size = AppButtonSize.Small,
                variant = AppButtonVariant.Outline,
            )
            AppButton(
                "Not saved",
                onClick = {
                    notice.show(
                        title = "Photo access was declined",
                        message = "Allow it in Settings to save your ayah image. Until then the Save button will keep asking.",
                        icon = Lucide.Info,
                        variant = AppTileVariant.Warning,
                        dismissible = true,
                    )
                },
                modifier = Modifier.weight(1f),
                size = AppButtonSize.Small,
                variant = AppButtonVariant.Outline,
            )
        }
        AppButton(
            "Plain",
            onClick = { notice.show("Copied") },
            modifier = Modifier.fillMaxWidth(),
            size = AppButtonSize.Small,
            variant = AppButtonVariant.Outline,
        )
    }
}

/** Every tile variant, in the live theme — group title, leading icon and fill all follow it. */
@Composable
private fun TileVariantShowcase() {
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SectionTitle("Tile variants")
        AppTileVariant.entries.forEach { v ->
            AppTileGroup(
                title = v.name,
                variant = v,
                items = listOf(
                    AppTileItem(
                        title = "Battery is restricted",
                        subtitle = "Reminders may arrive late",
                        leadingIcon = Lucide.Info,
                        onClick = {},
                    ),
                    AppTileItem(title = "Second row", trailing = { Text("value") }, onClick = {}),
                ),
            )
        }
        AppTileGroup(
            title = "Mixed — a row overrides its group",
            items = listOf(
                AppTileItem(title = "Inherits Normal", leadingIcon = Lucide.Info, onClick = {}),
                AppTileItem(title = "Says Warning", variant = AppTileVariant.Warning, leadingIcon = Lucide.Info, onClick = {}),
                AppTileItem(title = "Says Error", variant = AppTileVariant.Error, leadingIcon = Lucide.Info, onClick = {}),
            ),
        )
    }
}

/** Standalone-waqf lab: the sign as its own inline run, tuned live with sliders. */
@Composable
private fun WaqfLab() {
    val fam = FontFamily(Font(Res.font.indopak_nastaleeq))
    val size = 40.sp
    var widthEm by remember { mutableStateOf(0.8f) }
    var xOffset by remember { mutableStateOf(0f) }
    var yOffset by remember { mutableStateOf(0f) }
    val inline = mapOf(
        "waqf" to InlineTextContent(Placeholder(widthEm.em, 1.em, PlaceholderVerticalAlign.TextCenter)) {
            Box(Modifier.fillMaxSize().offset(x = xOffset.dp, y = yOffset.dp), contentAlignment = Alignment.Center) {
                Text(
                    "\u0020\u06DA", // space + jeem, the font's own seated form
                    fontFamily = fam, fontSize = size, maxLines = 1, softWrap = false,
                    overflow = TextOverflow.Visible,
                    modifier = Modifier.wrapContentSize(unbounded = true),
                )
            }
        }
    )
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Waqf lab", fontSize = 12.sp)

        Text("raw", fontSize = 10.sp)
        Text("هُوَ ۚ اَلْحَیُّ الْقَیُّوْمُ", fontFamily = fam, fontSize = size)

        Text("inline run", fontSize = 10.sp)
        Text(buildAnnotatedString {
            append("هُوَ")
            appendInlineContent("waqf", "\u06DA")
            append("اَلْحَیُّ الْقَیُّوْمُ")
        }, inlineContent = inline, fontFamily = fam, fontSize = size)

        Text("width ${"$"}{(widthEm * 100).toInt()} / 100 em", fontSize = 10.sp)
        Slider(widthEm, { widthEm = it }, valueRange = 0.2f..2f)
        Text("y offset ${"$"}{yOffset.toInt()} dp", fontSize = 10.sp)
        Slider(yOffset, { yOffset = it }, valueRange = -40f..40f)
    }
}

/** Preview of the store-update sheet, with a no-op update action. */
@Composable
private fun UpdateSheetLab() {
    var open by remember { mutableStateOf(false) }
    AppButton(
        text = "Update sheet",
        onClick = { open = true },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        variant = AppButtonVariant.Outline,
    )
    if (open) UpdateSheet(onUpdate = { open = false }, onDismiss = { open = false })
}

/** Highlight color picker + a live preview of the tint drawn behind a sample ayah. */
@Composable
private fun HighlightShowcase() {
    var sel by remember { mutableStateOf(HighlightColor.Green) }
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HighlightColorRow(sel) { sel = it }
        Box(Modifier.clip(RoundedCornerShape(6.dp)).background(sel.tint(colors.background)).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text("ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ", fontFamily = FontFamily(Font(QuranDefaults.FONT.res)), fontSize = 24.sp)
        }
    }
}

/** Every AppActionGroup usage: the three width modes, states, two-up, and a standalone Single cell. */
@Composable
private fun ActionShowcase() {
    // Fill — cells split the full width equally (best for a small fixed set)
    AppActionGroup(
        title = "Fill (2-4, full width)",
        width = ActionWidth.Fill,
        items = listOf(
            AppActionItem("Play", Lucide.Play) {},
            AppActionItem("Bookmark", Lucide.Bookmark) {},
            AppActionItem("Share", Lucide.Share2) {},
        ),
    )
    // Wrap — cells size to content, start-aligned
    AppActionGroup(
        title = "Wrap (content width)",
        width = ActionWidth.Wrap,
        items = listOf(
            AppActionItem("Play", Lucide.Play) {},
            AppActionItem("Bookmark", Lucide.Bookmark) {},
            AppActionItem("Share", Lucide.Share2) {},
        ),
    )
    // Scroll — many items, row scrolls horizontally on overflow
    AppActionGroup(
        title = "Scroll (overflow)",
        width = ActionWidth.Scroll,
        items = listOf(
            AppActionItem("Play", Lucide.Play) {},
            AppActionItem("Bookmark", Lucide.Bookmark) {},
            AppActionItem("Tafsir", Lucide.BookOpen) {},
            AppActionItem("Highlight", Lucide.Highlighter) {},
            AppActionItem("Like", Lucide.Heart) {},
            AppActionItem("Save", Lucide.Download) {},
            AppActionItem("Info", Lucide.Info) {},
            AppActionItem("Share", Lucide.Share2) {},
        ),
    )
    // states — a selected cell and a badge dot (Fill)
    AppActionGroup(
        title = "Selected + badge",
        width = ActionWidth.Fill,
        items = listOf(
            AppActionItem("Bookmark", Lucide.Bookmark, selected = true) {},
            AppActionItem("Like", Lucide.Heart, badge = {
                Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.error, CircleShape))
            }) {},
            AppActionItem("Info", Lucide.Info) {},
        ),
    )
    // standalone Single cell (all-round corners)
    AppAction("Single", Lucide.Star, modifier = Modifier.fillMaxWidth(0.3f), position = ActionPosition.Single) {}
}

@Composable
private fun ColorPalette(c: AppColors) {
    colorRows(c).forEach { (name, color) ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(52.dp).background(color, RoundedCornerShape(8.dp)))
            Text(name, modifier = Modifier.fillMaxWidth(0.45f), fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
            Text(color.toHex(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun SectionTitle(text: String) =
    Text(text, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)

private fun Color.toHex(): String =
    "#" + toArgb().toUInt().toString(16).padStart(8, '0').uppercase()

private fun colorRows(c: AppColors): List<Pair<String, Color>> = listOf(
    "primary" to c.primary, "onPrimary" to c.onPrimary,
    "primaryContainer" to c.primaryContainer, "onPrimaryContainer" to c.onPrimaryContainer,
    "secondary" to c.secondary, "onSecondary" to c.onSecondary,
    "error" to c.error, "onError" to c.onError,
    "success" to c.success, "successContainer" to c.successContainer,
    "info" to c.info, "infoContainer" to c.infoContainer,
    "warning" to c.warning, "warningContainer" to c.warningContainer,
    "scaffoldBackground" to c.scaffoldBackgroundColor, "card" to c.cardColor,
    "appbar" to c.appbarColor, "shadow" to c.shadow,
    "neutral" to c.neutral, "neutralContainer" to c.neutralContainer,
    "neutralVariant" to c.neutralVariant, "neutralMuted" to c.neutralMuted,
)

// Backup lab: fill the phone with recognisable data, wipe it again, and jump to the backup screen,
// so an upload and a restore can be checked end to end without living with the app for months.
@Composable
private fun BackupLab() {
    val nav = LocalAppNavigator.current
    val scope = rememberCoroutineScope()
    val tracker = koinInject<TrackerRepository>()
    val bookmarks = koinInject<BookmarksStore>()
    val notes = koinInject<NotesStore>()
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Backup lab", fontWeight = FontWeight.SemiBold)
        AppButton(
            text = "Seed test data",
            onClick = {
                scope.launch {
                    val today = Now.date()
                    for (back in 1..110) {
                        val d = today.minus(back, DateTimeUnit.DAY)
                        Miqat.PRAYERS.forEachIndexed { i, p ->
                            tracker.setStatus(d, p, if ((back + i) % 6 == 0) PrayerTrackerStatus.PrayedWithJamaat else PrayerTrackerStatus.PrayedOnTime)
                        }
                    }
                    listOf(2 to 255, 36 to 1, 55 to 13, 112 to 1).forEach { (s, a) -> bookmarks.toggle(s, a) }
                    notes.set(2, 255, "Ayat al-Kursi: memorise by Friday")
                    notes.set(94, 5, "with hardship comes ease")
                    SettingsStore.setTimeFormat(TimeFormat.TwentyFour)
                    SettingsStore.setStreakEnabled(true)
                }
            },
            variant = AppButtonVariant.Outline,
            modifier = Modifier.fillMaxWidth(),
        )
        AppButton(
            text = "Wipe local data (fresh phone)",
            onClick = {
                scope.launch {
                    tracker.wipe()
                    val saved = bookmarks.keys.value
                    listOf(2 to 255, 36 to 1, 55 to 13, 112 to 1).forEach { (s, a) -> if ("$s:$a" in saved) bookmarks.toggle(s, a) }
                    notes.set(2, 255, ""); notes.set(94, 5, "")
                    SettingsStore.setTimeFormat(TimeFormat.Twelve)
                }
            },
            variant = AppButtonVariant.ErrorOutline,
            modifier = Modifier.fillMaxWidth(),
        )
        AppButton(text = "Open backup", onClick = { nav.navigate(AppRoute.Backup) }, modifier = Modifier.fillMaxWidth())
    }
}

// Sheet lab: drag the blur and dim for the current theme, open a sample sheet over real rows to judge it.
// The numbers you settle on go into OverlayStyle as the shipped defaults.
@Composable
private fun SheetLab() {
    val dark = AppTheme.colors.isDark
    var open by remember { mutableStateOf(false) }
    val blur = if (dark) OverlayStyle.blurDark else OverlayStyle.blurLight
    val scrim = if (dark) OverlayStyle.scrimDark else OverlayStyle.scrimLight
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Sheet lab · ${if (dark) "dark" else "light"}", fontWeight = FontWeight.SemiBold)
        Text("Blur ${blur.value.toInt()} dp", fontSize = 12.sp)
        Slider(
            value = blur.value, valueRange = 0f..40f,
            onValueChange = { v -> if (dark) OverlayStyle.blurDark = v.dp else OverlayStyle.blurLight = v.dp },
        )
        Text("Dim ${(scrim * 100).toInt()} %", fontSize = 12.sp)
        Slider(
            value = scrim, valueRange = 0f..0.8f,
            onValueChange = { v -> if (dark) OverlayStyle.scrimDark = v else OverlayStyle.scrimLight = v },
        )
        AppButton(text = "Open sample sheet", onClick = { open = true }, modifier = Modifier.fillMaxWidth(), variant = AppButtonVariant.Outline)
    }
    if (open) AppBottomSheet(onDismiss = { open = false }, title = "Sample sheet", subtitle = "Blur ${blur.value.toInt()} dp · dim ${(scrim * 100).toInt()} %") {
        AppTileGroup(
            modifier = Modifier.padding(top = 4.dp),
            items = listOf(
                AppTileItem(title = "Light: ${OverlayStyle.blurLight.value.toInt()} dp, ${(OverlayStyle.scrimLight * 100).toInt()} %"),
                AppTileItem(title = "Dark: ${OverlayStyle.blurDark.value.toInt()} dp, ${(OverlayStyle.scrimDark * 100).toInt()} %"),
            ),
        )
    }
}
