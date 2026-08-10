package com.kodeelite.nooreislam.core.platform

import platform.Foundation.NSBundle

// CFBundleShortVersionString, i.e. MARKETING_VERSION from Config.xcconfig
actual val appVersion: String
    get() = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: ""
