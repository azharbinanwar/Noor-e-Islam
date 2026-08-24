package com.kodeelite.nooreislam.core.assets

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.Foundation.NSUserDomainMask

/** Application Support, created on first ask and kept out of iCloud backups. */
@OptIn(ExperimentalForeignApi::class)
internal actual fun appStoragePath(): String {
    val url = NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    ) as NSURL
    url.setResourceValue(true, forKey = NSURLIsExcludedFromBackupKey, error = null)
    return url.path!!
}
