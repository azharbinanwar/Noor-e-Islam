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
fun quarantineIfUnusable(path: String, expectedVersion: Int, expectedTables: List<String>): Boolean {
    if (!databaseFileExists(path)) return false
    val state = runCatching {
        val driver = BundledSQLiteDriver()
        val connection = driver.open(path)
        try {
            var version = 0
            val versionStatement = connection.prepare("PRAGMA user_version")
            try {
                if (versionStatement.step()) version = versionStatement.getInt(0)
            } finally {
                versionStatement.close()
            }
            val tables = mutableSetOf<String>()
            val tableStatement = connection.prepare("SELECT name FROM sqlite_master WHERE type = 'table'")
            try {
                while (tableStatement.step()) tables += tableStatement.getText(0)
            } finally {
                tableStatement.close()
            }
            version to tables
        } finally {
            connection.close()
        }
    }.getOrNull()

    if (state != null) {
        val (version, tables) = state
        // a fresh file reads 0 and has no tables yet — Room stamps both on first write
        val fresh = version == 0 && tables.none { it in expectedTables }
        val newer = version > expectedVersion
        // same version, different shape: an entity renamed or added without a version bump. Room only
        // finds this after opening, and then throws, so it has to be caught out here.
        val drifted = !fresh && expectedTables.any { it !in tables }
        if (!newer && !drifted) return false
    }
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
