package com.kodeelite.nooreislam.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.feature.miqat.store.MiqatTimesStore
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.math.roundToInt

/**
 * Where the loop scene gets proven before it reaches Home, and stays afterwards — retuning the header
 * later means coming back here, not rebuilding this. Everything above the controls is the shipped
 * component, so what passes here is literally what Home draws.
 */
@Composable
fun SkyLabScreen() {
    val clock by Now.now.collectAsState()
    var date by remember { mutableStateOf(Now.date()) }
    var scrub by remember { mutableStateOf<Float?>(null) }
    var showPoints by remember { mutableStateOf(true) }

    val times = remember(date) { MiqatTimesStore.timesFor(date) }
    val minutes = scrub?.roundToInt() ?: (clock.time.hour * 60 + clock.time.minute)
    val time = LocalTime(minutes / 60 % 24, minutes % 60)
    val sunPoint = remember(time, times) { sunPointAt(time, times) }
    val c = AppTheme.colors

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            LoopSceneHeader(now = time, date = date, times = times, showPoints = showPoints)

            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Sky lab · header calibration",
                    color = c.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "The scene above is the real LoopSceneHeader. Only the controls below belong to this " +
                        "screen. Prayer points live in LOOP_ANCHORS, the ridge crossings in RIDGE_EAST / RIDGE_WEST.",
                    color = c.onSurfaceVariant,
                    fontSize = 11.sp,
                )
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("${time.hour.pad()}:${time.minute.pad()}", color = c.onSurface)
                    Text(
                        "sun ${sunPoint.wrapped()}  splash ${splashAt(sunPoint).fmt()}   moon ${(sunPoint + MOON_LEAD).wrapped()}",
                        color = c.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                }

                Slider(minutes.toFloat(), { scrub = it }, valueRange = 0f..1439f)

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    TextButton({ date = date.minus(1, DateTimeUnit.MONTH) }) { Text("month -") }
                    TextButton({ date = date.minus(1, DateTimeUnit.DAY) }) { Text("day -") }
                    Text("$date", color = c.onSurface)
                    TextButton({ date = date.plus(1, DateTimeUnit.DAY) }) { Text("day +") }
                    TextButton({ date = date.plus(1, DateTimeUnit.MONTH) }) { Text("month +") }
                }

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("ruler", color = c.onSurfaceVariant)
                    Switch(showPoints, { showPoints = it })
                    TextButton({ scrub = null; date = Now.date() }) { Text("live") }
                }

                Text(
                    LOOP_ANCHORS.joinToString("   ") { (m, p) -> "${m.name.take(3)} ${p.fmt()}" },
                    color = c.onSurfaceVariant,
                    fontSize = 11.sp,
                )
                Text(
                    times.filter { t -> LOOP_ANCHORS.any { it.first == t.miqat } }
                        .joinToString("   ") { "${it.miqat.name.take(3)} ${it.at.time.hour.pad()}:${it.at.time.minute.pad()}" },
                    color = c.onSurfaceVariant,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

private fun Int.pad() = toString().padStart(2, '0')
private fun Float.fmt() = ((this * 10).roundToInt() / 10f).toString()
private fun Float.wrapped() = mod(LOOP_POINTS).fmt()
