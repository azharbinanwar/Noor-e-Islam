package com.kodeelite.nooreislam.feature.quran.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile

// rewrite into the Documents dir only when missing or a different size, so a shipped db update takes
// effect on an existing install; writing once left every updated app reading the database it was
// first installed with, and a new column read as "no such column"
@OptIn(ExperimentalForeignApi::class)
actual fun materializeDb(name: String, bytes: ByteArray): String {
    val fm = NSFileManager.defaultManager
    val dir = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true).first() as String
    val path = "$dir/$name"
    val onDisk = (fm.attributesOfItemAtPath(path, null)?.get(NSFileSize) as? NSNumber)?.longValue
    if (onDisk != bytes.size.toLong()) {
        val data = bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
        data.writeToFile(path, atomically = true)
    }
    return path
}
