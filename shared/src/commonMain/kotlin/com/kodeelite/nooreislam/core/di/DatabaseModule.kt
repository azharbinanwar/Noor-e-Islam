package com.kodeelite.nooreislam.core.di

import com.kodeelite.nooreislam.core.database.AppDatabase
import com.kodeelite.nooreislam.core.database.getRoomDatabase
import com.kodeelite.nooreislam.feature.notifications.data.NotificationScheduleRepository
import com.kodeelite.nooreislam.feature.quran.data.BookmarksRepository
import com.kodeelite.nooreislam.feature.quran.data.CollectionRepository
import com.kodeelite.nooreislam.feature.quran.data.HighlightsRepository
import com.kodeelite.nooreislam.feature.quran.data.NotesRepository
import com.kodeelite.nooreislam.feature.studio.data.StudioCreationRepository
import com.kodeelite.nooreislam.feature.tracker.data.TrackerRepository
import org.koin.core.module.Module
import org.koin.dsl.module

// DB, DAOs, and data repos. The builder comes from the platform module below.
val databaseModule = module {
    single { getRoomDatabase(get()) }
    single { get<AppDatabase>().scheduledNotificationDao() }
    single { NotificationScheduleRepository(get()) }
    single { get<AppDatabase>().studioCreationDao() }
    single { StudioCreationRepository(get()) }
    single { get<AppDatabase>().bookmarksDao() }
    single { BookmarksRepository(get()) }
    single { get<AppDatabase>().highlightsDao() }
    single { HighlightsRepository(get()) }
    single { get<AppDatabase>().notesDao() }
    single { NotesRepository(get()) }
    single { get<AppDatabase>().collectionDao() }
    single { get<AppDatabase>().collectionAyahDao() }
    single { CollectionRepository(get(), get()) }
    single { get<AppDatabase>().surahReminderDao() }
    single { get<AppDatabase>().trackedPrayerDao() }
    single { get<AppDatabase>().excusedPeriodDao() }
    single { TrackerRepository(get(), get()) }
}

// Android needs a Context, iOS a file path.
expect fun platformDatabaseModule(): Module
