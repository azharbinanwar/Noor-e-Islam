package com.kodeelite.nooreislam.feature.backup.data

import com.kodeelite.nooreislam.core.backup.BackupScheduler
import com.kodeelite.nooreislam.core.backup.GoogleAccount
import com.kodeelite.nooreislam.core.constants.PrefConst
import com.kodeelite.nooreislam.core.constants.defaults.BackupDefaults
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.enums.BackupFrequency
import com.kodeelite.nooreislam.core.enums.BackupNetwork
import com.kodeelite.nooreislam.core.prefs.PrefsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlin.time.Duration.Companion.milliseconds

/** What the backup screen shows and edits: account, schedule, last result, and whether a job is running. State only. */
object BackupStore {

    sealed interface Busy {
        data object Idle : Busy
        data object Connecting : Busy
        data object Deleting : Busy
        data class BackingUp(val progress: Float) : Busy
        data class Restoring(val progress: Float) : Busy
    }

    private val _account = MutableStateFlow(
        PrefsService.getStringOrNull(PrefConst.BACKUP_ACCOUNT)?.let {
            GoogleAccount(it, PrefsService.getStringOrNull(PrefConst.BACKUP_ACCOUNT_NAME), PrefsService.getStringOrNull(PrefConst.BACKUP_ACCOUNT_PHOTO))
        },
    )
    val account: StateFlow<GoogleAccount?> = _account.asStateFlow()

    private val _lastAt = MutableStateFlow(PrefsService.getStringOrNull(PrefConst.BACKUP_LAST_AT)?.toLongOrNull())
    val lastAt: StateFlow<Long?> = _lastAt.asStateFlow()

    private val _lastSizeKb = MutableStateFlow(PrefsService.getInt(PrefConst.BACKUP_LAST_SIZE_KB, 0))
    val lastSizeKb: StateFlow<Int> = _lastSizeKb.asStateFlow()

    private val _frequency = MutableStateFlow(BackupFrequency.from(PrefsService.getStringOrNull(PrefConst.BACKUP_FREQUENCY)))
    val frequency: StateFlow<BackupFrequency> = _frequency.asStateFlow()

    private val _network = MutableStateFlow(BackupNetwork.from(PrefsService.getStringOrNull(PrefConst.BACKUP_NETWORK)))
    val network: StateFlow<BackupNetwork> = _network.asStateFlow()

    private val _time = MutableStateFlow(
        LocalTime(PrefsService.getInt(PrefConst.BACKUP_HOUR, BackupDefaults.HOUR), PrefsService.getInt(PrefConst.BACKUP_MINUTE, BackupDefaults.MINUTE)),
    )
    val time: StateFlow<LocalTime> = _time.asStateFlow()

    private val _weekday = MutableStateFlow(
        PrefsService.getStringOrNull(PrefConst.BACKUP_WEEKDAY)?.let { v -> DayOfWeek.entries.firstOrNull { it.name == v } } ?: BackupDefaults.WEEKDAY,
    )
    val weekday: StateFlow<DayOfWeek> = _weekday.asStateFlow()

    /** What Drive holds for the connected account, looked up on connect and when the screen opens. */
    data class RemoteBackup(val atMillis: Long, val sizeKb: Int)

    private val _remote = MutableStateFlow<RemoteBackup?>(null)
    val remote: StateFlow<RemoteBackup?> = _remote.asStateFlow()

    private val _busy = MutableStateFlow<Busy>(Busy.Idle)
    val busy: StateFlow<Busy> = _busy.asStateFlow()

    fun setAccount(account: GoogleAccount?) {
        if (account == null) {
            PrefsService.remove(PrefConst.BACKUP_ACCOUNT); PrefsService.remove(PrefConst.BACKUP_ACCOUNT_NAME); PrefsService.remove(PrefConst.BACKUP_ACCOUNT_PHOTO)
            clearLast()
        } else {
            PrefsService.putString(PrefConst.BACKUP_ACCOUNT, account.email)
            account.name?.let { PrefsService.putString(PrefConst.BACKUP_ACCOUNT_NAME, it) } ?: PrefsService.remove(PrefConst.BACKUP_ACCOUNT_NAME)
            account.photoUrl?.let { PrefsService.putString(PrefConst.BACKUP_ACCOUNT_PHOTO, it) } ?: PrefsService.remove(PrefConst.BACKUP_ACCOUNT_PHOTO)
        }
        _account.value = account
        BackupScheduler.reschedule()
    }

    fun setFrequency(value: BackupFrequency) {
        PrefsService.putString(PrefConst.BACKUP_FREQUENCY, value.name)
        _frequency.value = value
    
        BackupScheduler.reschedule()
    }

    fun setNetwork(value: BackupNetwork) {
        PrefsService.putString(PrefConst.BACKUP_NETWORK, value.name)
        _network.value = value
    
        BackupScheduler.reschedule()
    }

    fun setTime(hour: Int, minute: Int) {
        PrefsService.putInt(PrefConst.BACKUP_HOUR, hour)
        PrefsService.putInt(PrefConst.BACKUP_MINUTE, minute)
        _time.value = LocalTime(hour, minute)
    
        BackupScheduler.reschedule()
    }

    fun setWeekday(day: DayOfWeek) {
        PrefsService.putString(PrefConst.BACKUP_WEEKDAY, day.name)
        _weekday.value = day
    
        BackupScheduler.reschedule()
    }

    fun setBusy(value: Busy) { _busy.value = value }

    private val periodMillis: Long
        get() = if (_frequency.value == BackupFrequency.Weekly) 7L * 86_400_000 else 86_400_000

    /** Three scheduled runs have come and gone without a backup landing. */
    fun isBehind(): Boolean {
        if (_account.value == null || _frequency.value == BackupFrequency.Off) return false
        val last = _lastAt.value ?: return false
        return Now.epochMillis() - last > 3 * periodMillis
    }

    /** A run was due and nothing has landed since; worth doing in the foreground while the app is open. */
    fun isOverdue(): Boolean {
        if (_account.value == null || _frequency.value == BackupFrequency.Off) return false
        val last = _lastAt.value ?: return true
        return Now.epochMillis() - last > periodMillis + 3_600_000
    }

    fun setRemote(value: RemoteBackup?) { _remote.value = value }

    /** A different account knows nothing about this phone's last backup; start from what Drive says. */
    fun clearLast() {
        PrefsService.remove(PrefConst.BACKUP_LAST_AT); PrefsService.remove(PrefConst.BACKUP_LAST_SIZE_KB)
        _lastAt.value = null; _lastSizeKb.value = 0; _remote.value = null
    }

    fun recordBackup(atMillis: Long, sizeKb: Int) {
        PrefsService.putString(PrefConst.BACKUP_LAST_AT, atMillis.toString())
        PrefsService.putInt(PrefConst.BACKUP_LAST_SIZE_KB, sizeKb)
        _lastAt.value = atMillis; _lastSizeKb.value = sizeKb
    }
}
