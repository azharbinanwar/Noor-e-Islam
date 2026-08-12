package com.kodeelite.nooreislam.feature.notifications.store

import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.constants.defaults.NotificationDefaults
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.feature.notifications.data.SurahReminder
import com.kodeelite.nooreislam.feature.notifications.data.SurahReminderDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The user's surah reminders. Rows shipped with the app (Al-Mulk, Al-Kahf, …) come from
 * [NotificationDefaults.SurahReminders] and are seeded once into an empty table; from then on they
 * are the user's, editable like any other row but not deletable.
 */
class SurahReminderStore(
    private val scope: CoroutineScope,
    private val dao: SurahReminderDao,
    private val edition: AppEdition,
) {
    val reminders: StateFlow<List<SurahReminder>> =
        dao.allFlow().stateIn(scope, SharingStarted.Eagerly, emptyList())

    init {
        scope.launch { seedOnce() }
    }

    fun save(row: SurahReminder) {
        scope.launch { dao.upsert(if (row.id == 0L) row.copy(createdAt = Now.epochMillis()) else row) }
    }

    fun setEnabled(row: SurahReminder, enabled: Boolean) {
        scope.launch { dao.upsert(row.copy(enabled = enabled)) }
    }

    fun delete(row: SurahReminder) {
        scope.launch { dao.delete(row) }
    }

    // An empty table means a fresh database: seeds can't be deleted, so they're there once written.
    // Deliberately not a pref — prefs survive the destructive migration that wipes the table, which
    // would leave a wiped database with seeding already marked done.
    private suspend fun seedOnce() {
        if (dao.count() > 0) return
        val at = Now.epochMillis()
        NotificationDefaults.SurahReminders.seedsFor(edition).forEach { dao.upsert(it.copy(createdAt = at)) }
    }
}
