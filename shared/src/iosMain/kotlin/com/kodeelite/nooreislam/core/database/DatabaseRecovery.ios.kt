package com.kodeelite.nooreislam.core.database

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager

internal actual fun databaseFileExists(path: String): Boolean =
    NSFileManager.defaultManager.fileExistsAtPath(path)

@OptIn(ExperimentalForeignApi::class)
internal actual fun moveDatabaseAside(path: String, to: String) {
    val fm = NSFileManager.defaultManager
    listOf("", "-wal", "-shm").forEach { suffix ->
        val from = path + suffix
        if (fm.fileExistsAtPath(from)) fm.moveItemAtPath(from, toPath = to + suffix, error = null)
    }
}
