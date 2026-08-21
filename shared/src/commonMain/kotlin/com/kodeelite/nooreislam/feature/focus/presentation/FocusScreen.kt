package com.kodeelite.nooreislam.feature.focus.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppSwitch
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.locale.tr
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.core.store.PrayerFocusStore
import com.kodeelite.nooreislam.feature.focus.presentation.components.FocusNeedsAttention
import com.kodeelite.nooreislam.feature.focus.presentation.components.FocusPrayers
import com.kodeelite.nooreislam.feature.focus.presentation.components.FocusTestTiles
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.all_focus
import com.kodeelite.nooreislam.resources.back
import com.kodeelite.nooreislam.resources.master_switch_for_every_prayer_window
import com.kodeelite.nooreislam.resources.prayer_focus
import com.kodeelite.nooreislam.resources.silence_phone_around_each_prayer_set_separately
import org.jetbrains.compose.resources.stringResource

// Prayer Focus (Android only): mute the phone around each prayer. Just layout; the pieces own their state.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen() {
    val nav = LocalAppNavigator.current
    val c = AppTheme.colors
    var taps by remember { mutableStateOf(0) } // 7 taps on the blurb reveals the test tiles; resets on re-entry
    val allFocus by PrayerFocusStore.allFocus.collectAsState()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.prayer_focus)) },
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
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text(
                stringResource(Res.string.silence_phone_around_each_prayer_set_separately),
                fontSize = 13.sp, color = c.onSurfaceVariant,
                // hidden dev gesture: no ripple, so it doesn't look tappable
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { taps++ },
            )
            Spacer(Modifier.height(12.dp))
            FocusNeedsAttention()
            AppTileGroup(
                items = listOf(
                    AppTileItem(
                        title = stringResource(Res.string.all_focus),
                        subtitle = stringResource(Res.string.master_switch_for_every_prayer_window),
                        trailing = { AppSwitch(allFocus, PrayerFocusStore::setAllFocus) },
                        onClick = { PrayerFocusStore.setAllFocus(!allFocus) },
                    )
                )
            )
            if (allFocus) {
                if (taps >= 7) FocusTestTiles()
                FocusPrayers()
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
