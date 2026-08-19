package com.kodeelite.nooreislam.feature.miqat.presentation.components

import androidx.compose.runtime.Composable
import com.kodeelite.nooreislam.core.datetime.format
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.feature.miqat.domain.MiqatTime
import com.kodeelite.nooreislam.feature.miqat.domain.endOf
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.time_from_to
import org.jetbrains.compose.resources.stringResource

/** "5:12 - 6:34" so it's clear when a prayer's window closes. Falls back to the start alone. */
@Composable
fun prayerWindow(times: List<MiqatTime>, prayer: Miqat, pattern: String): String? {
    val start = times.firstOrNull { it.miqat == prayer }?.at?.time?.format(pattern) ?: return null
    val end = times.endOf(prayer)?.format(pattern) ?: return start
    return stringResource(Res.string.time_from_to, start, end)
}
