package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeftRight
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.FastForward
import com.composables.icons.lucide.FolderPlus
import com.composables.icons.lucide.Highlighter
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Link
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mic
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Repeat
import com.composables.icons.lucide.RotateCcw
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.StickyNote
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.ActionWidth
import com.kodeelite.nooreislam.core.components.AppActionGroup
import com.kodeelite.nooreislam.core.components.AppActionItem
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.SHEET_SCRIM_ALPHA
import com.kodeelite.nooreislam.feature.quran.data.Ayah
import com.kodeelite.nooreislam.feature.quran.data.BookmarksStore
import com.kodeelite.nooreislam.feature.quran.data.HighlightsStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.action_add_note
import com.kodeelite.nooreislam.resources.action_add_to_collection
import com.kodeelite.nooreislam.resources.action_audio
import com.kodeelite.nooreislam.resources.action_bookmark
import com.kodeelite.nooreislam.resources.action_choose_reciter
import com.kodeelite.nooreislam.resources.action_context_revelation
import com.kodeelite.nooreislam.resources.action_go_to_surah_start
import com.kodeelite.nooreislam.resources.action_highlight
import com.kodeelite.nooreislam.resources.action_mark_memorized
import com.kodeelite.nooreislam.resources.action_navigation
import com.kodeelite.nooreislam.resources.action_personal
import com.kodeelite.nooreislam.resources.action_play_from_here
import com.kodeelite.nooreislam.resources.action_play_recitation
import com.kodeelite.nooreislam.resources.action_related_ayahs
import com.kodeelite.nooreislam.resources.action_repeat_loop
import com.kodeelite.nooreislam.resources.action_set_last_read
import com.kodeelite.nooreislam.resources.action_share_image
import com.kodeelite.nooreislam.resources.action_share_link
import com.kodeelite.nooreislam.resources.action_sharing
import com.kodeelite.nooreislam.resources.action_study
import com.kodeelite.nooreislam.resources.more_actions
import com.kodeelite.nooreislam.resources.play
import com.kodeelite.nooreislam.resources.share
import com.kodeelite.nooreislam.resources.share_as_text
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private class QAction(val icon: ImageVector, val labelRes: StringResource)
private class QGroup(val titleRes: StringResource, val items: List<QAction>)

// full action set from the design; ponytail: all shown now, hide non-applicable ones as features land
private val MORE_GROUPS = listOf(
    QGroup(
        Res.string.action_audio, listOf(
            QAction(Lucide.Play, Res.string.action_play_recitation), QAction(Lucide.Repeat, Res.string.action_repeat_loop),
            QAction(Lucide.FastForward, Res.string.action_play_from_here), QAction(Lucide.Mic, Res.string.action_choose_reciter),
        )
    ),
    QGroup(
        Res.string.action_personal, listOf(
            QAction(Lucide.Bookmark, Res.string.action_bookmark), QAction(Lucide.FolderPlus, Res.string.action_add_to_collection),
            QAction(Lucide.StickyNote, Res.string.action_add_note), QAction(Lucide.Highlighter, Res.string.action_highlight),
            QAction(Lucide.Check, Res.string.action_mark_memorized),
        )
    ),
    QGroup(
        Res.string.action_sharing, listOf(
            QAction(Lucide.Share2, Res.string.share_as_text),
            QAction(Lucide.Image, Res.string.action_share_image),
            QAction(Lucide.Link, Res.string.action_share_link),
        )
    ),
    QGroup(
        Res.string.action_navigation, listOf(
            QAction(Lucide.House, Res.string.action_go_to_surah_start), QAction(Lucide.RotateCcw, Res.string.action_set_last_read),
        )
    ),
    QGroup(
        Res.string.action_study, listOf(
            QAction(Lucide.ArrowLeftRight, Res.string.action_related_ayahs), QAction(Lucide.Info, Res.string.action_context_revelation),
        )
    ),
)

