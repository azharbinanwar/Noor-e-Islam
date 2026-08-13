package com.kodeelite.nooreislam.core.platform

import platform.Foundation.NSBundle

// "1.0.0+3" — MARKETING_VERSION plus CURRENT_PROJECT_VERSION, the same shape Android shows
actual val appVersion: String
    get() {
        val bundle = NSBundle.mainBundle
        val name = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: return ""
        val build = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String
        return if (build.isNullOrBlank()) name else "$name+$build"
    }
