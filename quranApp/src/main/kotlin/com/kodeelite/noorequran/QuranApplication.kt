package com.kodeelite.noorequran

import android.app.Application
import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.di.initKoin
import com.kodeelite.nooreislam.core.platform.AppCtx
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.notifications.scheduler.NotificationScheduler
import java.util.Locale

class QuranApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCtx.context = this // the Quran DB (QuranDb.android.kt) and other shared Android code read this
        initKoin(AppEdition.QURAN)
        Locale.setDefault(Locale(SettingsStore.language.value.code)) // background alerts build strings off the UI; mirror the app language so they aren't in the system language
        NotificationScheduler.start() // build Surah Al-Mulk/Al-Kahf reminders + re-arm on any change
    }
}
