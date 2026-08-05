package com.kodeelite.nooreislam.feature.quran.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.config.theme.LocalAppColors
import com.kodeelite.nooreislam.config.theme.LocalBaseAppColors
import com.kodeelite.nooreislam.feature.quran.data.QuranStore
import com.kodeelite.nooreislam.feature.quran.data.QuranTheme
import com.kodeelite.nooreislam.feature.quran.data.applyTo

import org.koin.compose.koinInject

// wraps the Quran index + reader in the picked reading theme: recolors the whole subtree, app stays
// untouched. Also republishes the true (pre-override) app colors via LocalBaseAppColors, so nested UI —
// e.g. the "System" preview chip in QuranThemePickerSheet — can still show the real app look.
@Composable
fun QuranThemeHost(content: @Composable () -> Unit) {
    val store = koinInject<QuranStore>()
    val theme by store.theme.collectAsState()
    val base = AppTheme.colors
    if (theme == QuranTheme.System) {
        CompositionLocalProvider(LocalBaseAppColors provides base, content = content)
        return
    } // follow the app theme — no override
    val colors = theme.applyTo(base)
    // Material components (Scaffold, TopAppBar) read colorScheme, so mirror the same overrides there
    val scheme = MaterialTheme.colorScheme.copy(
        background = theme.background, onBackground = theme.onBackground,
        surface = theme.surface, onSurface = theme.onSurface,
        surfaceVariant = theme.surfaceContainer, onSurfaceVariant = theme.onSurfaceVariant,
        surfaceContainerLowest = theme.background, surfaceContainerLow = theme.surface,
        surfaceContainer = theme.surfaceContainer, surfaceContainerHigh = theme.surfaceContainer, surfaceContainerHighest = theme.surfaceContainer,
        outline = theme.outline, outlineVariant = theme.outline, surfaceTint = theme.primary,
        primary = theme.primary, onPrimary = theme.onPrimary,
    )
    CompositionLocalProvider(LocalAppColors provides colors, LocalBaseAppColors provides base) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
