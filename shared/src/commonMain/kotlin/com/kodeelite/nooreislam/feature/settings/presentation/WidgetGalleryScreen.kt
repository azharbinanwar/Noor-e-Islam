package com.kodeelite.nooreislam.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppButtonSize
import com.kodeelite.nooreislam.core.components.AppButtonVariant
import com.kodeelite.nooreislam.core.components.AppCard
import com.kodeelite.nooreislam.core.locale.tr
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.feature.widget.WidgetConfig
import com.kodeelite.nooreislam.feature.widget.WidgetKind
import com.kodeelite.nooreislam.feature.widget.WidgetStyle
import com.kodeelite.nooreislam.feature.widget.pinWidget
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.back
import com.kodeelite.nooreislam.resources.dimension_format
import com.kodeelite.nooreislam.resources.widget_add
import com.kodeelite.nooreislam.resources.widget_customize
import com.kodeelite.nooreislam.resources.widgets
import org.jetbrains.compose.resources.stringResource

// Widget gallery: browse every widget shown in its neutral default look. Customize opens the sheet and drops the
// widget straight to the home screen with the chosen look; Add to home drops it and lets you tune it at drop time.
// The listing never changes — every widget's style is its own, saved per appWidgetId, never shared.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetGalleryScreen() {
    val nav = LocalAppNavigator.current
    val c = AppTheme.colors
    val sample = WidgetStyle() // neutral look for the listing previews; never edited
    var customizing by remember { mutableStateOf<WidgetKind?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.widgets)) },
                navigationIcon = {
                    IconButton(onClick = { nav.back() }) {
                        Icon(
                            tr(Lucide.ChevronLeft, Lucide.ChevronRight),
                            stringResource(Res.string.back)
                        )
                    }
                },
            )
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp)) {
            WidgetKind.entries.forEach { kind ->
                AppCard(modifier = Modifier.padding(bottom = 12.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(kind.displayName(), fontWeight = FontWeight.SemiBold, color = c.onSurface, modifier = Modifier.weight(1f))
                        Box(
                            Modifier.clip(RoundedCornerShape(8.dp)).background(c.onSurface.copy(alpha = 0.06f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                stringResource(Res.string.dimension_format, kind.cols, kind.rows),
                                color = c.onSurface.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                        WidgetPreview(kind, sample.color, sample.alpha)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppButton(
                            stringResource(Res.string.widget_customize),
                            onClick = { customizing = kind },
                            modifier = Modifier.weight(1f),
                            variant = AppButtonVariant.Outline,
                            size = AppButtonSize.Medium
                        )
                        AppButton(
                            stringResource(Res.string.widget_add),
                            onClick = { pinWidget(kind) },
                            modifier = Modifier.weight(1f),
                            size = AppButtonSize.Medium
                        )
                    }
                }
            }
        }
    }

    customizing?.let { kind ->
        WidgetCustomizeSheet(
            kind = kind,
            initial = WidgetStyle(),
            // Confirm drops the widget straight to home with this per-instance look; the listing stays untouched.
            onConfirm = { style -> WidgetConfig.stashPending(style); pinWidget(kind); customizing = null },
            onDismiss = { customizing = null },
        )
    }
}
