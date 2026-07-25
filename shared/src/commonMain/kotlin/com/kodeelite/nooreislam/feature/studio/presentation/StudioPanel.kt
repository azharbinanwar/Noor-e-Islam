package com.kodeelite.nooreislam.feature.studio.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme

// Floating editor card: section label + scrollable body slot + the mode bar footer.
@Composable
fun BoxScope.StudioPanel(
    studioMode: StudioMode,
    onSelectMode: (StudioMode) -> Unit,
    content: @Composable () -> Unit,
) {
    val colors = AppTheme.colors
    Column(
        // side margins, rounded all corners, nav-bar-safe, sheet-matching surface
        Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(colors.surfaceContainerHigh)
            .animateContentSize(alignment = Alignment.BottomCenter)   // grow upward; footer (chips) stays locked at the bottom
            .padding(bottom = 8.dp)
    ) {
        // Templates / Background / Gradient render their own SectionHeader — skip the panel label for them
        if (studioMode != StudioMode.Templates && studioMode != StudioMode.BgImage && studioMode != StudioMode.BgGradient) {
            Text(
                studioMode.label.uppercase(),
                color = colors.onSurfaceVariant.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp)
            )
        }
        Box(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                content()
            }
        }
        ModeBar(studioMode, onSelectMode)
    }
}
