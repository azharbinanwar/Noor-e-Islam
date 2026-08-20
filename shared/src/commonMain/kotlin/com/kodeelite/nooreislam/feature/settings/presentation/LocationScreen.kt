package com.kodeelite.nooreislam.feature.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPinOff
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Navigation
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.X
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppTextField
import com.kodeelite.nooreislam.core.components.AppTile
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.core.components.TilePosition
import com.kodeelite.nooreislam.core.constants.Place
import com.kodeelite.nooreislam.core.constants.countryLabel
import com.kodeelite.nooreislam.core.constants.defaults.MiqatDefaults
import com.kodeelite.nooreislam.core.enums.CalculationMethod
import com.kodeelite.nooreislam.core.locale.tr
import com.kodeelite.nooreislam.core.location.nearestTo
import com.kodeelite.nooreislam.core.location.rememberGeoLocator
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.core.permissions.AppPermission
import com.kodeelite.nooreislam.core.components.AppTileVariant
import com.kodeelite.nooreislam.core.permissions.LocationPermissionTile
import com.kodeelite.nooreislam.core.components.LocalNotice
import com.kodeelite.nooreislam.core.location.LocationServiceTile
import com.kodeelite.nooreislam.core.permissions.LocationDeniedSheet
import com.kodeelite.nooreislam.core.permissions.PermissionStatus
import com.kodeelite.nooreislam.core.permissions.rememberPermissionService
import com.kodeelite.nooreislam.core.store.LocationStore
import com.kodeelite.nooreislam.feature.miqat.store.MiqatCalculationStore
import com.kodeelite.nooreislam.feature.settings.presentation.components.MethodSwitchSheet
import com.kodeelite.nooreislam.resources.Res
import org.jetbrains.compose.resources.getString
import com.kodeelite.nooreislam.resources.open_settings
import com.kodeelite.nooreislam.resources.location_is_off_sub
import com.kodeelite.nooreislam.resources.location_is_off
import com.composables.icons.lucide.LocateOff
import com.kodeelite.nooreislam.resources.notif_needs_attention
import com.kodeelite.nooreislam.resources.back
import com.kodeelite.nooreislam.resources.clear_search
import com.kodeelite.nooreislam.resources.location
import com.kodeelite.nooreislam.resources.location_permission_needed
import com.kodeelite.nooreislam.resources.location_permission_rationale
import com.kodeelite.nooreislam.resources.no_cities_found
import com.kodeelite.nooreislam.resources.saved
import com.kodeelite.nooreislam.resources.search_city
import com.kodeelite.nooreislam.resources.search_city_hint_message
import com.kodeelite.nooreislam.resources.search_city_hint_title
import com.kodeelite.nooreislam.resources.suggested
import com.kodeelite.nooreislam.resources.try_a_different_search
import com.kodeelite.nooreislam.resources.use_current_location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource

/** Same place regardless of coord precision — dedupe key for the saved list. */
private fun Place.sameAs(other: Place) = name == other.name && countryCode == other.countryCode

