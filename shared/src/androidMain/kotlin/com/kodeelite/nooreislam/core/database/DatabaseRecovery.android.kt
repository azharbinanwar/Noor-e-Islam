package com.kodeelite.nooreislam.core.database

import java.io.File

internal actual fun databaseFileExists(path: String): Boolean = File(path).exists()

internal actual fun moveDatabaseAside(path: String, to: String) {
    listOf("", "-wal", "-shm").forEach { suffix ->
        val from = File(path + suffix)
        if (from.exists()) from.renameTo(File(to + suffix))
    }
}
