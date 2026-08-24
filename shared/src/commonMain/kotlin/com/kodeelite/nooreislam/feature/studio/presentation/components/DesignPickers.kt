package com.kodeelite.nooreislam.feature.studio.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.kodeelite.nooreislam.core.assets.DownloadService
import com.kodeelite.nooreislam.core.constants.AssetDirs
import com.composables.icons.lucide.AlignCenter
import com.composables.icons.lucide.AlignLeft
import com.composables.icons.lucide.AlignRight
import com.composables.icons.lucide.Ban
import com.composables.icons.lucide.CircleDot
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Moon
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.SeparatorHorizontal
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.Tent
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.colorpicker.ColorPickerSheet
import com.kodeelite.nooreislam.core.components.colorpicker.RecentColors
import com.kodeelite.nooreislam.feature.quran.data.QuranFont
import com.kodeelite.nooreislam.feature.studio.data.CanvasPattern
import com.kodeelite.nooreislam.feature.studio.data.GradientStore
import com.kodeelite.nooreislam.feature.studio.data.ImageStore
import com.kodeelite.nooreislam.feature.studio.data.StudioGradient
import com.kodeelite.nooreislam.feature.studio.data.StudioImage
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.generate
import com.kodeelite.nooreislam.resources.generate_again
import com.kodeelite.nooreislam.resources.mode_background
import com.kodeelite.nooreislam.resources.mode_gradient
import com.kodeelite.nooreislam.resources.show_less
import com.kodeelite.nooreislam.resources.tap_generate_for_fresh_gradients
import com.kodeelite.nooreislam.resources.view_all
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Compact Professional Slider */
@Composable
fun ArtSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    valueDisplay: String = ""
) {
    val colors = AppTheme.colors
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.width(70.dp)) {
            Text(
                label.uppercase(),
                color = colors.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(valueDisplay, color = colors.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = colors.primary,
                activeTrackColor = colors.primary,
                inactiveTrackColor = colors.onSurface.copy(alpha = 0.08f)
            ),
            modifier = Modifier.weight(1f).height(30.dp)
        )
    }
}

@Composable
fun FontPicker(current: QuranFont, onChange: (QuranFont) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(QuranFont.entries) { f ->
            SmallFontChip(f, f == current) { onChange(f) }
        }
    }
}

@Composable
fun ImageGridPicker(images: List<StudioImage>, selected: String?, onSelect: (String?) -> Unit) {
    val downloads = koinInject<DownloadService>()
    val progress by downloads.progress.collectAsState()

    // cells show the tiny thumb; the original moves only when its download button is tapped,
    // and an image can be picked only once its file is local
    ExpandableStrip(
        title = stringResource(Res.string.mode_background), all = images,
        isSelected = { it.url == selected }, noneSelected = selected == null,
        onNone = { onSelect(null) },
        onPick = { image ->
            if (ImageStore.isDownloaded(image)) onSelect(image.url)
            else downloads.download(image.url, AssetDirs.STUDIO_IMAGES, image.fileName)
        },
    ) { image ->
        Box(Modifier.fillMaxSize()) {
            AsyncImage(image.thumbUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            ComboSwatches(image, Modifier.align(Alignment.BottomStart).padding(6.dp))

            val part = progress["${AssetDirs.STUDIO_IMAGES}/${image.fileName}"]
            if (part != null || !ImageStore.isDownloaded(image)) {
                Box(
                    Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (part != null) CircularProgressIndicator(
                        progress = { part },
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.25f),
                        strokeWidth = 2.dp,
                    )
                    else Icon(Lucide.Download, null, Modifier.size(18.dp), tint = Color.White)
                }
            }
        }
    }
}

// tiny bg+text pairs sampled from the photo — each ring is a base tone with its readable on-color inside,
// so the card previews the color combinations it gives you before you pick it.
@Composable
private fun ComboSwatches(image: StudioImage, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        val n = minOf(3, image.colors.size, image.onColors.size)
        repeat(n) { i ->
            Box(
                Modifier.size(14.dp).clip(CircleShape).background(image.colors[i])
                    .border(1.dp, Color.White.copy(alpha = 0.65f), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Box(Modifier.size(5.dp).clip(CircleShape).background(image.onColors[i])) }
        }
    }
}

@Composable
fun GradientStripPicker(selected: StudioGradient?, onSelect: (StudioGradient?) -> Unit, gradients: List<StudioGradient>) {
    Column(Modifier.fillMaxWidth()) {
        ExpandableStrip(
            title = stringResource(Res.string.mode_gradient), all = gradients,
            isSelected = { it == selected }, noneSelected = selected == null,
            onNone = { onSelect(null) }, onPick = { onSelect(it) },
        ) { g ->
            Box(Modifier.fillMaxSize().background(Brush.linearGradient(g.stops)))
        }
        Spacer(Modifier.size(12.dp))
        GenerateGradients(onSelect)
    }
}

