package com.example.miqatapp.core.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.miqatapp.core.components.AppDrawer
import com.example.miqatapp.core.components.LocalDrawerState
import com.example.miqatapp.core.components.LocalOverlay
import com.example.miqatapp.core.components.OverlayState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.miqatapp.feature.onboarding.presentation.OnboardingScreen
import com.example.miqatapp.feature.quran.presentation.QuranIndexScreen
import com.example.miqatapp.feature.quran.presentation.QuranReaderScreen
import com.example.miqatapp.feature.studio.presentation.StudioScreen
import com.example.miqatapp.feature.quran.data.Ayah
import com.example.miqatapp.feature.quran.data.AyahRef
import com.example.miqatapp.feature.quran.data.QuranRepository
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import com.example.miqatapp.feature.quran.presentation.QuranThemeHost
import com.example.miqatapp.feature.tasbih.presentation.TasbihHubScreen
import com.example.miqatapp.feature.tasbih.presentation.TasbihScreen
import com.example.miqatapp.feature.home.presentation.HomeScreen
import com.example.miqatapp.feature.miqat.presentation.MiqatTimesScreen
import com.example.miqatapp.feature.qibla.presentation.QiblaScreen
import com.example.miqatapp.feature.tracker.presentation.TrackerScreen
import com.example.miqatapp.feature.azkar.presentation.AzkarScreen
import com.example.miqatapp.feature.settings.presentation.SettingsScreen
import com.example.miqatapp.feature.settings.presentation.LocationScreen
import com.example.miqatapp.feature.settings.presentation.MiqatCalculationScreen
import com.example.miqatapp.feature.settings.presentation.WidgetGalleryScreen
import com.example.miqatapp.feature.notifications.presentation.NotificationsScreen
import com.example.miqatapp.feature.focus.presentation.PrayerFocusScreen
import com.example.miqatapp.feature.sandbox.presentation.SandboxScreen

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
                        QuranThemeHost { QuranReaderScreen(startId = entry.toRoute<AppRoute.QuranReader>().startId) }
                    }
                    composable<AppRoute.Studio> { entry ->
                        val r = entry.toRoute<AppRoute.Studio>()
                        val ayah by produceState<Ayah?>(null, r.surah, r.ayah) {
                            value = QuranRepository.ayah(AyahRef(r.surah, r.ayah))
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
