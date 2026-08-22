package com.kodeelite.nooreislam.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.BuildType
import com.kodeelite.nooreislam.core.catalog.Surface
import com.kodeelite.nooreislam.core.catalog.featuresOn
import com.kodeelite.nooreislam.core.components.ActionWidth
import com.kodeelite.nooreislam.core.components.AppActionGroup
import com.kodeelite.nooreislam.core.components.AppActionItem
import com.kodeelite.nooreislam.core.components.LocalDrawerState
import com.kodeelite.nooreislam.core.constants.Place
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.enums.CalculationMethod
import com.kodeelite.nooreislam.core.location.LocationMoveSheet
import com.kodeelite.nooreislam.core.location.LocationResolver
import com.kodeelite.nooreislam.core.location.rememberGeoLocator
import com.kodeelite.nooreislam.core.navigation.LocalNavController
import com.kodeelite.nooreislam.core.store.HomeShortcutStore
import com.kodeelite.nooreislam.core.store.LocationStore
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.home.presentation.components.DailyVerseCard
import com.kodeelite.nooreislam.feature.home.presentation.components.MulkReminderCard
import com.kodeelite.nooreislam.feature.home.presentation.components.SceneDebugOverlay
import com.kodeelite.nooreislam.feature.home.presentation.components.ShortcutPickerSheet
import com.kodeelite.nooreislam.feature.home.presentation.components.StreakCard
import com.kodeelite.nooreislam.feature.home.presentation.components.TodayPrayers
import com.kodeelite.nooreislam.feature.miqat.store.MiqatCalculationStore
import com.kodeelite.nooreislam.feature.miqat.store.MiqatTimesStore
import com.kodeelite.nooreislam.feature.tracker.presentation.components.ExemptionControl
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private val ExpandedHeader = 380.dp
private val CollapsedHeader = 116.dp

@Composable
fun HomeScreen() {
    val place by LocationStore.activePlace.collectAsState()
    val calc by MiqatCalculationStore.calculation.collectAsState()

    // silent GPS check — never prompts, only offers a move when you've actually travelled
    val geo = rememberGeoLocator()
    var moveCandidate by remember { mutableStateOf<Place?>(null) }
    LaunchedEffect(Unit) {
        val fix = geo.current() ?: return@LaunchedEffect
        moveCandidate = LocationResolver.detectMove(LocationStore.activePlace.value, fix)
    }

    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val rangePx = with(density) { (ExpandedHeader - CollapsedHeader).toPx() }
    val fraction = (scroll.value / rangePx).coerceIn(0f, 1f)
    val drawerState = LocalDrawerState.current
    val nav = LocalNavController.current
    val scope = rememberCoroutineScope()
    val streakEnabled by SettingsStore.streakEnabled.collectAsState()
    val clock by Now.now.collectAsState()
    val today by MiqatTimesStore.today.collectAsState()
    val edition = koinInject<AppEdition>()
    val debug = koinInject<BuildType>().isDebug
    val offered = featuresOn(Surface.Home, edition, debug)
    val pinned by HomeShortcutStore.pinned.collectAsState()
    val shortcuts = pinned.mapNotNull { route -> offered.firstOrNull { it.target == route } }
    var editing by remember { mutableStateOf<Int?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
            Spacer(Modifier.height(ExpandedHeader))
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (streakEnabled) StreakCard()
                if (shortcuts.isNotEmpty()) AppActionGroup(
                    items = shortcuts.mapIndexed { i, feature ->
                        AppActionItem(
                            label = stringResource(feature.name),
                            icon = feature.icon,
                            onLongClick = { editing = i },
                            onClick = { nav.navigate(feature.target) },
                        )
                    },
                    width = ActionWidth.Fill,
                )
                TodayPrayers()
                MulkReminderCard()
                DailyVerseCard()
                ExemptionControl()
                Spacer(Modifier.height(8.dp))
            }
        }

        LoopSceneHeader(
            now = clock.time,
            date = clock.date,
            times = today,
            fraction = fraction,
            expandedHeight = ExpandedHeader,
            collapsedHeight = CollapsedHeader,
            onMenuClick = { scope.launch { drawerState.open() } },
        )

        moveCandidate?.let { cand ->
            val newMethod = CalculationMethod.forCountry(cand.countryCode)
            val methodChange = if (newMethod != calc.method) calc.method to newMethod else null
            LocationMoveSheet(
                candidate = cand,
                current = place,
                methodChange = methodChange,
                onUpdate = { switchMethod ->
                    LocationStore.setActive(cand)
                    if (switchMethod) MiqatCalculationStore.setMethod(newMethod)
                    moveCandidate = null
                },
                onKeep = { moveCandidate = null },
            )
        }

        editing?.let { slot ->
            ShortcutPickerSheet(
                current = pinned[slot],
                pinned = pinned,
                onPick = { HomeShortcutStore.replace(slot, it) },
                onDismiss = { editing = null },
            )
        }

        SceneDebugOverlay(Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp))
    }
}
