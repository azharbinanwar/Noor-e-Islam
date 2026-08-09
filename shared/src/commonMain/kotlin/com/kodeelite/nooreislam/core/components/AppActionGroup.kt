package com.kodeelite.nooreislam.core.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme

/** Where an action sits in a horizontal group — drives corner rounding (start/end differ). */
enum class ActionPosition {
    Single, First, Middle, Last;

    companion object {
        // position of action `index` within a group of `count` — first/last round the outer corners, rest stay square
        fun at(index: Int, count: Int) = when {
            count == 1 -> Single
            index == 0 -> First
            index == count - 1 -> Last
            else -> Middle
        }
    }
}

/** How an [AppActionGroup] spreads its cells across the width. */
enum class ActionWidth {
    Fill,    // split the full width equally (weight) — best for a small fixed set (2-4)
    Wrap,    // size to content (min width), start-aligned — clips if the cells overflow the width
    Scroll,  // size to content (min width), row scrolls horizontally when the cells overflow
}

/** Data for one quick action in an [AppActionGroup]. */
class AppActionItem(
    val label: String,
    val icon: ImageVector,
    val selected: Boolean = false,
    val iconColor: Color? = null,          // custom icon tint (e.g. red for a destructive action); selected still wins
    val badge: (@Composable () -> Unit)? = null,
    val onLongClick: (() -> Unit)? = null,
    val onClick: () -> Unit,
)

/**
 * One quick-action cell — icon over label, centered, theme-filled (no border), grouped corners.
 * The horizontal sibling of [AppTile]; same look, laid on the cross axis.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppAction(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    iconColor: Color? = null,
    badge: (@Composable () -> Unit)? = null,
    position: ActionPosition = ActionPosition.Single,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val c = AppTheme.colors
    val bg = if (selected) c.primary.copy(alpha = 0.10f) else c.cardColor
    // selected highlight wins; otherwise a custom tint, else the muted token softened (not hard black)
    val tint = if (selected) c.primary else iconColor ?: c.onSurfaceVariant.copy(alpha = 0.7f)
    Column(
        modifier
            .clip(actionShapeFor(position))
            .background(bg)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .height(72.8.dp)
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        // fixed height with the icon+label centered as one block, evenly spaced
        verticalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterVertically),
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
            badge?.invoke()
        }
        Text(
            text = label,
            fontSize = 9.24.sp,
            lineHeight = 9.24.sp,
            // trim the line-box padding above/below the glyph so the label isn't taller than it looks
            style = TextStyle(lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both)),
            color = if (selected) c.primary else c.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

/**
 * Borderless grouped quick actions laid horizontally: first cell start-rounded, last end-rounded,
 * middle square, small gaps. Theme-filled, optional section title. The horizontal sibling of [AppTileGroup].
 *
 * [width] picks how the cells fill the row: [ActionWidth.Fill] stretches them equally across the screen,
 * [ActionWidth.Wrap] sizes them to content, [ActionWidth.Scroll] does the same but scrolls on overflow.
 * [minCellWidth] is the floor for Wrap/Scroll cells (ignored by Fill).
 */
@Composable
fun AppActionGroup(
    items: List<AppActionItem>,
    modifier: Modifier = Modifier,
    title: String? = null,
    width: ActionWidth = ActionWidth.Wrap,
    minCellWidth: Dp = 70.4.dp,
) {
    // own container, independent of AppTileGroup/TileGroupShell — no bottom padding added here,
    // the caller decides its own spacing to whatever comes next
    val c = AppTheme.colors
    Column(modifier.fillMaxWidth()) {
        if (title != null) {
            Text(
                text = title,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
                style = MaterialTheme.typography.titleSmall,
                color = c.primary,
                fontWeight = FontWeight.Bold,
            )
        }
        val rowModifier = when (width) {
            ActionWidth.Fill -> Modifier.fillMaxWidth()
            ActionWidth.Wrap -> Modifier
            ActionWidth.Scroll -> Modifier.horizontalScroll(rememberScrollState())
        }
        Row(rowModifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items.forEachIndexed { i, item ->
                val cellModifier = if (width == ActionWidth.Fill) Modifier.weight(1f) else Modifier.widthIn(min = minCellWidth)
                AppAction(
                    label = item.label,
                    icon = item.icon,
                    modifier = cellModifier,
                    selected = item.selected,
                    iconColor = item.iconColor,
                    badge = item.badge,
                    position = ActionPosition.at(i, items.size),
                    onLongClick = item.onLongClick,
                    onClick = item.onClick,
                )
            }
        }
    }
}

// corner rounding for one action's position within its group — shared by every horizontal action row
fun actionShapeFor(pos: ActionPosition): Shape {
    val r = 16.dp
    val m = 4.dp
    return when (pos) {
        ActionPosition.Single -> RoundedCornerShape(r)
        // start/end corners (not left/right) so the rounding mirrors correctly in RTL
        ActionPosition.First -> RoundedCornerShape(topStart = r, bottomStart = r, topEnd = m, bottomEnd = m)
        ActionPosition.Middle -> RoundedCornerShape(m)
        ActionPosition.Last -> RoundedCornerShape(topStart = m, bottomStart = m, topEnd = r, bottomEnd = r)
    }
}
