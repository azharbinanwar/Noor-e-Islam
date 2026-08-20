package com.kodeelite.nooreislam.core.database

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kodeelite.nooreislam.core.datetime.Now

/**
 * Keeps a database file Room cannot open from killing the app on every launch.
 *
 * SQLite only reports a bad file when something reads it, so this opens the file and asks it one
 * cheap question before Room ever touches it. Two answers mean trouble: it cannot be read at all,
 * or it was written by a newer build than this one — Room refuses to step a version down and would
 * throw on every launch. Either way the file is moved aside — renamed, never deleted, so the bytes
 * stay on the device — and Room creates a fresh one in its place.
 *
 * Deliberately narrow: a missing migration between versions this build knows about is still a hard
 * failure, because treating that as corruption would let one bad release wipe everyone's notes.
 */
fun quarantineIfUnusable(path: String, expectedVersion: Int): Boolean {
    if (!databaseFileExists(path)) return false
    val version = runCatching {
        val driver = BundledSQLiteDriver()
        val connection = driver.open(path)
        try {
            val statement = connection.prepare("PRAGMA user_version")
            try {
                if (statement.step()) statement.getInt(0) else 0
            } finally {
                statement.close()
            }
        } finally {
            connection.close()
        }
    }.getOrNull()
    // a fresh file reads 0 — Room stamps the version on first write, so it is not a downgrade
    if (version != null && version <= expectedVersion) return false
    // the -wal and -shm siblings belong to the same database and must travel with it
    moveDatabaseAside(path, "$path.corrupt-${Now.epochMillis()}")
    return true
}

/** True when the app started on a fresh database because the old one could not be opened. */
object DatabaseRecovery {
    var recoveredThisLaunch: Boolean = false
        internal set
}

internal expect fun databaseFileExists(path: String): Boolean

/** Renames the database and its -wal/-shm siblings. Nothing is deleted. */
internal expect fun moveDatabaseAside(path: String, to: String)
