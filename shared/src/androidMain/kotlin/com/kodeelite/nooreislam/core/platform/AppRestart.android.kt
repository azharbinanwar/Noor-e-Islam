package com.kodeelite.nooreislam.core.platform

import android.content.Intent
import kotlin.system.exitProcess

actual fun restartApp() {
    val ctx = AppCtx.context
    val launch = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)
        ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) ?: return
    ctx.startActivity(launch)
    exitProcess(0)
}
