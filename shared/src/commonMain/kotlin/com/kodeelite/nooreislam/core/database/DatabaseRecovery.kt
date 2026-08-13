package com.kodeelite.nooreislam.core.database

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kodeelite.nooreislam.core.datetime.Now

/**
 * Keeps a damaged database file from killing the app on every launch.
 *
 * SQLite only reports a bad file when something reads it, so this opens the file and asks it one
 * cheap question before Room ever touches it. If that fails the file is moved aside — renamed, never
 * deleted, so the bytes stay on the device — and Room creates a fresh one in its place.
 *
 * Deliberately narrow: it recovers from a file that cannot be read at all, not from every error.
 * Treating any failure as corruption would mean one bad release silently wipes everyone's notes.
 */
fun quarantineIfUnreadable(path: String): Boolean {
    if (!databaseFileExists(path)) return false
    val readable = runCatching {
        val driver = BundledSQLiteDriver()
        val connection = driver.open(path)
        try {
            val statement = connection.prepare("PRAGMA user_version")
            try {
                statement.step()
            } finally {
                statement.close()
            }
        } finally {
            connection.close()
        }
    }.isSuccess
    if (readable) return false
    // the -wal and -shm siblings belong to the same database and must travel with it
    moveDatabaseAside(path, "$path.corrupt-${Now.epochMillis()}")
    return true
}

/** True when the app started on a fresh database because the old one was unreadable. */
object DatabaseRecovery {
    var recoveredThisLaunch: Boolean = false
        internal set
}

internal expect fun databaseFileExists(path: String): Boolean

/** Renames the database and its -wal/-shm siblings. Nothing is deleted. */
internal expect fun moveDatabaseAside(path: String, to: String)
