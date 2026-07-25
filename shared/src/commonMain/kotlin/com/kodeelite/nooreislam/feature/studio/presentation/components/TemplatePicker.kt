package com.kodeelite.nooreislam.feature.studio.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.feature.quran.data.QuranFont
import com.kodeelite.nooreislam.feature.studio.data.StudioConfig
import com.kodeelite.nooreislam.feature.studio.data.StudioTemplate
import com.kodeelite.nooreislam.feature.studio.data.TemplateStore
import org.jetbrains.compose.resources.Font

// one-tap look picker: header (Templates + View all / Show less), a strip that expands IN PLACE into a
// width-filling grid. Picking collapses back to the strip; switching studio action resets it.
@Composable
fun TemplatePicker(current: StudioConfig, onPick: (StudioConfig) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        SectionHeader("Templates", actionLabel = if (expanded) "Show less" else "View all", onAction = { expanded = !expanded })
        if (!expanded) {
            LazyRow(
                Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(TemplateStore.presets) { t -> TemplateChip(t, Modifier.width(88.dp)) { onPick(t.applyTo(current)) } }
            }
        } else {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(max = 300.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TemplateStore.presets.chunked(3).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { t ->
                            Box(Modifier.weight(1f)) {
                                TemplateChip(
                                    t,
                                    Modifier.fillMaxWidth()
                                ) { onPick(t.applyTo(current)); expanded = false }
                            }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }   // fill the row so cells stay aligned, no right gap
                    }
                }
            }
        }
        Spacer(Modifier.size(12.dp))
        GenerateTemplates(current, onPick)
    }
}

// fresh looks on demand: empty until "Generate" is tapped, then 4-5 constrained-random looks; tap again for a new batch
@Composable
private fun GenerateTemplates(current: StudioConfig, onPick: (StudioConfig) -> Unit) {
    val colors = AppTheme.colors
    var seed by remember { mutableStateOf(0) }
    var batch by remember { mutableStateOf<List<StudioTemplate>>(emptyList()) }
    Column(Modifier.fillMaxWidth()) {
        SectionHeader(
            "Generate",
            actionLabel = if (batch.isEmpty()) "Generate" else "Generate again",
            onAction = { seed++; batch = TemplateStore.generate(seed = 1000 + seed) })
        if (batch.isEmpty()) {
            Text(
                "Tap Generate for fresh looks",
                color = colors.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        } else {
            LazyRow(
                Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(batch) { t -> TemplateChip(t, Modifier.width(88.dp)) { onPick(t.applyTo(current)) } }
            }
        }
    }
}

@Composable
private fun TemplateChip(t: StudioTemplate, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth().height(112.dp).clip(RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            // background: photo (blurred only if the template blurs) + scrim, or a gradient/solid
            if (t.imageUrl != null) {
                val previewBlur = if (t.blur > 0f) 4.dp else 0.dp
                AsyncImage(t.imageUrl, null, Modifier.matchParentSize().blur(previewBlur), contentScale = ContentScale.Crop)
                Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = t.overlay)))
            } else {
                val stops = if (t.bg.size > 1) t.bg else listOf(t.bg.first(), t.bg.first())
                Box(Modifier.matchParentSize().background(Brush.verticalGradient(stops)))
            }
            // translucent/solid card + text, like the real design
            Column(
                Modifier.fillMaxWidth().padding(8.dp).clip(RoundedCornerShape(8.dp)).background(t.card).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("ع", fontFamily = FontFamily(Font(QuranFont.DEFAULT.res)), color = t.accent, fontSize = 14.sp)
                Bar(t.textColor, 1f)
                Bar(t.textColor, 1f)
                Bar(t.textColor, 0.6f, Modifier.align(Alignment.CenterHorizontally))
            }
        }
        Spacer(Modifier.size(6.dp))
        Text(t.name, color = colors.onSurfaceVariant, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun Bar(color: Color, widthFraction: Float, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(widthFraction).height(6.dp).clip(RoundedCornerShape(3.dp)).background(color.copy(alpha = 0.9f)))
}
