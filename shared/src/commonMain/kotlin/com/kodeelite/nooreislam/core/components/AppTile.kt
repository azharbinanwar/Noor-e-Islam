package com.kodeelite.nooreislam.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.GripVertical
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppColors
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.locale.tr
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/** What a tile is saying — drives the section title, the leading icon and the row's fill. */
enum class AppTileVariant {
    Normal, Success, Warning, Error;

    fun accentOf(c: AppColors): Color = when (this) {
        Normal -> c.primary
        Success -> c.success
        Warning -> c.warning
        Error -> c.error
    }

    /** Normal keeps the card colour; the rest wash their accent over it. */
    fun containerOf(c: AppColors): Color = if (this == Normal) c.cardColor else accentOf(c).copy(alpha = 0.12f)
}

/** Where a tile sits in a group — drives corner rounding (first/last differ). */
enum class TilePosition {
    Single, First, Middle, Last;

    companion object {
        // position of item `index` within a group of `count` — first/last round the outer corners, rest stay square
        fun at(index: Int, count: Int) = when {
            count == 1 -> Single
            index == 0 -> First
            index == count - 1 -> Last
            else -> Middle
        }
    }
}

// corner rounding for one tile's position within its group — shared by every card-style list item
fun shapeFor(pos: TilePosition): Shape {
    val r = 16.dp
    val m = 4.dp
    return when (pos) {
        TilePosition.Single -> RoundedCornerShape(r)
        TilePosition.First -> RoundedCornerShape(topStart = r, topEnd = r, bottomStart = m, bottomEnd = m)
        TilePosition.Middle -> RoundedCornerShape(m)
        TilePosition.Last -> RoundedCornerShape(topStart = m, topEnd = m, bottomStart = r, bottomEnd = r)
    }
}

/** A small icon action; tiles and group headers render these with one consistent size/tint/spacing. */
class AppIconAction(val icon: ImageVector, val onClick: () -> Unit)

/** Data for one tile in an [AppTileGroup]. */
class AppTileItem(
    val title: String,
    val variant: AppTileVariant? = null,
    val titleFontFamily: FontFamily? = null,
    val subtitle: String? = null,
    val leadingIcon: ImageVector? = null,
    val leadingColor: Color? = null,
    val leading: (@Composable () -> Unit)? = null,
    val trailing: (@Composable () -> Unit)? = null,
    val badge: (@Composable () -> Unit)? = null,
    val actions: List<AppIconAction> = emptyList(),
    val selected: Boolean = false,
    val enabled: Boolean = true,
    val visible: Boolean = true,
    val onClick: (() -> Unit)? = null,
    val onLongClick: (() -> Unit)? = null,
)

/**
 * Borderless settings-style tile — theme-filled background (no border), grouped corners.
 * leading / title+badge / subtitle / trailing-or-chevron, selection tint.
 */
@Composable
fun AppTile(
    title: String,
    modifier: Modifier = Modifier,
    variant: AppTileVariant = AppTileVariant.Normal,
    titleFontFamily: FontFamily? = null,
    subtitle: String? = null,
    subtitleFontFamilyFamily: FontFamily? = null,
    subtitleAlign: TextAlign? = null,
    subtitleMaxLines: Int = Int.MAX_VALUE,
    leadingIcon: ImageVector? = null,
    leadingColor: Color? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
    actions: List<AppIconAction> = emptyList(),
    selected: Boolean = false,
    enabled: Boolean = true,
    // false collapses it rather than removing it — an `if` around the call would unmount it and
    // there would be nothing left to animate
    visible: Boolean = true,
    position: TilePosition = TilePosition.Single,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val everShown = remember { mutableStateOf(false) }
    if (visible) everShown.value = true
    if (!everShown.value) return

    val c = AppTheme.colors
    val bg = if (selected) c.primary.copy(alpha = 0.10f) else variant.containerOf(c)
    AnimatedVisibility(visible = visible, enter = expandVertically(), exit = shrinkVertically()) {
    Column(modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.45f).clip(shapeFor(position)).background(bg)) {
        TileRow(
            title,
            variant,
            titleFontFamily,
            subtitle,
            subtitleFontFamilyFamily,
            subtitleAlign,
            subtitleMaxLines,
            leadingIcon,
            leadingColor,
            leading,
            trailing,
            badge,
            actions,
            selected,
            onClick.takeIf { enabled },
            onLongClick.takeIf { enabled }
        )
    }
    }
}

/**
 * Borderless grouped tiles: first tile top-rounded, last bottom-rounded, middle square,
 * with small gaps. Theme-filled (no border), optional section title, selection tint.
 */
