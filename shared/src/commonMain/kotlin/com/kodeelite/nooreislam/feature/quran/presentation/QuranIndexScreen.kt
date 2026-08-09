package com.kodeelite.nooreislam.feature.quran.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.Navigation
import com.composables.icons.lucide.Palette
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Settings
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.components.ActionWidth
import com.kodeelite.nooreislam.core.components.AppActionGroup
import com.kodeelite.nooreislam.core.components.AppActionItem
import com.kodeelite.nooreislam.core.components.AppChip
import com.kodeelite.nooreislam.core.components.LocalDrawerState
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.feature.quran.data.QuranStore
import com.kodeelite.nooreislam.feature.quran.presentation.components.QuranSearchSheet
import com.kodeelite.nooreislam.feature.quran.presentation.components.QuranThemePickerSheet
import com.kodeelite.nooreislam.feature.quran.presentation.components.SurahPickerSheet
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.jump_to
import com.kodeelite.nooreislam.resources.menu
import com.kodeelite.nooreislam.resources.quran
import com.kodeelite.nooreislam.resources.resume
import com.kodeelite.nooreislam.resources.search
import com.kodeelite.nooreislam.resources.settings
import com.kodeelite.nooreislam.resources.theme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QuranIndexScreen() {
    val nav = LocalAppNavigator.current
    val edition = koinInject<AppEdition>()
    val quranStore = koinInject<QuranStore>()
    val lastRead by quranStore.lastRead.collectAsState()
    val tabs = QuranTab.entries
    val pager = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val drawerState = LocalDrawerState.current
    val density = LocalDensity.current
    var showTheme by remember { mutableStateOf(false) }
    var showJumpTo by remember { mutableStateOf(false) }
    var showSearchQuran by remember { mutableStateOf(false) }

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
                    // the Quran app has no drawer to open — nothing else to navigate to from here
                    if (edition != AppEdition.QURAN) IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Lucide.Menu, stringResource(Res.string.menu))
                    }
                },
                actions = {
                    // no drawer means no other way in — the Quran app gets a direct settings entry instead
                    if (edition == AppEdition.QURAN) IconButton(onClick = { nav.navigate(AppRoute.Settings) }) {
                        Icon(Lucide.Settings, stringResource(Res.string.settings))
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
                    AppActionGroup(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        width = ActionWidth.Fill,
                        items = listOf(
                            AppActionItem(
                                label = stringResource(Res.string.resume),
                                icon = Lucide.BookOpen,
                                onClick = {
                                    val target = lastRead
                                    nav.navigate(if (target != null) AppRoute.QuranReader(target.first, target.second) else AppRoute.QuranReader())
                                },
                            ),
                            AppActionItem(label = stringResource(Res.string.jump_to), icon = Lucide.Navigation, onClick = { showJumpTo = true }),
                            AppActionItem(label = stringResource(Res.string.search), icon = Lucide.Search, onClick = { showSearchQuran = true }),
                            AppActionItem(label = stringResource(Res.string.theme), icon = Lucide.Palette, onClick = { showTheme = true }),
                        ),
                    )
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
        QuranThemePickerSheet(onDismiss = { showTheme = false })
    }

    if (showJumpTo) {
        SurahPickerSheet(
            onOpen = { surah, ayah -> nav.navigate(AppRoute.QuranReader(surah, ayah)) },
            onDismiss = { showJumpTo = false },
        )
    }

    if (showSearchQuran) {
        QuranSearchSheet(
            onOpen = { surah, ayah -> nav.navigate(AppRoute.QuranReader(surah, ayah)) },
            onDismiss = { showSearchQuran = false },
        )
    }
}
