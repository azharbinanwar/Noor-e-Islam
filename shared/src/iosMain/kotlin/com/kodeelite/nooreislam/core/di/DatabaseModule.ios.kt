package com.kodeelite.nooreislam.core.di

import androidx.room.RoomDatabase
import com.kodeelite.nooreislam.core.database.AppDatabase
import com.kodeelite.nooreislam.core.database.getDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformDatabaseModule(): Module = module {
    single<RoomDatabase.Builder<AppDatabase>> { getDatabaseBuilder() }
}
