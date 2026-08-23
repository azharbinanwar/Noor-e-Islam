package com.kodeelite.nooreislam.feature.sandbox.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppButtonSize
import com.kodeelite.nooreislam.core.components.AppButtonVariant
import com.kodeelite.nooreislam.core.components.AppChip
import com.kodeelite.nooreislam.core.components.AppTextField
import com.kodeelite.nooreislam.core.location.GeoAddress
import com.kodeelite.nooreislam.core.location.GeoResult
import com.kodeelite.nooreislam.core.location.rememberGeoCoder
import com.kodeelite.nooreislam.core.location.rememberGeoLocator
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

// half of these are Pakistani, which is where the returned names get interesting
private val PRESETS = listOf(
    "Karachi" to (24.8607 to 67.0011),
    "Lahore" to (31.5204 to 74.3587),
    "Islamabad" to (33.6844 to 73.0479),
    "Peshawar" to (34.0151 to 71.5249),
    "Quetta" to (30.1798 to 66.9750),
    "Gilgit" to (35.9208 to 74.3144),
    "Gwadar" to (25.1264 to 62.3225),
    "Makkah" to (21.4225 to 39.8262),
    "Dubai" to (25.2048 to 55.2708),
    "London" to (51.5074 to -0.1278),
    "Jakarta" to (-6.2088 to 106.8456),
    "Open sea" to (0.0 to -140.0),
)

private const val PK_PRESETS = 7
private val WAIT = 15.seconds

/** Geocoder rig: throw coordinates at the OS and read back every field it fills. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeoShowcase() {
    val c = AppTheme.colors
    val coder = rememberGeoCoder()
    val locator = rememberGeoLocator()
    val scope = rememberCoroutineScope()

    var lat by remember { mutableStateOf("24.8607") }
    var lng by remember { mutableStateOf("67.0011") }
    var query by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("geocoder ${if (coder.available()) "ready" else "unavailable"}") }
    var found by remember { mutableStateOf<List<GeoAddress>>(emptyList()) }

    suspend fun resolve() {
        val a = lat.toDoubleOrNull()
        val o = lng.toDoubleOrNull()
        if (a == null || o == null) {
            status = "lat/lng is not a number"
            return
        }
        when (val r = coder.reverse(a, o)) {
            is GeoResult.Ok -> {
                found = listOf(r.address)
                status = "ok"
            }

            is GeoResult.Fail -> {
                found = emptyList()
                status = "${r.error}: ${r.message.orEmpty()}"
            }
        }
    }

    // nothing here is allowed to hang: a refused permission, a dead service or an unanswered
    // callback has to read as a failure, the same as a thrown error would
    fun work(note: String, block: suspend () -> Unit) {
        busy = true
        status = note
        found = emptyList()
        scope.launch {
            runCatching { withTimeoutOrNull(WAIT) { block() } }
                .onSuccess { if (it == null) status = "timed out after ${WAIT.inWholeSeconds}s" }
                .onFailure { status = "failed: ${it.message ?: it::class.simpleName}" }
            busy = false
        }
    }

    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Geocoder", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = c.onSurface)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppTextField(lat, { lat = it }, Modifier.weight(1f), placeholder = "Latitude")
            AppTextField(lng, { lng = it }, Modifier.weight(1f), placeholder = "Longitude")
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PRESETS.forEach { (name, at) ->
                AppChip(
                    label = name,
                    selected = lat == at.first.toString() && lng == at.second.toString(),
                    onClick = { lat = at.first.toString(); lng = at.second.toString() },
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppButton(
                text = "Random",
                size = AppButtonSize.Small,
                variant = AppButtonVariant.Outline,
                modifier = Modifier.weight(1f),
                // ocean included, so the empty answer gets a run too
                onClick = {
                    lat = Random.nextDouble(-60.0, 70.0).round5()
                    lng = Random.nextDouble(-180.0, 180.0).round5()
                },
            )
            AppButton(
                text = "Random PK",
                size = AppButtonSize.Small,
                variant = AppButtonVariant.Outline,
                modifier = Modifier.weight(1f),
                // jittered off the city pin, so it lands on a neighbourhood
                onClick = {
                    val (_, at) = PRESETS.take(PK_PRESETS).random()
                    lat = (at.first + Random.nextDouble(-0.1, 0.1)).round5()
                    lng = (at.second + Random.nextDouble(-0.1, 0.1)).round5()
                },
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppButton(
                text = "Resolve",
                size = AppButtonSize.Small,
                modifier = Modifier.weight(1f),
                onClick = { work("resolving") { resolve() } },
            )
            AppButton(
                text = "My GPS",
                size = AppButtonSize.Small,
                variant = AppButtonVariant.Outline,
                modifier = Modifier.weight(1f),
                onClick = {
                    work("reading gps") {
                        val fix = locator.current()
                        if (fix == null) status = "no fix: permission off, or location off"
                        else {
                            lat = fix.latitude.round5()
                            lng = fix.longitude.round5()
                            resolve()
                        }
                    }
                },
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            AppTextField(query, { query = it }, Modifier.weight(1f), placeholder = "City name")
            AppButton(
                text = "Find",
                size = AppButtonSize.Small,
                variant = AppButtonVariant.Outline,
                onClick = {
                    work("searching") {
                        found = coder.search(query)
                        status = "${found.size} result(s)"
                    }
                },
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (busy) {
                CircularProgressIndicator(Modifier.size(13.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(status, fontSize = 12.sp, color = c.onSurfaceVariant)
        }

        found.forEach { address ->
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(c.cardColor).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                address.fields().forEach { (key, value) -> FieldRow(key, value) }
            }
        }
    }
}

// blanks are kept: which fields come back empty on which platform is half of what this is for
private fun GeoAddress.fields(): List<Pair<String, String>> = listOf(
    "short" to short.orEmpty(),
    "name" to name.orEmpty(),
    "street" to street.orEmpty(),
    "subLocality" to subLocality.orEmpty(),
    "locality" to locality.orEmpty(),
    "subAdminArea" to subAdminArea.orEmpty(),
    "adminArea" to adminArea.orEmpty(),
    "postalCode" to postalCode.orEmpty(),
    "country" to country.orEmpty(),
    "countryCode" to countryCode.orEmpty(),
    "timeZone" to timeZone.orEmpty(),
    "latitude" to latitude?.toString().orEmpty(),
    "longitude" to longitude?.toString().orEmpty(),
) + lines.mapIndexed { i, line -> "line[$i]" to line }

@Composable
private fun FieldRow(key: String, value: String) {
    val c = AppTheme.colors
    Row(Modifier.fillMaxWidth()) {
        Text(key, fontSize = 12.sp, color = c.onSurfaceVariant, modifier = Modifier.width(104.dp))
        Text(
            value.ifBlank { "—" },
            fontSize = 12.sp,
            color = if (value.isBlank()) c.onSurfaceVariant.copy(alpha = 0.45f) else c.onSurface,
            fontWeight = if (value.isBlank()) FontWeight.Normal else FontWeight.Medium,
        )
    }
}

private fun Double.round5(): String = ((this * 100000).toLong() / 100000.0).toString()
