package com.kodeelite.nooreislam.feature.qibla.store

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kodeelite.nooreislam.core.location.Coordinates
import com.kodeelite.nooreislam.core.location.GeoCoder
import com.kodeelite.nooreislam.core.location.GeoLocator
import com.kodeelite.nooreislam.core.location.LocationRepository
import com.kodeelite.nooreislam.core.network.dataOrNull
import com.kodeelite.nooreislam.core.permissions.AppPermission
import com.kodeelite.nooreislam.core.permissions.PermissionService
import com.kodeelite.nooreislam.core.permissions.PermissionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/** Why the compass has no position of its own. Each one has a different way out. */
enum class QiblaGate { Ask, ServiceOff, Blocked }

/**
 * Where the compass measures from: the location switch, the permission, the fix and its place name.
 * The saved city is fine for prayer times but points the qibla wrong once you travel, so the
 * compass keeps its own position rather than borrowing that one.
 */
class QiblaOriginStore(
    private val perms: PermissionService,
    private val geo: GeoLocator,
    private val locations: LocationRepository,
    private val geoCoder: GeoCoder?,
    private val scope: CoroutineScope,
) {
    val gateState = mutableStateOf<QiblaGate?>(null)
    val fixState = mutableStateOf<Coordinates?>(null)
    val fixNameState = mutableStateOf<String?>(null)
    val sheetOpenState = mutableStateOf(false)

    private var gate by gateState
    private var fix by fixState
    private var fixName by fixNameState
    private var sheetOpen by sheetOpenState

    // status() reads back as "never asked" after a permanent refusal, so remember it here
    private var blocked = false

    // the sheet leads on arrival; after that the tile is the way back in
    private var sheetShown = false
    private var usingPlace = false
    private var watchJob: Job? = null

    /** Access is fine, the fix is just still coming. Not a fault, so not a warning. */
    val locating: Boolean get() = gate == null && fix == null

    /** Both are granted through system UI that never pauses the app, so a resume check never fires. */
    fun start() {
        if (watchJob?.isActive == true) return
        watchJob = scope.launch {
            while (isActive) {
                refresh()
                delay(POLL)
            }
        }
    }

    fun stop() {
        watchJob?.cancel()
        watchJob = null
    }

    /** The action that actually unblocks the current gate. */
    fun resolve(onOpenSystemUi: () -> Unit = {}) {
        val current = gate ?: return
        sheetOpen = false
        when (current) {
            QiblaGate.Ask -> scope.launch {
                if (perms.request(AppPermission.Location) == PermissionStatus.DeniedPermanently) {
                    blocked = true
                    gate = QiblaGate.Blocked
                    sheetOpen = true
                } else refresh()
            }

            QiblaGate.ServiceOff -> { geo.requestLocationOn(); onOpenSystemUi() }
            QiblaGate.Blocked -> { perms.openAppSettings(); onOpenSystemUi() }
        }
    }

    /** Aim from the saved city instead. Offered, never assumed. */
    fun useSavedPlace() {
        usingPlace = true
        sheetOpen = false
    }

    fun openSheet() { sheetOpen = true }

    fun dismissSheet() { sheetOpen = false }

    private suspend fun refresh() {
        val status = perms.status(AppPermission.Location)
        if (status == PermissionStatus.Granted) blocked = false
        val next = when {
            // switch first: access to an app that still cannot see a location fixes nothing
            !geo.servicesEnabled() -> QiblaGate.ServiceOff
            blocked || status == PermissionStatus.DeniedPermanently -> QiblaGate.Blocked
            status != PermissionStatus.Granted -> QiblaGate.Ask
            else -> null
        }
        if (next != null) fix = null
        if (next == null && gate != null) { usingPlace = false; sheetOpen = false }
        if (next != null && gate == null && !usingPlace && !sheetShown) {
            sheetOpen = true
            sheetShown = true
        }
        gate = next
        // a fix can outlast one turn of the loop, so it is fetched here rather than restarted each turn
        if (next == null && fix == null) {
            val at = geo.current() ?: return
            fix = at
            fixName = locations.resolve(at, geoCoder).dataOrNull()?.name
        }
    }

    private companion object {
        val POLL = 1200.milliseconds
    }
}
