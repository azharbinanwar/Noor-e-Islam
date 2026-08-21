package com.kodeelite.nooreislam.feature.tracker.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kodeelite.nooreislam.core.datetime.formatted
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.tracker.data.ExemptionStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.skip_the_days_prayer_is_not_owed
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** What the exemption row says, so Settings and Home can't drift apart. */
@Composable
fun exemptionSubtitle(): String {
    val exemption = koinInject<ExemptionStore>()
    val running by exemption.running.collectAsState()
    val dateFormat by SettingsStore.gregorianDateFormat.collectAsState()
    val period = running ?: return stringResource(Res.string.skip_the_days_prayer_is_not_owed)
    // the last exempt day is stored, so prayer is owed again the morning after
    val resumes = period.endDate?.plus(1, DateTimeUnit.DAY)?.formatted(dateFormat)
    return if (resumes != null) "No prayer owed until $resumes" else "No prayer owed until you turn this off"
}
