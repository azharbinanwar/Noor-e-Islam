package com.kodeelite.nooreislam.core.platform

import android.os.Build

// "1.0.0+3" — versionName plus the build number, so a tester can name the exact build they are on
actual val appVersion: String
    get() = runCatching {
        val info = AppCtx.context.packageManager.getPackageInfo(AppCtx.context.packageName, 0)
        val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode else info.versionCode.toLong()
        "${info.versionName}+$code"
    }.getOrNull().orEmpty()