// fresh gradients on demand: empty until "Generate", then 4-5 harmonious ones; tap again for a new batch
@Composable
private fun GenerateGradients(onSelect: (StudioGradient?) -> Unit) {
    val colors = AppTheme.colors
    var seed by remember { mutableStateOf(0) }
    var batch by remember { mutableStateOf<List<StudioGradient>>(emptyList()) }
    Column(Modifier.fillMaxWidth()) {
        SectionHeader(
            stringResource(Res.string.generate),
            actionLabel = if (batch.isEmpty()) stringResource(Res.string.generate) else stringResource(Res.string.generate_again),
            onAction = { seed++; batch = GradientStore.generate(5, seed) })
        if (batch.isEmpty()) {
            Text(
                stringResource(Res.string.tap_generate_for_fresh_gradients),
                color = colors.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        } else {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(batch) { g ->
                    StripItem(Modifier.width(88.dp).height(112.dp), selected = false, onClick = { onSelect(g) }) {
                        Box(Modifier.fillMaxSize().background(Brush.linearGradient(g.stops)))
                    }
                }
            }
        }
    }
}

@Composable
fun ColorStripPicker(selected: Color, onSelect: (Color) -> Unit, palette: List<Color> = emptyList()) {
    // curated, on-brand neutrals shown after the image's own colors
    val neutrals = listOf(
        Color.White, Color(0xFFF5F0E6),   // white, cream
        Color.Black, Color(0xFF14151A),   // black, near-black
        Color(0xFFC9A24B), Color(0xFFD8B48C), // gold, warm sand
        Color(0xFF1F6F4A), Color(0xFF8CA9C9), // deep green, slate blue
    )
    val base = palette + neutrals
    // recently picked custom colors sit first, as ordinary swatches (dedup against the built-ins)
    val recents = RecentColors.colors.filter { r -> base.none { it.copy(alpha = 1f).value == r.value } }
    val combined = recents + base
    var pickerOpen by remember { mutableStateOf(false) }

    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item { PlusNode { pickerOpen = true } }   // custom color — any shade
        items(combined) { c -> ColorNode(c, c.copy(alpha = 1f) == selected.copy(alpha = 1f)) { onSelect(c) } }
    }

    if (pickerOpen) {
        ColorPickerSheet(
            initialColor = selected,
            onConfirm = { RecentColors.add(it); onSelect(it.copy(alpha = selected.alpha)) },  // keep the field's own alpha
            onDismiss = { pickerOpen = false },
            presets = combined,
        )
    }
}

