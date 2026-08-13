package com.kodeelite.nooreislam.feature.quran.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Palette
import com.composables.icons.lucide.Settings2
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.constants.defaults.QuranDefaults
import com.kodeelite.nooreislam.core.locale.tr
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.core.util.toArabicIndic
import com.kodeelite.nooreislam.core.util.toJuzKey
import com.kodeelite.nooreislam.core.util.toSurahKey
import com.kodeelite.nooreislam.feature.quran.data.Ayah
import com.kodeelite.nooreislam.feature.quran.data.NotesStore
import com.kodeelite.nooreislam.feature.quran.data.QuranRepository
import com.kodeelite.nooreislam.feature.quran.data.QuranStore
import com.kodeelite.nooreislam.feature.quran.presentation.components.AutoScrollControl
import com.kodeelite.nooreislam.feature.quran.presentation.components.AyahActionSheet
import com.kodeelite.nooreislam.feature.quran.presentation.components.CollectionPickerSheet
import com.kodeelite.nooreislam.feature.quran.presentation.components.HighlightQuickPicker
import com.kodeelite.nooreislam.feature.quran.presentation.components.KeepScreenOn
import com.kodeelite.nooreislam.feature.quran.presentation.components.KeepScreenOnIndicator
import com.kodeelite.nooreislam.feature.quran.presentation.components.NoteEditorSheet
import com.kodeelite.nooreislam.feature.quran.presentation.components.QuranCalligraphy
import com.kodeelite.nooreislam.feature.quran.presentation.components.QuranThemePickerSheet
import com.kodeelite.nooreislam.feature.quran.presentation.components.ReaderSettingsSheet
import com.kodeelite.nooreislam.feature.quran.presentation.components.RukuBlock
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.back
import com.kodeelite.nooreislam.resources.quran_juz
import com.kodeelite.nooreislam.resources.quran_surah_name
import com.kodeelite.nooreislam.resources.reading_settings
import com.kodeelite.nooreislam.resources.surah_number_ayah_number
import com.kodeelite.nooreislam.resources.theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

