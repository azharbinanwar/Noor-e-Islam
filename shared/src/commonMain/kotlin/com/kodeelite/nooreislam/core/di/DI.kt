package com.kodeelite.nooreislam.core.di

import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.feature.quran.data.BookmarksStore
import com.kodeelite.nooreislam.feature.quran.data.CollectionStore
import com.kodeelite.nooreislam.feature.quran.data.HighlightsStore
import com.kodeelite.nooreislam.feature.quran.data.NotesStore
import com.kodeelite.nooreislam.feature.notifications.store.SurahReminderStore
import com.kodeelite.nooreislam.feature.quran.data.QuranStore
import com.kodeelite.nooreislam.feature.tracker.data.TrackerStore
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
    single { SurahReminderStore(get(), get(), get()) }
    single { TrackerStore(get(), get()) }
}

val appModules = listOf(appModule, databaseModule, platformDatabaseModule())

/** Start Koin once per platform entry point. [edition] is injectable wherever UI needs to branch on it. */
fun initKoin(edition: AppEdition = AppEdition.MAIN) {
    startKoin {
        modules(appModules + module { single { edition } })
    }
}

/** Swift-friendly entry point — call from iOSApp.init(). */
fun startKoinForIos(edition: AppEdition = AppEdition.MAIN) = initKoin(edition)
