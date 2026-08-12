package com.kodeelite.nooreislam.core.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppContentHost
import com.kodeelite.nooreislam.core.database.DatabaseRecovery
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.got_it
import com.kodeelite.nooreislam.resources.something_went_wrong_with_your_saved_data
import com.kodeelite.nooreislam.resources.sorry_app_started_fresh_set_up_again
import org.jetbrains.compose.resources.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.components.AppDrawer
import com.kodeelite.nooreislam.core.components.LocalDrawerState
import com.kodeelite.nooreislam.core.components.LocalOverlay
import com.kodeelite.nooreislam.core.components.OverlayState
import com.kodeelite.nooreislam.feature.azkar.presentation.AzkarScreen
import com.kodeelite.nooreislam.feature.focus.presentation.PrayerFocusScreen
import com.kodeelite.nooreislam.feature.home.presentation.HomeScreen
import com.kodeelite.nooreislam.feature.miqat.presentation.MiqatTimesScreen
import com.kodeelite.nooreislam.feature.notifications.presentation.NotificationsScreen
import com.kodeelite.nooreislam.feature.notifications.presentation.QuranNotificationsScreen
import com.kodeelite.nooreislam.feature.onboarding.presentation.OnboardingScreen
import com.kodeelite.nooreislam.feature.onboarding.presentation.QuranIntroScreen
import com.kodeelite.nooreislam.feature.qibla.presentation.QiblaScreen
import com.kodeelite.nooreislam.feature.quran.data.Ayah
import com.kodeelite.nooreislam.feature.quran.data.QuranRepository
import com.kodeelite.nooreislam.feature.quran.presentation.CollectionDetailsScreen
import com.kodeelite.nooreislam.feature.quran.presentation.QuranIndexScreen
import com.kodeelite.nooreislam.feature.quran.presentation.QuranReaderScreen
import com.kodeelite.nooreislam.feature.quran.presentation.QuranThemeHost
import com.kodeelite.nooreislam.feature.sandbox.presentation.SandboxScreen
import com.kodeelite.nooreislam.feature.settings.presentation.LocationScreen
import com.kodeelite.nooreislam.feature.settings.presentation.MiqatCalculationScreen
import com.kodeelite.nooreislam.feature.settings.presentation.SettingsScreen
import com.kodeelite.nooreislam.feature.settings.presentation.WidgetGalleryScreen
import com.kodeelite.nooreislam.feature.studio.presentation.StudioScreen
import com.kodeelite.nooreislam.feature.tasbih.presentation.TasbihHubScreen
import com.kodeelite.nooreislam.feature.tasbih.presentation.TasbihScreen
import com.kodeelite.nooreislam.feature.tracker.presentation.TrackerScreen
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val edition = koinInject<AppEdition>()
    // A tapped notification's destination, held until the graph exists — a cold-start tap lands
    // here long before this composes. The route itself is the only thing either platform sends.
    val pending by PendingNavigation.route.collectAsState()
    LaunchedEffect(pending) {
        pending?.let { navController.navigate(it); PendingNavigation.consume() }
    }
    AppNavigatorHost(navController) {
        // drawer + overlay hoisted once around the NavHost; screens open it via LocalDrawerState
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val overlay = remember { OverlayState() }
        CompositionLocalProvider(
            LocalDrawerState provides drawerState,
            LocalOverlay provides overlay,
        ) {
            val navHost = @Composable {
                NavHost(
                    navController = navController,
                    // the Quran-only app has nothing else to land on — its "home" is the Quran section itself
                    startDestination = AppRoute.Onboarding, // TEMP: always show onboarding to review it
                    modifier = modifier
                ) {
                    composable<AppRoute.Onboarding> {
                        if (edition == AppEdition.QURAN) QuranIntroScreen(onDone = { navController.navigate(AppRoute.Quran) })
                        else OnboardingScreen()
                    }
                    composable<AppRoute.Home> { HomeScreen() }
                    composable<AppRoute.PrayerTimes> { MiqatTimesScreen() }
                    composable<AppRoute.Qibla> { QiblaScreen() }
                    composable<AppRoute.Tracker> { TrackerScreen() }
                    composable<AppRoute.Azkar> { AzkarScreen() }
                    composable<AppRoute.Quran> {
                        QuranThemeHost { QuranIndexScreen() }
                    }
                    composable<AppRoute.QuranReader> { entry ->
                        val r = entry.toRoute<AppRoute.QuranReader>()
                        QuranThemeHost { QuranReaderScreen(surah = r.surah, ayah = r.ayah) }
                    }
                    composable<AppRoute.Studio> { entry ->
                        val r = entry.toRoute<AppRoute.Studio>()
                        val ayah by produceState<Ayah?>(null, r.surah, r.ayah) {
                            value = QuranRepository.ayah(r.surah, r.ayah)
                        }
                        ayah?.let { StudioScreen(ayahs = listOf(it)) }
                    }
                    composable<AppRoute.CollectionDetails> { entry ->
                        val r = entry.toRoute<AppRoute.CollectionDetails>()
                        QuranThemeHost { CollectionDetailsScreen(collectionId = r.collectionId) }
                    }
                    composable<AppRoute.Tasbih> { TasbihHubScreen() }
                    composable<AppRoute.TasbihHistory> { /* TasbihHistoryScreen() */ }
                    composable<AppRoute.TasbihCounter> { TasbihScreen() }
                    composable<AppRoute.Settings> { SettingsScreen() }
                    composable<AppRoute.Location> { LocationScreen() }
                    composable<AppRoute.PrayerCalc> { MiqatCalculationScreen() }
                    composable<AppRoute.Widgets> { WidgetGalleryScreen() }
                    // two separate screens, not one branching screen — the Quran app lists its
                    // reminders inline, the main app keeps its prayer-shaped one
                    composable<AppRoute.Notifications> {
                        if (edition == AppEdition.QURAN) QuranNotificationsScreen() else NotificationsScreen()
                    }
                    composable<AppRoute.PrayerFocus> { PrayerFocusScreen() }
                    composable<AppRoute.Sandbox> { SandboxScreen() }
                }
            }
            // the Quran app has nothing else to navigate to, so the drawer shell (and its menu icon)
            // stays out of the tree entirely rather than being present-but-empty
            if (edition == AppEdition.QURAN) AppContentHost { navHost() } else AppDrawer(drawerState) { navHost() }

            // shown once, on the launch after a damaged database was moved aside
            var recovered by remember { mutableStateOf(DatabaseRecovery.recoveredThisLaunch) }
            if (recovered) AppBottomSheet(
                onDismiss = { recovered = false; DatabaseRecovery.recoveredThisLaunch = false },
                title = stringResource(Res.string.something_went_wrong_with_your_saved_data),
                footer = {
                    AppButton(
                        text = stringResource(Res.string.got_it),
                        onClick = { recovered = false; DatabaseRecovery.recoveredThisLaunch = false },
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            ) {
                Text(
                    stringResource(Res.string.sorry_app_started_fresh_set_up_again),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}
