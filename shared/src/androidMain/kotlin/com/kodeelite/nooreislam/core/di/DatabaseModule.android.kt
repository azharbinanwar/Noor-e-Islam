package com.kodeelite.nooreislam.core.di

import androidx.room.RoomDatabase
import com.kodeelite.nooreislam.core.database.AppDatabase
import com.kodeelite.nooreislam.core.database.getDatabaseBuilder
import com.kodeelite.nooreislam.core.platform.AppCtx
import org.koin.core.module.Module
import org.koin.dsl.module

// AppCtx.context is set in NoorApplication before the DB is ever touched (singles are lazy).
actual fun platformDatabaseModule(): Module = module {
    single<RoomDatabase.Builder<AppDatabase>> { getDatabaseBuilder(AppCtx.context) }
}