// finger-following sheet: peek shows quick actions, drag/expand reveals grouped actions + style/script
@Composable
fun AyahActionSheet(
    label: String,
    ayah: Ayah,
    onShareAsImage: () -> Unit,
    onHighlight: () -> Unit,
    onNote: () -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val density = LocalDensity.current
    val winPx = LocalWindowInfo.current.containerSize.height
    val peekPx = with(density) { 172.dp.toPx() }
    val expandedPx = if (winPx > 0) winPx * 0.80f else with(density) { 560.dp.toPx() }

    val scope = rememberCoroutineScope()
    val bookmarksStore = koinInject<BookmarksStore>()
    val highlightsStore = koinInject<HighlightsStore>()

    val bookmarkedKeys by bookmarksStore.keys.collectAsState()
    val isBookmarked = "${ayah.surah}:${ayah.ayah}" in bookmarkedKeys
    val heightPx = remember { Animatable(peekPx) }
    var atExpanded by remember { mutableStateOf(false) }
    val scrimAlpha by animateFloatAsState(if (atExpanded) SHEET_SCRIM_ALPHA else 0f, label = "scrim")

    fun expand() {
        atExpanded = true; onExpandedChange(true); scope.launch { heightPx.animateTo(expandedPx) }
    }

    fun collapse() {
        atExpanded = false; onExpandedChange(false); scope.launch { heightPx.animateTo(peekPx) }
    }

    fun settle() {
        val h = heightPx.value
        when {
            h < peekPx * 0.6f -> onDismiss() // flung/dragged well below the peek → close
            h > (peekPx + expandedPx) / 2f -> expand()
            else -> collapse()
        }
    }

    Box(Modifier.fillMaxSize()) {
        // scrim scales with how far the sheet is open; only intercepts taps once it's actually visible
        if (scrimAlpha > 0.01f) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = scrimAlpha))
                    .pointerInput(Unit) { detectTapGestures { collapse() } },
            )
        }
        Surface(
            color = colors.surfaceContainerHigh,
            shape = RoundedCornerShape(28.dp), // all corners rounded, like AppBottomSheet
            tonalElevation = 3.dp,
            shadowElevation = 12.dp,
            // float on every side (matches AppBottomSheet): clear the system bars, then a margin all around
            modifier = Modifier.align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 10.dp, vertical = 12.dp)
                .fillMaxWidth()
                .height(with(density) { heightPx.value.toDp() }),
        ) {
            Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
                // drag handle: follows the finger 1:1, settles to peek/expanded (or dismiss) on release
                Box(
                    Modifier.align(Alignment.CenterHorizontally).padding(vertical = 8.dp)
                        .size(width = 44.dp, height = 5.dp).clip(CircleShape).background(colors.outlineVariant)
                        .pointerInput(expandedPx) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { change, dy ->
                                    change.consume()
                                    scope.launch { heightPx.snapTo((heightPx.value - dy).coerceIn(peekPx * 0.4f, expandedPx)) }
                                },
                                onDragEnd = { settle() },
                            )
                        },
                )
                Text(label, color = colors.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
                AppActionGroup(
                    width = ActionWidth.Fill,
                    items = listOf(
                        AppActionItem(stringResource(Res.string.play), Lucide.Play, iconColor = colors.primary) {},
                        AppActionItem(
                            stringResource(Res.string.action_bookmark),
                            Lucide.Bookmark,
                            selected = isBookmarked,
                            iconColor = colors.primary
                        ) {
                            bookmarksStore.toggle(ayah.surah, ayah.ayah)
                            onDismiss()
                        },
                        AppActionItem(stringResource(Res.string.action_highlight), Lucide.Highlighter, iconColor = colors.primary) {
                            val key = "${ayah.surah}:${ayah.ayah}"
                            if (key in highlightsStore.colors.value) highlightsStore.set(ayah.surah, ayah.ayah, null)
                            else highlightsStore.applyDefault(ayah.surah, ayah.ayah)
                            onHighlight()
                        },
                        AppActionItem(stringResource(Res.string.action_add_note), Lucide.Pencil, iconColor = colors.primary) { onNote() },
                        AppActionItem(stringResource(Res.string.share), Lucide.Share2, iconColor = colors.primary) { onShareAsImage() },
                    ),
                )
                // "More" affordance at the peek: tap to open (drag still works too); hidden once expanded
                if (!atExpanded) {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { expand() }.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Lucide.ChevronUp, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(stringResource(Res.string.more_actions), color = colors.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.size(8.dp))
                // grouped actions fill the remaining sheet height and scroll; revealed as the sheet grows
                Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                    MORE_GROUPS.forEach { g ->
                        AppTileGroup(title = stringResource(g.titleRes), items = g.items.map { a ->
                            AppTileItem(
                                title = stringResource(a.labelRes),
                                leadingIcon = a.icon,
                                onClick = {
                                    onDismiss()
                                    if (a.icon == Lucide.Image) onShareAsImage()
                                    if (a.icon == Lucide.StickyNote) onNote()
                                }
                            )
                        })
                    }
                }
            }
        }
    }
}
