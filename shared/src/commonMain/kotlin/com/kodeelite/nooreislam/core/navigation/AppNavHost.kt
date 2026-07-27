package com.kodeelite.nooreislam.core.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kodeelite.nooreislam.core.components.AppDrawer
import com.kodeelite.nooreislam.core.components.LocalDrawerState
import com.kodeelite.nooreislam.core.components.LocalOverlay
import com.kodeelite.nooreislam.core.components.OverlayState
import com.kodeelite.nooreislam.feature.azkar.presentation.AzkarScreen
import com.kodeelite.nooreislam.feature.focus.presentation.PrayerFocusScreen
import com.kodeelite.nooreislam.feature.home.presentation.HomeScreen
import com.kodeelite.nooreislam.feature.miqat.presentation.MiqatTimesScreen
import com.kodeelite.nooreislam.feature.notifications.presentation.NotificationsScreen
import com.kodeelite.nooreislam.feature.onboarding.presentation.OnboardingScreen
import com.kodeelite.nooreislam.feature.qibla.presentation.QiblaScreen
import com.kodeelite.nooreislam.feature.quran.data.Ayah
import com.kodeelite.nooreislam.feature.quran.data.QuranRepository
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

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    AppNavigatorHost(navController) {
        // drawer + overlay hoisted once around the NavHost; screens open it via LocalDrawerState
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val overlay = remember { OverlayState() }
        CompositionLocalProvider(
            LocalDrawerState provides drawerState,
            LocalOverlay provides overlay,
        ) {
            AppDrawer(drawerState) {
                NavHost(
                    navController = navController,
                    startDestination = AppRoute.Home,
                    modifier = modifier
                ) {
                    composable<AppRoute.Onboarding> { OnboardingScreen() }
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
                    composable<AppRoute.Tasbih> { TasbihHubScreen() }
                    composable<AppRoute.TasbihHistory> { /* TasbihHistoryScreen() */ }
                    composable<AppRoute.TasbihCounter> { TasbihScreen() }
                    composable<AppRoute.Settings> { SettingsScreen() }
                    composable<AppRoute.Location> { LocationScreen() }
                    composable<AppRoute.PrayerCalc> { MiqatCalculationScreen() }
                    composable<AppRoute.Widgets> { WidgetGalleryScreen() }
                    composable<AppRoute.Notifications> { NotificationsScreen() }
                    composable<AppRoute.PrayerFocus> { PrayerFocusScreen() }
                    composable<AppRoute.Sandbox> { SandboxScreen() }
                }
            }
        }
    }
}
