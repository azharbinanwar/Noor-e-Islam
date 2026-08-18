package com.kodeelite.nooreislam.feature.tracker.data

import androidx.room.Entity
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.PrayerTrackerStatus
import kotlinx.datetime.LocalDate

// (date, prayer) key so re-tapping replaces the row instead of stacking duplicates
@Entity(tableName = "tracked_prayer", primaryKeys = ["date", "prayer"])
data class TrackedPrayer(
    val date: LocalDate,
    val prayer: Miqat,
    val status: PrayerTrackerStatus,
)