/**
 * Location picker — a tidy hub: "Use current location" + "Search for a city" (opens a full-screen search
 * over the bundled offline catalog, ~49k cities), then your saved cities with the active one checked.
 * Picking a city saves + activates it. ponytail: mock GPS + state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen() {
    val nav = LocalAppNavigator.current
    val c = AppTheme.colors
    // Single source of truth — the repo resolves Prefs ?: MiqatDefaults and emits on every change.
    val active by LocationStore.activePlace.collectAsState()
    val savedRaw by LocationStore.savedPlaces.collectAsState()
    val saved = savedRaw.ifEmpty { listOf(active) } // show the active place even before anything is saved
    var showSearch by remember { mutableStateOf(false) }
    // after a manual pick, offer the region's official method (skippable) — only when it differs from the current one
    var methodPrompt by remember { mutableStateOf<Pair<Place, CalculationMethod>?>(null) }
    fun selectPlace(place: Place) {
        LocationStore.setActive(place)
        val suggested = CalculationMethod.forCountry(place.countryCode)
        methodPrompt = if (suggested != MiqatCalculationStore.method.value) place to suggested else null
    }

    // load the 49k-row catalog once, off the main thread — so opening search is instant
    var all by remember { mutableStateOf<List<Place>>(emptyList()) }
    LaunchedEffect(Unit) { all = withContext(Dispatchers.Default) { Place.fromCatalog(Res.readBytes("files/cities.txt")) } }

    // GPS: request permission → get a fix → snap to the nearest catalog city (offline) → save it.
    val perms = rememberPermissionService()
    val geo = rememberGeoLocator()
    val notice = LocalNotice.current
    val scope = rememberCoroutineScope()
    var showDeniedSheet by remember { mutableStateOf(false) }
    var locating by remember { mutableStateOf(false) } // GPS in flight — drives the tile spinner
    fun useCurrentLocation() {
        if (locating) return
        locating = true
        scope.launch {
            try {
                when (perms.request(AppPermission.Location)) {
                    PermissionStatus.Granted -> {
                        // allowed, but the device switch is off — there is no location to read
                        if (!geo.servicesEnabled()) {
                            notice.show(
                                title = getString(Res.string.location_is_off),
                                message = getString(Res.string.location_is_off_sub),
                                icon = Lucide.LocateOff,
                                variant = AppTileVariant.Warning,
                                actionLabel = getString(Res.string.open_settings),
                                onAction = { geo.requestLocationOn() },
                            )
                            return@launch
                        }
                        val fix = geo.current()
                        val place = fix?.let { all.nearestTo(it.latitude, it.longitude) }
                        if (place != null) selectPlace(place) // else: no fix / catalog still loading — keep current
                    }

                    else -> showDeniedSheet = true // denied/dismissed → explain + offer Settings
                }
            } finally {
                locating = false
            }
        }
    }

    // full-screen search takes over when open (LazyColumn handles hundreds of rows efficiently)
    if (showSearch) {
        CitySearchScreen(all = all, onPick = { selectPlace(it); showSearch = false }, onClose = { showSearch = false })
        return
    }

    if (showDeniedSheet) {
        LocationDeniedSheet(
            onOpenSettings = { showDeniedSheet = false; perms.openAppSettings() },
            onDismiss = { showDeniedSheet = false },
        )
    }

    methodPrompt?.let { (place, suggested) ->
        MethodSwitchSheet(
            place = place,
            method = suggested,
            onConfirm = { MiqatCalculationStore.setMethod(suggested); methodPrompt = null },
            onDismiss = { methodPrompt = null },
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.location)) },
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
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp).verticalScroll(rememberScrollState())) {
            AppTileGroup(
                title = stringResource(Res.string.notif_needs_attention),
                variant = AppTileVariant.Warning,
                items = listOf(LocationServiceTile(), LocationPermissionTile()),
            )
            AppTileGroup(
                items = listOf(
                    AppTileItem(
                        title = stringResource(Res.string.use_current_location),
                        leadingIcon = Lucide.Navigation,
                        trailing = {
                            if (locating) CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = c.primary,
                                strokeWidth = 2.dp
                            )
                        },
                        onClick = { useCurrentLocation() },
                    ),
                    AppTileItem(title = stringResource(Res.string.search_city), leadingIcon = Lucide.Search, onClick = { showSearch = true }),
                ),
            )
            Spacer(Modifier.height(12.dp))
            AppTileGroup(
                title = stringResource(Res.string.saved),
                items = saved.map { place ->
                    val isActive = place.sameAs(active)
                    AppTileItem(
                        title = place.name,
                        subtitle = place.countryLabel,
                        leadingIcon = Lucide.MapPin,
                        selected = isActive,
                        trailing = {
                            if (isActive) Icon(Lucide.Check, null, tint = c.primary, modifier = Modifier.size(20.dp))
                            else Icon(
                                Lucide.X,
                                null,
                                tint = c.onSurfaceVariant,
                                modifier = Modifier.size(18.dp).clickable { LocationStore.remove(place) })
                        },
                        onClick = { selectPlace(place) },
                    )
                },
            )
            Spacer(Modifier.height(12.dp))
            AppTileGroup(
                title = stringResource(Res.string.suggested),
                items = MiqatDefaults.places.map { place ->
                    AppTileItem(
                        title = place.name,
                        subtitle = place.countryLabel,
                        leadingIcon = Lucide.MapPin,
                        selected = place.sameAs(active),
                        onClick = { selectPlace(place) },
                    )
                },
            )
        }
    }
}

/**
 * Full-screen city search over the whole offline catalog. Pinned search bar on top, a lazy list of up to 200
 * matches below (prefix matches ranked first). All state is local — it's gone when the screen closes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CitySearchScreen(all: List<Place>, onPick: (Place) -> Unit, onClose: () -> Unit) {
    val c = AppTheme.colors
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Place>>(emptyList()) }
    LaunchedEffect(query, all) {
        val q = query.trim()
        results = if (q.isBlank()) emptyList() else withContext(Dispatchers.Default) {
            all.asSequence()
                .filter { it.ascii.contains(q, ignoreCase = true) || it.name.contains(q, ignoreCase = true) }
                .sortedWith(compareByDescending<Place> { it.ascii.startsWith(q, ignoreCase = true) }.thenBy { it.ascii })
                .take(200).toList()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.search_city)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            tr(Lucide.ChevronLeft, Lucide.ChevronRight),
                            stringResource(Res.string.back)
                        )
                    }
                },
            )
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp)) {
            AppTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = stringResource(Res.string.search_city),
                trailing = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                Lucide.X,
                                stringResource(Res.string.clear_search),
                                tint = c.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else null,
            )
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().weight(1f)) {
                when {
                    all.isEmpty() -> CircularProgressIndicator(color = c.primary, modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp))
                    query.isBlank() -> StateView(
                        title = stringResource(Res.string.search_city_hint_title),
                        message = stringResource(Res.string.search_city_hint_message),
                        icon = { Icon(Lucide.Search, null, tint = c.primary, modifier = Modifier.size(40.dp)) },
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp),
                    )

                    results.isEmpty() -> StateView(
                        title = stringResource(Res.string.no_cities_found),
                        message = stringResource(Res.string.try_a_different_search),
                        icon = { Icon(Lucide.MapPin, null, tint = c.onSurfaceVariant, modifier = Modifier.size(40.dp)) },
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp),
                    )

                    else -> LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        itemsIndexed(results, key = { _, place -> place.name + place.countryCode + place.latitude }) { i, place ->
                            AppTile(
                                title = place.name,
                                subtitle = place.countryLabel,
                                leadingIcon = Lucide.MapPin,
                                position = when {
                                    results.size == 1 -> TilePosition.Single
                                    i == 0 -> TilePosition.First
                                    i == results.lastIndex -> TilePosition.Last
                                    else -> TilePosition.Middle
                                },
                                onClick = { onPick(place) },
                            )
                        }
                    }
                }
            }
        }
    }
}
