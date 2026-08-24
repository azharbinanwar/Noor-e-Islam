package com.kodeelite.nooreislam.core.assets

import com.kodeelite.nooreislam.core.platform.AppCtx

internal actual fun appStoragePath(): String = AppCtx.context.filesDir.absolutePath
