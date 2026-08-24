package com.kodeelite.nooreislam.core.platform

import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale

actual fun deviceCountryCode(): String? =
    NSLocale.currentLocale.countryCode?.takeIf { it.length == 2 }?.uppercase()
