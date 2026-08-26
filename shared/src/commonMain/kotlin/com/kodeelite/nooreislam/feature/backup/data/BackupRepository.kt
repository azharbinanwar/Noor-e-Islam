package com.kodeelite.nooreislam.feature.backup.data

import com.kodeelite.nooreislam.core.backup.BackupArchive
import com.kodeelite.nooreislam.core.constants.AppConst
import com.kodeelite.nooreislam.core.backup.BackupFormatException
import com.kodeelite.nooreislam.core.backup.GoogleSignIn
import com.kodeelite.nooreislam.core.backup.GoogleSignInException
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.platform.restartApp
import com.kodeelite.nooreislam.core.store.BackupStore
import com.kodeelite.nooreislam.core.store.BackupStore.Busy
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.Instant

/**
 * The backup use cases: connect an account, back up, restore, disconnect. Sign-in is handed in from
 * the screen because it needs the Activity; everything else here is plain. Progress and results go to
 * [BackupStore], which the screen watches.
 */
class BackupRepository(private val drive: DriveClient) {

    sealed interface Connect {
        data object Connected : Connect
        data object Cancelled : Connect
        data class Failed(val reason: String) : Connect
    }

    sealed interface Outcome {
        data object Done : Outcome
        data object NoToken : Outcome          // she declined the Drive consent
        data object NothingToRestore : Outcome
        data object Offline : Outcome
        data class Failed(val reason: String) : Outcome
    }

    // Ktor surfaces a dead network as an IO exception, never as a Drive error
    private fun Throwable.asOutcome(): Outcome = when (this) {
        is DriveClient.DriveException, is BackupFormatException -> Outcome.Failed(message ?: "")
        else -> if (this is CancellationException) throw this else Outcome.Offline
    }

    /** Account picker, then remember the choice. */
    suspend fun connect(signIn: GoogleSignIn): Connect {
        if (BackupStore.busy.value != Busy.Idle) return Connect.Cancelled
        BackupStore.setBusy(Busy.Connecting)
        return try {
            val account = signIn.connect()
            if (account == null) Connect.Cancelled else {
                if (account.email != BackupStore.account.value?.email) BackupStore.clearLast()
                BackupStore.setAccount(account)
                Connect.Connected
            }
        } catch (e: GoogleSignInException) {
            Connect.Failed(e.message ?: "")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Connect.Failed(e.message ?: "") // Play Services can throw its own kinds offline; never let one kill the app
        } finally {
            BackupStore.setBusy(Busy.Idle)
        }
    }

    /** Ask Drive what it holds for this account. A failed call changes nothing; only "no file" clears. */
    suspend fun checkRemote(signIn: GoogleSignIn): BackupStore.RemoteBackup? {
        val token = signIn.driveToken() ?: return BackupStore.remote.value
        val file = try {
            drive.find(token, AppConst.BACKUP_FILE_NAME)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return BackupStore.remote.value // offline or refused: keep what we knew
        }
        if (file == null) { BackupStore.setRemote(null); return null }
        val at = file.modifiedTime?.let { runCatching { Instant.parse(it).toEpochMilliseconds() }.getOrNull() } ?: Now.epochMillis()
        val remote = BackupStore.RemoteBackup(at, ((file.size?.toLongOrNull() ?: 0L) / 1024).toInt())
        BackupStore.setRemote(remote)
        return remote
    }

    suspend fun disconnect(signIn: GoogleSignIn) {
        signIn.disconnect()
        BackupStore.setAccount(null)
    }

    /** Zip the database and preferences and put them in the account's Drive app folder, replacing the last one. */
    suspend fun backUpNow(signIn: GoogleSignIn): Outcome {
        if (BackupStore.busy.value != Busy.Idle) return Outcome.Failed("")
        BackupStore.setBusy(Busy.BackingUp(0f))
        return try {
            val token = signIn.driveToken() ?: return Outcome.NoToken
            val file = BackupArchive.create()
            drive.upload(token, AppConst.BACKUP_FILE_NAME, file.bytes) { BackupStore.setBusy(Busy.BackingUp(it)) }
            BackupStore.recordBackup(Now.epochMillis(), file.sizeKb)
            BackupStore.setRemote(BackupStore.RemoteBackup(Now.epochMillis(), file.sizeKb))
            Outcome.Done
        } catch (e: Exception) {
            e.asOutcome()
        } finally {
            BackupStore.setBusy(Busy.Idle)
        }
    }

    /** Remove the Drive copy; this phone's data and the linked account stay. */
    suspend fun deleteRemote(signIn: GoogleSignIn): Outcome {
        if (BackupStore.busy.value != Busy.Idle) return Outcome.Failed("")
        BackupStore.setBusy(Busy.Deleting)
        return try {
            val token = signIn.driveToken() ?: return Outcome.NoToken
            val file = drive.find(token, AppConst.BACKUP_FILE_NAME) ?: return Outcome.NothingToRestore
            drive.delete(token, file.id)
            BackupStore.clearLast()
            Outcome.Done
        } catch (e: Exception) {
            e.asOutcome()
        } finally {
            BackupStore.setBusy(Busy.Idle)
        }
    }

    /** Fetch the Drive file, put it in place of this phone's data, and start over. Returns only on failure. */
    suspend fun restore(signIn: GoogleSignIn): Outcome {
        if (BackupStore.busy.value != Busy.Idle) return Outcome.Failed("")
        BackupStore.setBusy(Busy.Restoring(0f))
        return try {
            val token = signIn.driveToken() ?: return Outcome.NoToken
            val file = drive.find(token, AppConst.BACKUP_FILE_NAME) ?: return Outcome.NothingToRestore
            val bytes = drive.download(token, file.id) { BackupStore.setBusy(Busy.Restoring(it)) }
            BackupArchive.restore(bytes)
            restartApp()
            Outcome.Done
        } catch (e: Exception) {
            e.asOutcome()
        } finally {
            BackupStore.setBusy(Busy.Idle)
        }
    }
}
