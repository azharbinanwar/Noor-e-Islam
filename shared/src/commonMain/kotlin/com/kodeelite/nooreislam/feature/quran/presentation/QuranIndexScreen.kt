package com.kodeelite.nooreislam.feature.quran.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.Palette
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppChip
import com.kodeelite.nooreislam.core.components.LocalDrawerState
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.feature.quran.data.QuranStore
import com.kodeelite.nooreislam.feature.quran.presentation.components.QuranThemePickerSheet
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.continue_reading
import com.kodeelite.nooreislam.resources.menu
import com.kodeelite.nooreislam.resources.quran
import com.kodeelite.nooreislam.resources.start_from_beginning
import com.kodeelite.nooreislam.resources.theme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QuranIndexScreen() {
    val nav = LocalAppNavigator.current
    val tabs = QuranTab.entries
    val pager = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val drawerState = LocalDrawerState.current
    val density = LocalDensity.current
    val quranStore = koinInject<QuranStore>()
    val fontSize by quranStore.fontSize.collectAsState()
    val lineSpacing by quranStore.lineSpacing.collectAsState()
    val script by quranStore.font.collectAsState()
    val readingTheme by quranStore.theme.collectAsState()
    var showTheme by remember { mutableStateOf(false) }

    var collapsibleH by remember { mutableIntStateOf(0) } // top bar + continue card (px) — the part that hides
    var headerH by remember { mutableIntStateOf(0) }       // full header incl. the pinned chips (px)
    var offsetY by remember { mutableFloatStateOf(0f) }    // header shift: 0 (open) .. -collapsibleH (chips pinned)

    // collapse the top block on scroll-up (onPreScroll), re-expand on scroll-down once the list is back at top (onPostScroll)
    val conn = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y >= 0f) return Offset.Zero
                val old = offsetY
                offsetY = (offsetY + available.y).coerceIn(-collapsibleH.toFloat(), 0f)
                return Offset(0f, offsetY - old)
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (available.y <= 0f) return Offset.Zero
                val old = offsetY
                offsetY = (offsetY + available.y).coerceIn(-collapsibleH.toFloat(), 0f)
                return Offset(0f, offsetY - old)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.quran)) },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Lucide.Menu, stringResource(Res.string.menu))
                    }
                },
                actions = {
                    IconButton(onClick = { showTheme = true }) {
                        Icon(Lucide.Palette, stringResource(Res.string.theme))
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).clipToBounds().nestedScroll(conn)) {
            // content sits below the header; its top inset shrinks as the collapsible part hides (no gap)
            HorizontalPager(
                state = pager,
                modifier = Modifier.fillMaxSize().padding(top = with(density) { (headerH + offsetY).toDp() }),
            ) { page ->
                when (tabs[page]) {
                    QuranTab.Surahs -> SurahsTab()
                    QuranTab.Juzs -> JuzsTab()
                    QuranTab.Bookmarks -> BookmarksTab()
                    QuranTab.Notes -> NotesTab()
                    QuranTab.Highlights -> HighlightsTab()
                    QuranTab.Collections -> CollectionsTab()
                }
            }

            // header on top: [top bar + continue card] scrolls off, the chip row pins at the top
            Column(
                Modifier.fillMaxWidth().zIndex(1f)
                    .offset { IntOffset(0, offsetY.roundToInt()) }
                    .onSizeChanged { headerH = it.height }
                    .background(AppTheme.colors.background),
            ) {
                Column(Modifier.onSizeChanged { collapsibleH = it.height }) {
                    ContinueCard { nav.navigate(AppRoute.QuranReader()) }
                }
                val chipState = rememberLazyListState()
                LaunchedEffect(pager.currentPage) { chipState.animateScrollToItem(pager.currentPage) } // keep the selected chip in view
                LazyRow(
                    state = chipState,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(tabs.size) { i ->
                        AppChip(tabs[i].label, pager.currentPage == i, { scope.launch { pager.animateScrollToPage(i) } }, tabs[i].icon)
                    }
                }
            }
        }
    }

    if (showTheme) {
        QuranThemePickerSheet(
            fontSize = fontSize,
            onFontChange = { quranStore.setFontSize(it) },
            lineSpacing = lineSpacing,
            onLineSpacingChange = { quranStore.setLineSpacing(it) },
            font = script,
            onFontSelect = { quranStore.setFont(it) },
            theme = readingTheme,
            onThemeSelect = { quranStore.setTheme(it) },
            onDismiss = { showTheme = false },
        )
    }
}

// basic hero — real last-read + progress come with that store
@Composable
private fun ContinueCard(onClick: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        Modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainerHigh).clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Lucide.BookOpen, null, tint = colors.primary)
        Spacer(Modifier.size(12.dp))
        Column {
            Text(stringResource(Res.string.continue_reading), color = colors.onSurface, fontWeight = FontWeight.SemiBold)
            Text(stringResource(Res.string.start_from_beginning), color = colors.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}
