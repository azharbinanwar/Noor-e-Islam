package com.kodeelite.nooreislam.core.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe routes for the entire app.
 */
@Serializable
sealed interface AppRoute {

    @Serializable
    data object Onboarding : AppRoute

    @Serializable
    data object Home : AppRoute

    @Serializable
    data object PrayerTimes : AppRoute

    @Serializable
    data object Qibla : AppRoute

    @Serializable
    data object Tracker : AppRoute

    @Serializable
    data object Azkar : AppRoute

    @Serializable
    data object Quran : AppRoute

    // surah:ayah, not a db id — the reader resolves the ruku to scroll to. Defaults open at the start.
    @Serializable
    data class QuranReader(val surah: Int = 1, val ayah: Int = 1) : AppRoute

    // canonical surah:ayah keys (one ayah now, list later); the screen loads the real Ayah
    @Serializable
    data class Studio(val surah: Int, val ayah: Int) : AppRoute

    @Serializable
    data class CollectionDetails(val collectionId: Long) : AppRoute

    @Serializable
    data object Tasbih : AppRoute

    @Serializable
    data class TasbihHistory(val dummy: Int = 0) : AppRoute

    @Serializable
    data object TasbihCounter : AppRoute

    @Serializable
    data object Settings : AppRoute

    @Serializable
    data object Location : AppRoute

    @Serializable
    data object PrayerCalc : AppRoute

    @Serializable
    data object Widgets : AppRoute

    @Serializable
    data object PrayerFocus : AppRoute

    @Serializable
    data object Sandbox : AppRoute

    @Serializable
    data object Calendar : AppRoute

    @Serializable
    data object NamesOfAllah : AppRoute

    @Serializable
    data object HajjUmrah : AppRoute

    @Serializable
    data object Community : AppRoute

    @Serializable
    data object Profile : AppRoute

    @Serializable
    data object Notifications : AppRoute

    @Serializable
    data object MosqueFinder : AppRoute

    @Serializable
    data object ZakatCalculator : AppRoute
}
