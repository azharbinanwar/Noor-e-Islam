package com.kodeelite.nooreislam.core

import androidx.compose.runtime.Composable
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.app_name
import com.kodeelite.nooreislam.resources.app_name_quran
import org.jetbrains.compose.resources.stringResource

// which app this build is — set once at startup (see initKoin), injected via Koin wherever UI needs
// to branch on it (drawer vs no drawer, which settings sections show, landing screen, etc.)
enum class
AppEdition {
    MAIN,
    QURAN,
}

@Composable
fun AppEdition.displayName(): String = stringResource(
    when (this) {
        AppEdition.MAIN -> Res.string.app_name
        AppEdition.QURAN -> Res.string.app_name_quran
    }
)

// plain, non-localized — for filesystem paths (e.g. the Studio gallery save folder) where a
// Composable-only stringResource() isn't reachable. User-facing text should use displayName() instead.
val AppEdition.folderName: String
    get() = when (this) {
        AppEdition.MAIN -> "Noor e Islam"
        AppEdition.QURAN -> "Noor e Quran"
    }