@Composable
private fun PlusNode(onClick: () -> Unit) {
    val colors = AppTheme.colors
    Box(
        Modifier.size(36.dp).clip(CircleShape)
            .border(1.5.dp, colors.onSurface.copy(alpha = 0.25f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) { Icon(Lucide.Plus, null, tint = colors.onSurface, modifier = Modifier.size(18.dp)) }
}

@Composable
private fun ColorNode(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(36.dp).clip(CircleShape)
            .background(color)
            .border(2.dp, if (isSelected) AppTheme.colors.primary else color.copy(alpha = 0.15f), CircleShape)
            .clickable { onClick() }
    )
}

@Composable
fun AspectRatioStrip(
    current: com.kodeelite.nooreislam.feature.studio.data.StudioAspectRatio,
    onSelect: (com.kodeelite.nooreislam.feature.studio.data.StudioAspectRatio) -> Unit
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        com.kodeelite.nooreislam.feature.studio.data.StudioAspectRatio.entries.forEach { ar ->
            val isSelected = ar == current
            val colors = AppTheme.colors
            Box(
                Modifier.height(36.dp).weight(1f).clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) colors.primary else colors.onSurface.copy(alpha = 0.05f))
                    .clickable { onSelect(ar) },
                contentAlignment = Alignment.Center
            ) {
                Text(ar.label, color = if (isSelected) colors.onPrimary else colors.onSurface, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AlignmentToggle(current: TextAlign, onSelect: (TextAlign) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(
            TextAlign.Left to Lucide.AlignLeft,
            TextAlign.Center to Lucide.AlignCenter,
            TextAlign.Right to Lucide.AlignRight
        ).forEach { (a, icon) ->
            val isSelected = a == current
            val colors = AppTheme.colors
            Box(
                Modifier.height(44.dp).weight(1f).clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) colors.primary.copy(alpha = 0.1f) else colors.onSurface.copy(alpha = 0.05f))
                    .border(1.5.dp, if (isSelected) colors.primary else Color.Transparent, RoundedCornerShape(10.dp))
                    .clickable { onSelect(a) },
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = if (isSelected) colors.primary else colors.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
        }
    }
}

@Composable
fun PatternPicker(current: CanvasPattern, onSelect: (CanvasPattern) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CanvasPattern.entries.forEach { p ->
            val isSelected = p == current
            val colors = AppTheme.colors
            Box(
                Modifier.height(44.dp).weight(1f).clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) colors.primary.copy(alpha = 0.1f) else colors.onSurface.copy(alpha = 0.05f))
                    .border(1.5.dp, if (isSelected) colors.primary else Color.Transparent, RoundedCornerShape(10.dp))
                    .clickable { onSelect(p) },
                contentAlignment = Alignment.Center
            ) {
                Text(p.name, color = if (isSelected) colors.primary else colors.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SmallFontChip(font: QuranFont, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val fam = FontFamily(Font(font.res))
    Column(
        Modifier.width(72.dp).height(80.dp).clip(RoundedCornerShape(10.dp))
            .background(if (selected) colors.primary.copy(alpha = 0.15f) else colors.onSurface.copy(alpha = 0.08f))
            .border(1.5.dp, if (selected) colors.primary else Color.Transparent, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick).padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // fixed box so a big or small font can't change the chip's height
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(font.sample, fontFamily = fam, fontSize = 18.sp, color = colors.onSurface, maxLines = 1)
        }
        Text(
            font.label,
            color = if (selected) colors.primary else colors.onSurfaceVariant,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

data class StickerDef(val id: String, val icon: ImageVector)

val STICKER_DEFS = listOf(
    StickerDef("crescent", Lucide.Moon),
    StickerDef("star", Lucide.Star),
    StickerDef("mosque", Lucide.Tent),
    StickerDef("lantern", Lucide.Flame),
    StickerDef("beads", Lucide.CircleDot),
    StickerDef("divider", Lucide.SeparatorHorizontal)
)

@Composable
fun StickerPicker(onAdd: (String) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(STICKER_DEFS) { s ->
            Box(
                Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
                    .background(AppTheme.colors.onSurface.copy(alpha = 0.05f))
                    .clickable { onAdd(s.id) },
                contentAlignment = Alignment.Center
            ) { Icon(s.icon, null, tint = AppTheme.colors.primary, modifier = Modifier.size(24.dp)) }
        }
    }
}

private const val STRIP_GRID_COLS = 3   // columns when a strip expands to a grid (match the templates grid)

/**
 * Header (title left, View all / Show less right) + a horizontal strip that expands IN PLACE into a
 * width-filling grid (cells weighted, no right gap). Picking an item collapses back to the strip;
 * switching studio action disposes this and resets it to collapsed automatically.
 */
@Composable
private fun <T> ExpandableStrip(
    title: String,
    all: List<T>,
    isSelected: (T) -> Boolean,
    noneSelected: Boolean,
    onNone: () -> Unit,
    onPick: (T) -> Unit,
    content: @Composable (T) -> Unit,   // fills the cell (AsyncImage / gradient box / …)
) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        val canExpand = all.size > STRIP_GRID_COLS
        SectionHeader(
            title,
            actionLabel = if (canExpand) (if (expanded) stringResource(Res.string.show_less) else stringResource(Res.string.view_all)) else null,
            onAction = if (canExpand) ({ expanded = !expanded }) else null,
        )
        if (!expanded) {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item { StripItem(Modifier.width(88.dp).height(112.dp), noneSelected, onClick = { onNone() }) { NoneContent() } }
                items(all) { t -> StripItem(Modifier.width(88.dp).height(112.dp), isSelected(t), onClick = { onPick(t) }) { content(t) } }
            }
        } else {
            val slots: List<@Composable () -> Unit> = buildList {
                add { StripItem(Modifier.fillMaxSize(), noneSelected, onClick = { onNone(); expanded = false }) { NoneContent() } }
                all.forEach { t ->
                    add {
                        StripItem(
                            Modifier.fillMaxSize(),
                            isSelected(t),
                            onClick = { onPick(t); expanded = false }) { content(t) }
                    }
                }
            }
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(max = 240.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                slots.chunked(STRIP_GRID_COLS).forEach { rowSlots ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowSlots.forEach { s -> Box(Modifier.weight(1f).height(112.dp)) { s() } }   // weighted → fills width, no right gap
                        repeat(STRIP_GRID_COLS - rowSlots.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun StripItem(mod: Modifier, selected: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        mod.clip(shape).border(1.5.dp, if (selected) AppTheme.colors.primary else Color.Transparent, shape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun NoneContent() {
    Box(Modifier.fillMaxSize().background(AppTheme.colors.onSurface.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
        Icon(Lucide.Ban, null, tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}
