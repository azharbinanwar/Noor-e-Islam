package com.kodeelite.nooreislam

import android.app.Application
import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.BuildType
import com.kodeelite.nooreislam.core.di.initKoin
import com.kodeelite.nooreislam.core.focus.FocusScheduling
import com.kodeelite.nooreislam.core.focus.PhoneSilencer
import com.kodeelite.nooreislam.core.platform.AppCtx
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.notifications.scheduler.NotificationScheduler
import com.kodeelite.nooreislam.feature.widget.WidgetPublisher
import java.util.Locale

class NoorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCtx.context = this // background code (the silence service) reads this
        initKoin(AppEdition.MAIN, if (BuildConfig.DEBUG) BuildType.DEBUG else BuildType.RELEASE, BuildConfig.GOOGLE_WEB_CLIENT_ID)
        Locale.setDefault(Locale(SettingsStore.language.value.code)) // background alerts build strings off the UI; mirror the app language so they aren't in the system language
        PhoneSilencer.rescheduleAll() // arm today's remaining prayer windows on cold start
        FocusScheduling.start() // re-arm whenever times or focus settings change
        NotificationScheduler.start() // build prayer/dhikr/surah reminders + re-arm on any change
        WidgetPublisher.start() // publish today's times to the widget store + re-arm widget refreshes
    }
}
