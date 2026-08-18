package com.kodeelite.nooreislam.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kodeelite.nooreislam.core.constants.AppConst
import com.kodeelite.nooreislam.feature.quran.data.Bookmark
import com.kodeelite.nooreislam.feature.quran.data.BookmarksDao
import com.kodeelite.nooreislam.feature.quran.data.Collection
import com.kodeelite.nooreislam.feature.quran.data.CollectionAyah
import com.kodeelite.nooreislam.feature.quran.data.CollectionAyahDao
import com.kodeelite.nooreislam.feature.quran.data.CollectionDao
import com.kodeelite.nooreislam.feature.quran.data.Highlight
import com.kodeelite.nooreislam.feature.quran.data.HighlightsDao
import com.kodeelite.nooreislam.feature.quran.data.Note
import com.kodeelite.nooreislam.feature.quran.data.NotesDao
import com.kodeelite.nooreislam.feature.notifications.data.SurahReminder
import com.kodeelite.nooreislam.feature.notifications.data.SurahReminderDao
import com.kodeelite.nooreislam.feature.studio.data.StudioCreationDao
import com.kodeelite.nooreislam.feature.studio.data.StudioCreationEntity
import com.kodeelite.nooreislam.feature.tracker.data.ExcusedPeriod
import com.kodeelite.nooreislam.feature.tracker.data.ExcusedPeriodDao
import com.kodeelite.nooreislam.feature.tracker.data.TrackedPrayer
import com.kodeelite.nooreislam.feature.tracker.data.TrackedPrayerDao

// exportSchema off while we use destructive migration in dev; turn on when real Migrations ship.
@Database(
    entities = [TrackedPrayer::class, ExcusedPeriod::class, ScheduledNotificationEntity::class, StudioCreationEntity::class, Bookmark::class, Highlight::class, Note::class, Collection::class, CollectionAyah::class, SurahReminder::class],
    version = AppConst.DB_VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackedPrayerDao(): TrackedPrayerDao
    abstract fun excusedPeriodDao(): ExcusedPeriodDao
    abstract fun scheduledNotificationDao(): ScheduledNotificationDao
    abstract fun studioCreationDao(): StudioCreationDao
    abstract fun bookmarksDao(): BookmarksDao
    abstract fun highlightsDao(): HighlightsDao
    abstract fun notesDao(): NotesDao
    abstract fun collectionDao(): CollectionDao
    abstract fun collectionAyahDao(): CollectionAyahDao
    abstract fun surahReminderDao(): SurahReminderDao
}

/** The Room compiler (KSP) generates the actual implementation per platform. */
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/** Finalize a platform-provided builder into a database (defaults to Dispatchers.IO). */
fun getRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        // ponytail: no persisted data shipped yet, so wipe on schema bump. Add real Migrations before that changes.
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
