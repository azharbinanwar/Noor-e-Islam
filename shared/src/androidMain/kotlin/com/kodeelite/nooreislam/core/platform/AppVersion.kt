package com.kodeelite.nooreislam.core.platform

// versionName from the manifest, i.e. whatever build.gradle.kts declared for this build
actual val appVersion: String
    get() = runCatching {
        AppCtx.context.packageManager.getPackageInfo(AppCtx.context.packageName, 0).versionName
    }.getOrNull().orEmpty()
