package com.kodeelite.nooreislam.core.backup

actual object BackupArchive {
    actual suspend fun create(): BackupFile = throw BackupFormatException("backup is not available on iOS yet")
    actual suspend fun restore(bytes: ByteArray) { throw BackupFormatException("backup is not available on iOS yet") }
}
