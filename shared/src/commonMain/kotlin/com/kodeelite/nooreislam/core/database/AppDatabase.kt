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
import com.kodeelite.nooreislam.feature.quran.data.Highlight
import com.kodeelite.nooreislam.feature.quran.data.HighlightsDao
import com.kodeelite.nooreislam.feature.quran.data.Note
import com.kodeelite.nooreislam.feature.quran.data.NotesDao
import com.kodeelite.nooreislam.feature.studio.data.StudioCreationDao
import com.kodeelite.nooreislam.feature.studio.data.StudioCreationEntity

// exportSchema off while we use destructive migration in dev; turn on when real Migrations ship.
@Database(
    entities = [PrayerLogEntity::class, ScheduledNotificationEntity::class, StudioCreationEntity::class, Bookmark::class, Highlight::class, Note::class],
    version = AppConst.DB_VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prayerLogDao(): PrayerLogDao
    abstract fun scheduledNotificationDao(): ScheduledNotificationDao
    abstract fun studioCreationDao(): StudioCreationDao
    abstract fun bookmarksDao(): BookmarksDao
    abstract fun highlightsDao(): HighlightsDao
    abstract fun notesDao(): NotesDao
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
