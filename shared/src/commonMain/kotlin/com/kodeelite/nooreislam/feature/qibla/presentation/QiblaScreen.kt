package com.kodeelite.nooreislam.feature.qibla.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Compass
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.SwatchBook
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.LocalDrawerState
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.core.store.LocationStore
import com.kodeelite.nooreislam.feature.qibla.domain.distanceToMakkahKm
import com.kodeelite.nooreislam.feature.qibla.domain.qiblaBearing
import com.kodeelite.nooreislam.feature.qibla.presentation.components.CompassCalibration
import com.kodeelite.nooreislam.feature.qibla.presentation.components.QiblaDialFor
import com.kodeelite.nooreislam.feature.qibla.presentation.components.QiblaStyleSheet
import com.kodeelite.nooreislam.feature.qibla.store.QiblaStyleStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.calibrate_compass
import com.kodeelite.nooreislam.resources.compass_style
import com.kodeelite.nooreislam.resources.compass_unavailable
import com.kodeelite.nooreislam.resources.device_has_no_compass_sensor
import com.kodeelite.nooreislam.resources.direction
import com.kodeelite.nooreislam.resources.distance_to_makkah
import com.kodeelite.nooreislam.resources.menu
import com.kodeelite.nooreislam.resources.qibla
import com.kodeelite.nooreislam.resources.turn_left_to_face_qibla
import com.kodeelite.nooreislam.resources.turn_right_to_face_qibla
import com.kodeelite.nooreislam.resources.you_are_facing_qibla
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import com.kodeelite.nooreislam.resources.qibla_aiming_from_you
import com.kodeelite.nooreislam.resources.qibla_aiming_from_place
import com.kodeelite.nooreislam.feature.qibla.presentation.components.QiblaLocationSheet
import com.kodeelite.nooreislam.core.permissions.rememberPermissionService
import com.kodeelite.nooreislam.core.location.rememberGeoLocator
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.LocateFixed
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.clickable
import com.kodeelite.nooreislam.core.components.AppTile
import com.composables.icons.lucide.ChevronRight
import com.kodeelite.nooreislam.core.components.AppTileVariant
import com.composables.icons.lucide.MapPinOff
import com.kodeelite.nooreislam.resources.qibla_only_right_here
import com.kodeelite.nooreislam.resources.qibla_finding_you
import com.kodeelite.nooreislam.core.location.LocationRepository
import com.kodeelite.nooreislam.core.location.rememberGeoCoder
import org.koin.compose.koinInject
import androidx.compose.material3.CircularProgressIndicator
import com.kodeelite.nooreislam.core.constants.defaults.QiblaDefaults
import androidx.compose.runtime.DisposableEffect
import com.kodeelite.nooreislam.feature.qibla.store.QiblaOriginStore
import com.kodeelite.nooreislam.feature.qibla.store.QiblaGate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen() {
    val drawerState = LocalDrawerState.current
    val scope = rememberCoroutineScope()

    val place by LocationStore.activePlace.collectAsState()

    val perms = rememberPermissionService()
    val geo = rememberGeoLocator()
    val geoCoder = rememberGeoCoder()
    val locations = koinInject<LocationRepository>()
    val origins = remember { QiblaOriginStore(perms, geo, locations, geoCoder, scope) }
    DisposableEffect(Unit) {
        origins.start()
        onDispose { origins.stop() }
    }

    val gate by origins.gateState
    val fix by origins.fixState
    val fixName by origins.fixNameState
    val sheetOpen by origins.sheetOpenState

    val origin = fix
    val lat = origin?.latitude ?: place.latitude
    val lng = origin?.longitude ?: place.longitude
    val qiblaDeg = qiblaBearing(lat, lng).toFloat()
    val distance = distanceToMakkahKm(lat, lng)
    // amber only when access is missing; a fix still on its way is not a fault
    val unproven = gate != null
    val locating = origins.locating

    val heading = rememberHeading()
    val qiblaStyle by QiblaStyleStore.style.collectAsState()

    var calibrationDismissed by remember { mutableStateOf(false) }
    var showStyleSheet by remember { mutableStateOf(false) }
    val needsCalibration = !heading.accurate

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.qibla)) },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Lucide.Menu, stringResource(Res.string.menu)) }
                },
                actions = {
                    IconButton(onClick = { showStyleSheet = true }) { Icon(Lucide.SwatchBook, stringResource(Res.string.compass_style)) }
                },
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
            if (!heading.available) {
                StateView(
                    title = stringResource(Res.string.compass_unavailable),
                    message = stringResource(Res.string.device_has_no_compass_sensor),
                    icon = { Icon(Lucide.Compass, null, tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(56.dp)) },
                )
                return@Box
            }

            // angle from current facing to qibla, normalized to -180..180
            val delta = (((qiblaDeg - heading.degrees) + 540f) % 360f) - 180f
            val aligned = abs(delta) <= QiblaDefaults.ALIGN_TOLERANCE_DEG

            Column(
                Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                Box(Modifier.alpha(if (origin == null) 0.35f else 1f)) {
                    QiblaDialFor(qiblaStyle, heading.degrees, qiblaDeg, aligned)
                }

                // where this direction is measured from, and the way back into the flow
                AppTile(
                    title = when {
                        unproven -> stringResource(Res.string.qibla_aiming_from_place, place.name)
                        locating -> stringResource(Res.string.qibla_finding_you)
                        else -> fixName?.let { stringResource(Res.string.qibla_aiming_from_place, it) }
                            ?: stringResource(Res.string.qibla_aiming_from_you)
                    },
                    // amber only while access is missing; a fix still arriving is not a fault
                    variant = if (unproven) AppTileVariant.Warning else AppTileVariant.Normal,
                    leadingIcon = if (unproven) Lucide.MapPinOff else Lucide.LocateFixed,
                    subtitle = if (unproven) stringResource(Res.string.qibla_only_right_here) else null,
                    // a spinner while the fix is on its way, the way the home header's pin spins:
                    // movement says "working" to someone who never reads the row
                    trailing = when {
                        unproven -> {
                            {
                                Icon(
                                    Lucide.ChevronRight,
                                    null,
                                    tint = AppTheme.colors.warning,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                        locating -> {
                            {
                                CircularProgressIndicator(
                                    Modifier.size(16.dp),
                                    color = AppTheme.colors.primary,
                                    strokeWidth = 2.dp,
                                )
                            }
                        }
                        else -> null
                    },
                    onClick = if (unproven) ({ origins.openSheet() }) else null,
                )

                // the reading — the hero, degree and instruction on one line
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${qiblaDeg.roundToInt()}°",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.onSurface,
                    )
                    val hint = when {
                        aligned -> stringResource(Res.string.you_are_facing_qibla)
                        delta > 0 -> stringResource(Res.string.turn_right_to_face_qibla)
                        else -> stringResource(Res.string.turn_left_to_face_qibla)
                    }
                    Text(
                        hint,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (aligned) AppTheme.colors.success else AppTheme.colors.primary,
                    )
                }

                // supporting info in the app's card style. ponytail: labels inline; move to resources with the rest.
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(AppTheme.colors.cardColor).height(IntrinsicSize.Min),
                ) {
                    InfoCell(stringResource(Res.string.direction), "${qiblaDeg.roundToInt()}° ${cardinal(qiblaDeg)}", Modifier.weight(1f))
                    VerticalDivider(color = AppTheme.colors.onSurfaceVariant.copy(alpha = 0.15f))
                    InfoCell(stringResource(Res.string.distance_to_makkah), "${distance.roundToInt()} km", Modifier.weight(1f))
                }
            }

            if (needsCalibration && !calibrationDismissed) {
                AppBottomSheet(
                    onDismiss = { calibrationDismissed = true },
                    title = stringResource(Res.string.calibrate_compass),
                ) {
                    CompassCalibration(Modifier.padding(vertical = 8.dp))
                }
            }

            gate?.takeIf { sheetOpen }?.let { g ->
                QiblaLocationSheet(
                    gate = g,
                    placeName = place.name,
                    onPrimary = { origins.resolve() },
                    onUsePlace = { origins.useSavedPlace() },
                    onDismiss = { origins.dismissSheet() },
                )
            }

            if (showStyleSheet) {
                QiblaStyleSheet(
                    current = qiblaStyle,
                    headingDeg = heading.degrees,
                    qiblaDeg = qiblaDeg,
                    aligned = aligned,
                    onSelect = { QiblaStyleStore.setStyle(it) },
                    onDismiss = { showStyleSheet = false },
                )
            }
        }
    }
}

@Composable
private fun InfoCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AppTheme.colors.onSurface)
    }
}

/** 8-point cardinal for a bearing, e.g. 267° → "W". */
private fun cardinal(deg: Float): String {
    val dirs = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    return dirs[(((deg % 360f + 360f) % 360f) / 45f).roundToInt() % 8]
}
