package com.example.miqatapp.core.di

import com.example.miqatapp.core.database.AppDatabase
import com.example.miqatapp.core.database.getRoomDatabase
import com.example.miqatapp.feature.notifications.data.NotificationScheduleRepository
import com.example.miqatapp.feature.studio.data.StudioCreationRepository
import org.koin.core.module.Module
import org.koin.dsl.module

// DB, DAOs, and data repos. The builder comes from the platform module below.
val databaseModule = module {
    single { getRoomDatabase(get()) }
    single { get<AppDatabase>().scheduledNotificationDao() }
    single { NotificationScheduleRepository(get()) }
    single { get<AppDatabase>().studioCreationDao() }
    single { StudioCreationRepository(get()) }
}

// Android needs a Context, iOS a file path.
expect fun platformDatabaseModule(): Module
