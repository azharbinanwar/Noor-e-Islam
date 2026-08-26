package com.kodeelite.nooreislam.core.backup

/** A finished backup: the zip bytes and the size the screen shows. */
class BackupFile(val bytes: ByteArray) {
    val sizeKb: Int get() = (bytes.size + 512) / 1024
}

/** The backup refused before touching anything: the file is from a newer app, or not ours at all. */
class BackupFormatException(message: String) : Exception(message)

/**
 * One zip: the database files as they are on disk, every preference, and a manifest saying which
 * app and schema wrote it. Restore puts the files back and hands the schema to Room, which migrates
 * forward the same way an app update does; a file from a newer schema is refused.
 */
expect object BackupArchive {
    suspend fun create(): BackupFile

    /** Replaces the database and preferences; the caller restarts the app right after. */
    suspend fun restore(bytes: ByteArray)
}