@Composable
fun AppTileGroup(
    items: List<AppTileItem?>,
    modifier: Modifier = Modifier,
    title: String? = null,
    variant: AppTileVariant = AppTileVariant.Normal,
    actions: List<AppIconAction> = emptyList(),
) {
    // nulls are rows that opted out (a granted permission, a hidden dev entry) — with none left there
    // is nothing to head, so the title and its padding go too. It collapses rather than vanishing:
    // the shell stays mounted through the exit, which is what the last row leaving needs.
    val rows = items.filterNotNull()
    val everFilled = remember { mutableStateOf(false) }
    if (rows.isNotEmpty()) everFilled.value = true
    if (!everFilled.value) return
    AnimatedVisibility(visible = rows.isNotEmpty(), enter = expandVertically(), exit = shrinkVertically()) {
        TileGroupShell(modifier, title, variant, actions) {
            // no spacedBy here: a hidden row is still a child, and the arrangement would leave its
            // gap behind. The 4dp rides inside each row instead, so it collapses with it.
            Column(Modifier.fillMaxWidth()) {
                items.forEachIndexed { slot, item ->
                    // a row that turns null keeps its last look while it collapses, so it shrinks
                    // out instead of vanishing and leaving the ones below to jump
                    val last = remember { mutableStateOf(item) }
                    if (item != null) last.value = item
                    val shown = items.take(slot).count { it != null }
                    AnimatedVisibility(
                        visible = item != null,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        Column(Modifier.padding(top = if (shown == 0) 0.dp else 4.dp)) {
                            last.value?.let { Tile(it, positionFor(shown, rows.size.coerceAtLeast(1)), variant) }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Same look as [AppTileGroup], but long-press a tile to lift it and drag up/down to reorder.
 * [onReorder] (from, to) commits on release. Opt-in variant so plain groups stay untouched.
 */
@Composable
fun AppTileGroupReorderable(
    items: List<AppTileItem>,
    onReorder: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    variant: AppTileVariant = AppTileVariant.Normal,
    actions: List<AppIconAction> = emptyList(),
) {
    TileGroupShell(modifier, title, variant, actions) { ReorderableTiles(items, onReorder, variant) }
}

/** Chrome (outer padding + optional section title) for the two tile-group variants — tile-only, not shared with AppActionGroup. */
@Composable
internal fun TileGroupShell(
    modifier: Modifier,
    title: String?,
    variant: AppTileVariant = AppTileVariant.Normal,
    actions: List<AppIconAction> = emptyList(),
    bottomSpace: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    val c = AppTheme.colors
    Column(modifier.fillMaxWidth().padding(bottom = bottomSpace)) {
        if (title != null) {
            Row(
                Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    color = variant.accentOf(c),
                    fontWeight = FontWeight.Bold,
                )
                ActionIcons(actions)
            }
        }
        content()
    }
}

/** Shared renderer for [AppIconAction]s — one look everywhere: 18dp icon, muted tint, 10dp gaps. */
@Composable
private fun ActionIcons(actions: List<AppIconAction>) {
    val c = AppTheme.colors
    actions.forEachIndexed { i, a ->
        if (i > 0) Spacer(Modifier.width(2.dp))
        // IconButton-style: unbounded circular ripple centered on the icon — never a box
        Icon(
            a.icon, null, tint = c.onSurfaceVariant,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, radius = 17.dp),
                    onClick = a.onClick,
                )
                .padding(8.dp)
                .size(18.dp),
        )
    }
}

@Composable
private fun Tile(item: AppTileItem, position: TilePosition, group: AppTileVariant = AppTileVariant.Normal) {
    AppTile(
        title = item.title,
        variant = item.variant ?: group,
        titleFontFamily = item.titleFontFamily,
        subtitle = item.subtitle,
        leadingIcon = item.leadingIcon,
        leadingColor = item.leadingColor,
        leading = item.leading,
        trailing = item.trailing,
        badge = item.badge,
        actions = item.actions,
        selected = item.selected,
        enabled = item.enabled,
        visible = item.visible,
        position = position,
        onClick = item.onClick,
        onLongClick = item.onLongClick,
    )
}

/** Long-press a tile to lift it, then drag up/down to reorder; onReorder(from, to) commits on release. */
@Composable
private fun ReorderableTiles(items: List<AppTileItem>, onReorder: (Int, Int) -> Unit, group: AppTileVariant = AppTileVariant.Normal) {
    var dragIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val heights = remember { mutableStateMapOf<Int, Int>() }
    val spacingPx = with(LocalDensity.current) { 4.dp.toPx() }
    val scope = rememberCoroutineScope()
    val di = dragIndex
    val unit = (((if (di != null) heights[di] else null) ?: 1).toFloat().coerceAtLeast(1f)) + spacingPx
    val target = if (di != null) (di + (dragOffset / unit).roundToInt()).coerceIn(0, items.lastIndex) else -1
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items.forEachIndexed { i, item ->
            val lifted = di == i
            // non-dragged tiles between the lift origin and the current target slide to open the gap
            val gapShift = when {
                di == null || lifted -> 0f
                di < target && i in (di + 1)..target -> -unit
                di > target && i in target until di -> unit
                else -> 0f
            }
            // animate the gap while dragging; snap closed on commit so neighbours don't re-slide
            val animatedShift by animateFloatAsState(gapShift, animationSpec = if (di == null) snap() else spring(), label = "reorderShift")
            Box(
                Modifier
                    .onSizeChanged { heights[i] = it.height }
                    .zIndex(if (lifted) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (lifted) dragOffset else animatedShift
                        if (lifted) {
                            scaleX = 1.02f; scaleY = 1.02f
                        }
                    }
                    .pointerInput(items.size, i) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { dragIndex = i; dragOffset = 0f },
                            onDrag = { change, amount -> change.consume(); dragOffset += amount.y },
                            onDragEnd = {
                                val h = (heights[i] ?: 1).toFloat().coerceAtLeast(1f) + spacingPx
                                val to = (i + (dragOffset / h).roundToInt()).coerceIn(0, items.lastIndex)
                                scope.launch {
                                    animate(dragOffset, (to - i) * h, animationSpec = tween(180)) { v, _ -> dragOffset = v } // settle into the gap
                                    if (to != i) onReorder(i, to)
                                    dragIndex = null; dragOffset = 0f
                                }
                            },
                            onDragCancel = {
                                scope.launch {
                                    animate(dragOffset, 0f, animationSpec = tween(150)) { v, _ -> dragOffset = v }
                                    dragIndex = null; dragOffset = 0f
                                }
                            },
                        )
                    },
            ) {
                AppTile(
                    title = item.title,
                    subtitle = item.subtitle,
                    leading = {
                        Icon(
                            Lucide.GripVertical,
                            "Drag to reorder",
                            tint = AppTheme.colors.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailing = item.trailing,
                    badge = item.badge,
                    selected = item.selected,
                    position = positionFor(i, items.size),
                )
            }
        }
    }
}

private fun positionFor(index: Int, size: Int): TilePosition = when {
    size == 1 -> TilePosition.Single
    index == 0 -> TilePosition.First
    index == size - 1 -> TilePosition.Last
    else -> TilePosition.Middle
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TileRow(
    title: String,
    variant: AppTileVariant,
    titleFontFamily: FontFamily?,
    subtitle: String?,
    subtitleFontFamilyFamily: FontFamily?,
    subtitleAlign: TextAlign?,
    subtitleMaxLines: Int,
    leadingIcon: ImageVector?,
    leadingColor: Color?,
    leading: (@Composable () -> Unit)?,
    trailing: (@Composable () -> Unit)?,
    badge: (@Composable () -> Unit)?,
    actions: List<AppIconAction>,
    selected: Boolean,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
) {
    val c = AppTheme.colors
    var row = Modifier.fillMaxWidth()
    if (onClick != null || onLongClick != null) row = row.combinedClickable(onClick = { onClick?.invoke() }, onLongClick = onLongClick)
    Row(
        modifier = row.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            val col = leadingColor ?: variant.accentOf(c)
            Box(Modifier.size(38.dp).clip(CircleShape).background(col.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(leadingIcon, null, tint = col, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
        } else if (leading != null) {
            leading()
            Spacer(Modifier.width(16.dp))
        }
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f, fill = false),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) c.primary else c.onSurface,
                    fontFamily = titleFontFamily,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                )
                if (badge != null) {
                    Spacer(Modifier.width(8.dp))
                    badge()
                }
            }
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.onSurfaceVariant,
                    fontFamily = subtitleFontFamilyFamily,
                    textAlign = subtitleAlign,
                    maxLines = subtitleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp)); trailing()
        }
        if (actions.isNotEmpty()) {
            Spacer(Modifier.width(8.dp)); ActionIcons(actions)
        }
        if (trailing == null && actions.isEmpty() && onClick != null) {
            Spacer(Modifier.width(8.dp))
            // a vector icon (not a text glyph) so RTL doesn't double-mirror it; tr picks the forward direction
            Icon(
                tr(Lucide.ChevronRight, Lucide.ChevronLeft),
                null,
                tint = if (selected) c.primary else c.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
