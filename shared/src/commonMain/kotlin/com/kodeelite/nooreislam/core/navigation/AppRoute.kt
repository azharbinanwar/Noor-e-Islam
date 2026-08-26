package com.kodeelite.nooreislam.core.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Type-safe routes for the entire app.
 */
@Serializable
sealed interface AppRoute {

    @Serializable
    @SerialName("Onboarding")
    data object Onboarding : AppRoute

    @Serializable
    @SerialName("Home")
    data object Home : AppRoute

    @Serializable
    @SerialName("PrayerTimes")
    data object PrayerTimes : AppRoute

    @Serializable
    @SerialName("Qibla")
    data object Qibla : AppRoute

    @Serializable
    @SerialName("Tracker")
    data object Tracker : AppRoute

    @Serializable
    @SerialName("Azkar")
    data object Azkar : AppRoute

    @Serializable
    @SerialName("Quran")
    data object Quran : AppRoute

    // surah:ayah, not a db id — the reader resolves the ruku to scroll to. Defaults open at the start.
    @Serializable
    @SerialName("QuranReader")
    data class QuranReader(val surah: Int = 1, val ayah: Int = 1) : AppRoute

    @Serializable
    @SerialName("ReadingMarks")
    data object ReadingMarks : AppRoute

    // canonical surah:ayah keys (one ayah now, list later); the screen loads the real Ayah
    @Serializable
    @SerialName("Studio")
    data class Studio(val surah: Int, val ayah: Int) : AppRoute

    @Serializable
    @SerialName("CollectionDetails")
    data class CollectionDetails(val collectionId: Long) : AppRoute

    @Serializable
    @SerialName("Tasbih")
    data object Tasbih : AppRoute

    @Serializable
    @SerialName("TasbihHistory")
    data class TasbihHistory(val dummy: Int = 0) : AppRoute

    @Serializable
    @SerialName("TasbihCounter")
    data object TasbihCounter : AppRoute

    // [open] is a catalog anchor: search lands on Settings with that row already open
    @Serializable
    @SerialName("Settings")
    data class Settings(val open: String? = null) : AppRoute

    @Serializable
    @SerialName("Location")
    data object Location : AppRoute

    @Serializable
    @SerialName("PrayerCalc")
    data object PrayerCalc : AppRoute

    @Serializable
    @SerialName("Widgets")
    data object Widgets : AppRoute

    @Serializable
    @SerialName("Focus")
    data object Focus : AppRoute

    @Serializable
    @SerialName("Backup")
    data object Backup : AppRoute

    @Serializable
    @SerialName("Sandbox")
    data object Sandbox : AppRoute

    @Serializable
    @SerialName("TrackerLab")
    data object TrackerLab : AppRoute

    @Serializable
    @SerialName("SkyLab")
    data object SkyLab : AppRoute

    @Serializable
    @SerialName("Calendar")
    data object Calendar : AppRoute

    @Serializable
    @SerialName("NamesOfAllah")
    data object NamesOfAllah : AppRoute

    @Serializable
    @SerialName("HajjUmrah")
    data object HajjUmrah : AppRoute

    @Serializable
    @SerialName("Community")
    data object Community : AppRoute

    @Serializable
    @SerialName("Profile")
    data object Profile : AppRoute

    @Serializable
    @SerialName("Notifications")
    data object Notifications : AppRoute

    @Serializable
    @SerialName("MosqueFinder")
    data object MosqueFinder : AppRoute

    @Serializable
    @SerialName("ZakatCalculator")
    data object ZakatCalculator : AppRoute
}
