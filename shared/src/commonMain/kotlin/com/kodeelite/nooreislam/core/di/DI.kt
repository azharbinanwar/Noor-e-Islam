package com.kodeelite.nooreislam.core.di

import com.kodeelite.nooreislam.feature.quran.data.BookmarksStore
import com.kodeelite.nooreislam.feature.quran.data.CollectionStore
import com.kodeelite.nooreislam.feature.quran.data.HighlightsStore
import com.kodeelite.nooreislam.feature.quran.data.NotesStore
import com.kodeelite.nooreislam.feature.quran.data.QuranStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * All DI modules. Feature modules (repositories, use cases, view models)
 * get added to [appModules] as they're built.
 */
val appModule = module {
    // feature registrations go here as they need DI — the pure engine and the stores don't
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { QuranStore(get()) }
    single { BookmarksStore(get(), get()) }
    single { HighlightsStore(get(), get()) }
    single { NotesStore(get(), get()) }
    single { CollectionStore(get(), get()) }
}

val appModules = listOf(appModule, databaseModule, platformDatabaseModule())

/** Start Koin once per platform entry point. */
fun initKoin() {
    startKoin {
        modules(appModules)
    }
}

/** Swift-friendly entry point — call from iOSApp.init(). */
fun startKoinForIos() = initKoin()
