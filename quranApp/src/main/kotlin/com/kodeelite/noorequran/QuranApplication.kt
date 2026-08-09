package com.kodeelite.noorequran

import android.app.Application
import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.di.initKoin
import com.kodeelite.nooreislam.core.platform.AppCtx

class QuranApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCtx.context = this // the Quran DB (QuranDb.android.kt) and other shared Android code read this
        initKoin(AppEdition.QURAN)
    }
}
