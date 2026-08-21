package com.kodeelite.nooreislam.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.Moon
import com.composables.icons.lucide.Sunrise
import com.composables.icons.lucide.Sunset
import com.kodeelite.nooreislam.core.components.SehriInfoSheet
import com.kodeelite.nooreislam.core.datetime.HijriMonth
import com.kodeelite.nooreislam.core.datetime.format
import com.kodeelite.nooreislam.core.datetime.labelRes
import com.kodeelite.nooreislam.core.datetime.toHijri
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.store.LocationStore
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.miqat.domain.MiqatTime
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.about_sehri
import com.kodeelite.nooreislam.resources.countdown_hms
import com.kodeelite.nooreislam.resources.countdown_ms
import com.kodeelite.nooreislam.resources.hijri_era
import com.kodeelite.nooreislam.resources.iftar
import com.kodeelite.nooreislam.resources.menu
import com.kodeelite.nooreislam.resources.now_caps
import com.kodeelite.nooreislam.resources.prayer_and_time_summary
import com.kodeelite.nooreislam.resources.prayer_countdown_summary
import com.kodeelite.nooreislam.resources.sehri
import com.kodeelite.nooreislam.resources.time_with_period
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource

/**
 * The whole header on the loop scene. Same content as the live one, but the clock and the day are
 * passed in so the lab can scrub a date without touching the stores.
 */
@Composable
fun LoopSceneHeader(
    now: LocalTime,
    date: LocalDate,
    times: List<MiqatTime>,
    modifier: Modifier = Modifier,
    fraction: Float = 0f,
    expandedHeight: Dp = 380.dp,
    collapsedHeight: Dp = 116.dp,
    showPoints: Boolean = false,
    onMenuClick: () -> Unit = {},
) {
    val place by LocationStore.activePlace.collectAsState()
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val sehriRef by SettingsStore.sehriReference.collectAsState()

    // Shuruq counts here: it is what closes Fajr, and leaving it out named Dhuhr instead
    val prayerTimes = times.filter { it.miqat in Miqat.DAILY }
    val nextMt = prayerTimes.firstOrNull { it.at.time > now } ?: prayerTimes.firstOrNull()
    val prayer = nextMt?.miqat ?: Miqat.Fajr
    val nextTime = nextMt?.at?.time?.format(timeFormat.pattern) ?: ""
    val countdown = nextMt?.let {
        val secs = ((it.at.time.toSecondOfDay() - now.toSecondOfDay()) + 24 * 3600) % (24 * 3600)
        val h = secs / 3600
        val m = (secs % 3600) / 60
        val s = secs % 60
        if (h > 0) stringResource(Res.string.countdown_hms, h, m, s)
        else stringResource(Res.string.countdown_ms, m, s)
    } ?: ""

    val hijri = remember(date) { toHijri(date) }
    val sky = remember(now, times, hijri.day) { loopSky(now, times, hijri.day) }
    val period = sky.period
    val dateLabel = "${stringResource(date.dayOfWeek.labelRes)}, ${hijri.day} ${
        HijriMonth.of(hijri.month).label()
    } ${hijri.year} ${stringResource(Res.string.hijri_era)}"

    val ramadan = hijri.month == 9
    val sehri = if (ramadan) times.firstOrNull { it.miqat == sehriRef }?.at?.time?.format(timeFormat.pattern) else null
    val iftar = if (ramadan) times.firstOrNull { it.miqat == Miqat.Maghrib }?.at?.time?.format(timeFormat.pattern) else null
    val sunrise = times.firstOrNull { it.miqat == Miqat.Sunrise }?.at?.time?.format(timeFormat.pattern)
    val sunset = times.firstOrNull { it.miqat == Miqat.Sunset }?.at?.time?.format(timeFormat.pattern)
    var showSehriInfo by remember { mutableStateOf(false) }

    val headerHeight = lerp(expandedHeight, collapsedHeight, fraction)
    val headerCorner = lerp(28.dp, 0.dp, fraction)
    val expandedAlpha = (1f - fraction * 1.7f).coerceIn(0f, 1f)
    val slimAlpha = ((fraction - 0.35f) / 0.65f).coerceIn(0f, 1f)

    Box(
        modifier.fillMaxWidth().height(headerHeight)
            .clip(RoundedCornerShape(bottomStart = headerCorner, bottomEnd = headerCorner)),
    ) {
        LoopScene(sky, Modifier.fillMaxSize(), showPoints)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)))))

        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onMenuClick) { Icon(Lucide.Menu, stringResource(Res.string.menu), tint = Color.White) }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.alpha(1f - slimAlpha)) {
                    Icon(Lucide.MapPin, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(place.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.alpha(slimAlpha)) {
                    Icon(prayer.icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(Res.string.prayer_and_time_summary, prayer.label(), nextTime),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(Modifier.size(48.dp))
        }

        if (expandedAlpha > 0f) {
            Column(Modifier.align(Alignment.BottomStart).padding(20.dp).alpha(expandedAlpha)) {
                Text(
                    stringResource(Res.string.now_caps),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.6.sp,
                )
                Spacer(Modifier.height(3.dp))
                Text(stringResource(period.labelRes), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(Res.string.prayer_countdown_summary, prayer.label(), nextTime, countdown),
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 13.sp,
                )
                if (sehri != null || iftar != null) {
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (sehri != null) LoopChip(Lucide.Moon, stringResource(Res.string.sehri), sehri)
                        if (sehri != null && iftar != null) Spacer(Modifier.width(8.dp))
                        if (iftar != null) LoopChip(Lucide.Sunset, stringResource(Res.string.iftar), iftar)
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Lucide.Info, stringResource(Res.string.about_sehri), tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp).clip(CircleShape).clickable { showSehriInfo = true },
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        dateLabel, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
                    )
                    if (sunrise != null || sunset != null) {
                        Spacer(Modifier.width(12.dp))
                        if (sunrise != null) LoopSunTime(Lucide.Sunrise, sunrise)
                        if (sunrise != null && sunset != null) Spacer(Modifier.width(10.dp))
                        if (sunset != null) LoopSunTime(Lucide.Sunset, sunset)
                    }
                }
            }
        }
    }

    if (showSehriInfo) SehriInfoSheet(onDismiss = { showSehriInfo = false })
}

@Composable
private fun LoopSunTime(icon: ImageVector, time: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text(time, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
    }
}

@Composable
private fun LoopChip(icon: ImageVector, label: String, time: String) {
    Row(
        Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.16f)).padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text(stringResource(Res.string.time_with_period, label, time), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
