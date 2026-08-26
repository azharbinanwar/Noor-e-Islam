package com.kodeelite.noorequran

import android.app.Application
import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.BuildType
import com.kodeelite.nooreislam.core.di.initKoin
import com.kodeelite.nooreislam.core.platform.AppCtx
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.notifications.scheduler.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Locale

class QuranApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCtx.context = this // the Quran DB (QuranDb.android.kt) and other shared Android code read this
        initKoin(AppEdition.QURAN, if (BuildConfig.DEBUG) BuildType.DEBUG else BuildType.RELEASE, BuildConfig.GOOGLE_WEB_CLIENT_ID) // cheap: Koin definitions are lazy, nothing is built here

        // Touching a store loads the prefs file off disk, which would block the main thread for the
        // whole splash — the splash icon animation is drawn on that thread, so it drops frames.
        // Neither of these is needed before the first frame, so they warm up in the background.
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            Locale.setDefault(Locale(SettingsStore.language.value.code)) // background alerts build strings off the UI; mirror the app language so they aren't in the system language
            NotificationScheduler.start() // build Surah Al-Mulk/Al-Kahf reminders + re-arm on any change
        }
    }
}
