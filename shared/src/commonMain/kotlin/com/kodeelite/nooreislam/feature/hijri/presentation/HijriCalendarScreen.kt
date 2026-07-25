package com.kodeelite.nooreislam.feature.hijri.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppCard
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.MiniStepper
import com.kodeelite.nooreislam.core.constants.PrefConst
import com.kodeelite.nooreislam.core.datetime.HijriMonth
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.locale.tr
import com.kodeelite.nooreislam.core.prefs.PrefsService
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.adjust_hijri_hint
import com.kodeelite.nooreislam.resources.back
import com.kodeelite.nooreislam.resources.date_adjustment
import com.kodeelite.nooreislam.resources.days
import com.kodeelite.nooreislam.resources.hijri_calendar
import com.kodeelite.nooreislam.resources.hijri_era
import org.jetbrains.compose.resources.stringResource

/**
 * Hijri calendar — shows today's Islamic (Umm al-Qura) date from the platform calendar, with a ±day
 * adjustment for when the local moon-sighting differs from the calculation. The hero updates live as you
 * adjust; the offset persists ([PrefConst.HIJRI_OFFSET]) and affects the Hijri date shown across the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HijriCalendarScreen(onBack: () -> Unit = {}) {
    val c = AppTheme.colors
    var offset by remember { mutableStateOf(PrefsService.getInt(PrefConst.HIJRI_OFFSET, 0)) }
    val hijri = Now.hijri(offset)
    val gregorian = Now.date()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.hijri_calendar)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            tr(Lucide.ChevronLeft, Lucide.ChevronRight),
                            stringResource(Res.string.back)
                        )
                    }
                },
            )
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp)) {
            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${hijri.day} ${HijriMonth.of(hijri.month).label()}", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${hijri.year} ${stringResource(Res.string.hijri_era)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = c.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("${gregorian.dayOfMonth}·${gregorian.monthNumber}·${gregorian.year}", fontSize = 12.sp, color = c.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(16.dp))
            AppTileGroup(
                items = listOf(
                    AppTileItem(
                        title = stringResource(Res.string.date_adjustment),
                        subtitle = stringResource(Res.string.adjust_hijri_hint),
                        trailing = {
                            MiniStepper(
                                offset,
                                stringResource(Res.string.days),
                                { offset = it; PrefsService.putInt(PrefConst.HIJRI_OFFSET, it) },
                                min = -2,
                                max = 2
                            )
                        },
                    ),
                ),
            )
        }
    }
}
