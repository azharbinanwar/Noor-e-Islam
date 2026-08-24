package com.kodeelite.nooreislam.core.platform

import java.util.Locale

actual fun deviceCountryCode(): String? =
    Locale.getDefault().country.takeIf { it.length == 2 }?.uppercase()