// whole Quran as one continuous scroll, verses paged 100 at a time, grouped into rukus by the UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(surah: Int = 1, ayah: Int = 1) {
    val nav = LocalAppNavigator.current
    val ayahs = remember { mutableStateListOf<Ayah>() }
    val listState = rememberLazyListState()

    // load the whole Quran once; LazyColumn only renders what's on screen, so this stays cheap
    LaunchedEffect(Unit) {
        if (ayahs.isEmpty()) ayahs.addAll(QuranRepository.all())
    }

    // stays true only after the list has loaded AND jumped to the target — the splash overlay covers until then
    var scrolled by remember { mutableStateOf(false) }

    val colors = AppTheme.colors
    val store = koinInject<QuranStore>()
    val autoScrollEnabled by store.autoScrollEnabled.collectAsState()
    val autoScrollPaused by store.autoScrollPaused.collectAsState()
    val autoScrollPxPerTick by store.autoScrollPxPerTick.collectAsState()
    val keepScreenOn by store.keepScreenOn.collectAsState()
    KeepScreenOn(enabled = keepScreenOn)
    // global collapse state for the reader's floating bottom controls — currently only auto-scroll
    // drives it, but the (<) tab it powers is meant to gather any future floating action too
    var controlsCollapsed by remember { mutableStateOf(false) }

    // autoScrollEnabled lives in the store, which outlives this screen (Koin singleton) — reset it on every
    // fresh entry so the reader always opens paused, even if it was left running on a previous visit
    LaunchedEffect(Unit) { store.stopAutoScroll() }

    // any manual drag stops auto-scroll — never fight the user's own gesture
    LaunchedEffect(listState) {
        listState.interactionSource.interactions.collect { if (it is DragInteraction.Start) store.stopAutoScroll() }
    }
    LaunchedEffect(autoScrollEnabled, autoScrollPaused, autoScrollPxPerTick) {
        if (!autoScrollEnabled || autoScrollPaused) return@LaunchedEffect
        while (isActive) {
            listState.scrollBy(autoScrollPxPerTick)
            delay(store.autoScrollTickInterval)
        }
    }

    // chrome (app bar + auto-scroll bar) visibility: normal reading hides while actively scrolling and
    // reappears the instant scrolling settles — no need to reverse direction to get it back, and it hides
    // again the moment scrolling resumes, so it never "locks" open at the cost of reading space. Auto-scroll
    // starting hides it immediately (no reveal flash) — tap the screen to bring it back briefly, which then
    // auto-hides again after a couple seconds of no interaction
    var chromeRevealed by remember { mutableStateOf(true) }
    LaunchedEffect(autoScrollEnabled) { if (autoScrollEnabled) chromeRevealed = false }
    LaunchedEffect(autoScrollEnabled, chromeRevealed) {
        if (autoScrollEnabled && chromeRevealed) {
            delay(2000.milliseconds)
            chromeRevealed = false
        }
    }
    // forced visible until the initial load+jump settles (hidden behind the splash anyway)
    val chromeVisible = !scrolled || if (autoScrollEnabled) chromeRevealed else !listState.isScrollInProgress
    var topBarHeightPx by remember { mutableStateOf(0) }
    val topBarHeight = with(LocalDensity.current) { topBarHeightPx.toDp() }
    val surahFont = FontFamily(Font(Res.font.quran_surah_name)) // top-bar surah name
    val juzFont = FontFamily(Font(Res.font.quran_juz))
    val rukus = remember(ayahs.size) { groupByRuku(ayahs) }
    // for each ruku: its number within its surah, and whether it's the surah's last ruku (then no "next")
    val rukuMeta = remember(rukus) {
        val meta = ArrayList<Pair<Int, Boolean>>(rukus.size)
        var surah = -1
        var n = 0
        rukus.forEach { r ->
            val s = r.first().surah
            n = if (s != surah) {
                surah = s; 1
            } else n + 1
            meta.add(n to false)
        }
        for (i in rukus.indices) {
            val lastInSurah = i == rukus.lastIndex || rukus[i + 1].first().surah != rukus[i].first().surah
            meta[i] = meta[i].first to lastInSurah
        }
        meta
    }

    // every jump landing — ruku top or exact ayah — keeps this much air above it instead of
    // touching the bar; a negative scroll offset is how a LazyList shows space above the item
    val jumpBreathPx = with(LocalDensity.current) { 12.dp.toPx() }
    // jump to the ruku holding the opened ayah, once; then the user scrolls freely both ways
    var justJumpedAyah by remember { mutableStateOf<Ayah?>(null) }
    LaunchedEffect(rukus) {
        if (rukus.isNotEmpty()) {
            val target = rukus.indexOfFirst { r -> r.any { it.surah == surah && it.ayah == ayah } }.coerceAtLeast(0)
            // target > 0 means a real deep link (Jump To, search, bookmarks, notes…) — flash it on landing.
            // A plain default open (Al-Fatihah, target 0) never flashes.
            if (target > 0) {
                listState.scrollToItem(target, -jumpBreathPx.roundToInt())
                justJumpedAyah = rukus[target].firstOrNull { it.surah == surah && it.ayah == ayah }
            }
            scrolled = true
        }
    }
    // blink then clear — same look as a manual tap-select, just self-dismissing
    LaunchedEffect(justJumpedAyah) {
        if (justJumpedAyah != null) {
            delay(1500.milliseconds)
            justJumpedAyah = null
        }
    }

    // for jumps triggered from within this same reader instance (e.g. "Go to surah start") — scrolls
    // in place instead of pushing a new QuranReaderScreen, so the back stack never grows from it
    val scope = rememberCoroutineScope()
    fun jumpTo(targetSurah: Int, targetAyah: Int) {
        val target = rukus.indexOfFirst { r -> r.any { it.surah == targetSurah && it.ayah == targetAyah } }.coerceAtLeast(0)
        scope.launch {
            listState.scrollToItem(target, -jumpBreathPx.roundToInt())
            justJumpedAyah = rukus.getOrNull(target)?.firstOrNull { it.surah == targetSurah && it.ayah == targetAyah }
        }
    }

    var selected by remember { mutableStateOf<Ayah?>(null) }
    var quickHighlight by remember { mutableStateOf<Ayah?>(null) } // long-press → floating color strip
    var viewingNote by remember { mutableStateOf<Ayah?>(null) } // tap the note glyph → stub preview, real editor later
    var pickingCollectionFor by remember { mutableStateOf<Ayah?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var showTheme by remember { mutableStateOf(false) }
    val notesStore = koinInject<NotesStore>()
    val noteMap by notesStore.noteMap.collectAsState()
    var expanded by remember(selected) { mutableStateOf(false) }
    val header by remember(rukus) { derivedStateOf { rukus.getOrNull(listState.firstVisibleItemIndex)?.firstOrNull() } }
    val blurRadius by animateDpAsState(if (expanded) 14.dp else 0.dp, label = "pageBlur")

    // "Resume" only updates after a real dwell, not a quick search-and-glance visit — save whatever
    // ayah is on screen when the reader closes, but only if this visit lasted long enough to count
    var dwelledLongEnough by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(QuranDefaults.READING_DWELL_MS.milliseconds)
        dwelledLongEnough = true
    }
    // set by the explicit "Set last read" action — when true, that specific ayah wins and this
    // screen's own exit must not immediately overwrite it with whatever ayah is merely on screen
    var lastReadSetManually by remember { mutableStateOf(false) }
    val latestHeader by rememberUpdatedState(header)
    DisposableEffect(Unit) {
        onDispose {
            if (dwelledLongEnough && !lastReadSetManually) latestHeader?.let { store.recordLastRead(it.surah, it.ayah) }
        }
    }

    Box(Modifier.fillMaxSize().background(colors.background)) {
        Box(Modifier.fillMaxSize().blur(blurRadius)) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                val tapAway = if ((selected != null && !expanded) || autoScrollEnabled) {
                    Modifier.pointerInput(selected, expanded, autoScrollEnabled) {
                        detectTapGestures {
                            if (selected != null && !expanded) selected = null
                            if (autoScrollEnabled) chromeRevealed = true
                        }
                    }
                } else Modifier
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().then(tapAway),
                    // reserved regardless of chromeVisible — the app bar overlays this space, never resizes it
                    contentPadding = PaddingValues(top = topBarHeight),
                ) {
                    items(rukus.size) { i ->
                        val ruku = rukus[i]
                        // juz of the ayah before this ruku, so RukuBlock can mark a juz that begins inside it
                        val prevJuz = if (i == 0) ruku.first().juz else rukus[i - 1].last().juz
                        val (numInSurah, lastInSurah) = rukuMeta[i]
                        RukuBlock(
                            ruku, numInSurah, if (lastInSurah) null else numInSurah + 1, prevJuz, selected ?: quickHighlight,
                            onSelect = { selected = if (selected == it) null else it },
                            onLongSelect = { quickHighlight = it },
                            onNoteTap = { viewingNote = it },
                            flashTarget = justJumpedAyah,
                            // no per-ayah refinement — every jump lands on the ruku's top exactly like
                            // opening a surah at ayah 1, with the flash marking the ayah in its context;
                            // pinning the ayah to the very top scrolled the surah header and context away
                            targetAyah = null,
                            onTargetLocated = {},
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = chromeVisible,
                modifier = Modifier.align(Alignment.TopCenter),
                enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { -it },
                exit = fadeOut(tween(220)) + slideOutVertically(tween(220)) { -it },
            ) {
                CenterAlignedTopAppBar(
                    modifier = Modifier.onSizeChanged { topBarHeightPx = it.height },
                    title = {
                        if (!autoScrollEnabled) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(header?.let { "${it.juz.toArabicIndic()} - " } ?: "", color = colors.primary, fontSize = 14.sp)
                                    Text(header?.juz?.toJuzKey() ?: "", fontFamily = juzFont, color = colors.primary, fontSize = 14.sp)
                                }
                                Text("  .  ", color = colors.primary, fontSize = 14.sp)
                                header?.let { Text(it.surah.toSurahKey(), fontFamily = surahFont, fontSize = 28.sp, color = colors.primary) }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton({ nav.back() }) {
                            Icon(
                                tr(Lucide.ChevronLeft, Lucide.ChevronRight),
                                stringResource(Res.string.back),
                                tint = colors.onSurface
                            )
                        }
                    },
                    actions = {
                        if (!autoScrollEnabled) {
                            IconButton({ showTheme = true }) {
                                Icon(
                                    Lucide.Palette,
                                    stringResource(Res.string.theme),
                                    tint = colors.onSurface
                                )
                            }
                            IconButton({ showSettings = true }) {
                                Icon(
                                    Lucide.Settings2,
                                    stringResource(Res.string.reading_settings),
                                    tint = colors.onSurface
                                )
                            }
                        }
                    },
                )
            }
        }

        selected?.let { ayah ->
            AyahActionSheet(
                label = stringResource(Res.string.surah_number_ayah_number, ayah.surah, ayah.ayah),
                ayah = ayah,
                onShareAsImage = { nav.navigate(AppRoute.Studio(ayah.surah, ayah.ayah)) },
                onHighlight = { quickHighlight = ayah; selected = null },
                onNote = { viewingNote = ayah; selected = null },
                onAddToCollection = { pickingCollectionFor = ayah; selected = null },
                onGoToSurahStart = { jumpTo(ayah.surah, 1) },
                onSetLastRead = {
                    lastReadSetManually = true
                    store.recordLastRead(ayah.surah, ayah.ayah)
                    nav.back()
                },
                onExpandedChange = { expanded = it },
                onDismiss = { selected = null },
            )
        }

        quickHighlight?.let { HighlightQuickPicker(it) { quickHighlight = null } }

        viewingNote?.let { ayah ->
            NoteEditorSheet(
                surah = ayah.surah,
                ayah = ayah.ayah,
                initialText = noteMap["${ayah.surah}:${ayah.ayah}"] ?: "",
                store = notesStore,
                onDismiss = { viewingNote = null },
            )
        }

        pickingCollectionFor?.let { ayah ->
            CollectionPickerSheet(ayah = ayah, onDismiss = { pickingCollectionFor = null })
        }

        if (showSettings) {
            ReaderSettingsSheet(onDismiss = { showSettings = false })
        }

        if (showTheme) {
            QuranThemePickerSheet(onDismiss = { showTheme = false })
        }

        if (selected == null && quickHighlight == null && viewingNote == null && pickingCollectionFor == null && !showSettings && !showTheme) {
            AutoScrollControl(
                isScrollInProgress = listState.isScrollInProgress,
                collapsed = controlsCollapsed,
                onCollapsedChange = { controlsCollapsed = it },
                onToggle = { store.toggleAutoScroll() },
                onSpeedDown = { store.decreaseAutoScrollSpeed() },
                onSpeedUp = { store.increaseAutoScrollSpeed() },
            )
            // true once the global controls-collapse group has actually taken effect (today that only
            // happens via auto-scroll's own idle timer) — the single source of truth for "the screen
            // cleaner is active," shared by the (<) tab below and every member of the group, so any
            // future floating action can hide the same way just by reading this same flag
            val controlsHidden = autoScrollEnabled && controlsCollapsed
            if (keepScreenOn) {
                KeepScreenOnIndicator(
                    checked = keepScreenOn,
                    onToggle = { store.toggleKeepScreenOn() },
                    // same "hide only for a genuine manual drag, not auto-scroll's own programmatic
                    // scroll" rule AutoScrollControl's pill already follows, so both hide/reappear together
                    manualScrolling = !autoScrollEnabled && listState.isScrollInProgress,
                    hiddenByCollapse = controlsHidden,
                )
            }
            // global collapse tab for the reader's floating controls — a "screen cleaner": its own
            // bottom-end spot, kept separate from KeepScreenOnIndicator's bottom-start one so the two
            // never read as a single toggling button, but every member of the group (including
            // KeepScreenOnIndicator, and any future floating action) hides together with this tab and
            // reappears together when it's tapped
            AnimatedVisibility(
                visible = controlsHidden,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it },
                exit = fadeOut(tween(220)) + slideOutVertically(tween(220)) { it },
            ) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(colors.cardColor)
                        .clickable { controlsCollapsed = false },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), null, tint = colors.primary)
                }
            }
        }

        // calligraphy overlay on top while loading + jumping, so the scroll jump is hidden; fades out when ready
        AnimatedVisibility(visible = !scrolled, enter = EnterTransition.None, exit = fadeOut()) {
            QuranCalligraphy()
        }
    }
}

// split verses into rukus (a run ends where endsRuku is true)
private fun groupByRuku(ayahs: List<Ayah>): List<List<Ayah>> {
    val out = ArrayList<List<Ayah>>()
    var run = ArrayList<Ayah>()
    for (a in ayahs) {
        run.add(a); if (a.endsRuku) {
            out.add(run); run = ArrayList()
        }
    }
    if (run.isNotEmpty()) out.add(run)
    return out
}
