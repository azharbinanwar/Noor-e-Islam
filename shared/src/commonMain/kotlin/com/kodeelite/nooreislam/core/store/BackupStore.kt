package com.kodeelite.nooreislam.core.store

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

/** What the backup screen shows and edits. The Drive work itself lands behind [backUpNow] and [restore]. */
object BackupStore {

    sealed interface Busy {
        data object Idle : Busy
        data class BackingUp(val progress: Float) : Busy
        data class Restoring(val progress: Float) : Busy
    }

    private val _account = MutableStateFlow(PrefsService.getStringOrNull(PrefConst.BACKUP_ACCOUNT))
    val account: StateFlow<String?> = _account.asStateFlow()

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

    private val _busy = MutableStateFlow<Busy>(Busy.Idle)
    val busy: StateFlow<Busy> = _busy.asStateFlow()

    fun setAccount(email: String?) {
        if (email == null) PrefsService.remove(PrefConst.BACKUP_ACCOUNT) else PrefsService.putString(PrefConst.BACKUP_ACCOUNT, email)
        _account.value = email
    }

    fun setFrequency(value: BackupFrequency) {
        PrefsService.putString(PrefConst.BACKUP_FREQUENCY, value.name)
        _frequency.value = value
    }

    fun setNetwork(value: BackupNetwork) {
        PrefsService.putString(PrefConst.BACKUP_NETWORK, value.name)
        _network.value = value
    }

    fun setTime(hour: Int, minute: Int) {
        PrefsService.putInt(PrefConst.BACKUP_HOUR, hour)
        PrefsService.putInt(PrefConst.BACKUP_MINUTE, minute)
        _time.value = LocalTime(hour, minute)
    }

    fun setWeekday(day: DayOfWeek) {
        PrefsService.putString(PrefConst.BACKUP_WEEKDAY, day.name)
        _weekday.value = day
    }

    // todo: zip the database + prefs and upload to the account's Drive app folder; this only walks the UI
    suspend fun backUpNow() {
        for (i in 1..20) { _busy.value = Busy.BackingUp(i / 20f); kotlinx.coroutines.delay(80.milliseconds) }
        val at = Now.epochMillis()
        PrefsService.putString(PrefConst.BACKUP_LAST_AT, at.toString())
        PrefsService.putInt(PrefConst.BACKUP_LAST_SIZE_KB, 2140)
        _lastAt.value = at; _lastSizeKb.value = 2140
        _busy.value = Busy.Idle
    }

    // todo: download the Drive file, replace the database + prefs, then restart
    suspend fun restore() {
        for (i in 1..20) { _busy.value = Busy.Restoring(i / 20f); kotlinx.coroutines.delay(80.milliseconds) }
        _busy.value = Busy.Idle
    }

}
