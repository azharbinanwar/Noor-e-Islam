package com.kodeelite.nooreislam

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.config.theme.ThemeMode
import com.kodeelite.nooreislam.core.locale.LocalAppLocale
import com.kodeelite.nooreislam.core.navigation.AppNavHost
import com.kodeelite.nooreislam.core.store.SettingsStore

@Composable
@Preview
fun App() {
    val systemDark = isSystemInDarkTheme()
    // the saved theme, else the system; System theme's dark == null falls through
    val dark = SettingsStore.theme.collectAsState().value.dark ?: systemDark
    val language = SettingsStore.language.collectAsState().value

    // re-point Compose Resources at the chosen language; key() forces a re-render on switch, RTL comes off the enum
    CompositionLocalProvider(
        LocalAppLocale provides language.code,
        LocalLayoutDirection provides language.direction,
    ) {
        key(language.code) {
            AppTheme(themeMode = if (dark) ThemeMode.DARK else ThemeMode.LIGHT) {
                Box(Modifier.fillMaxSize().background(AppTheme.colors.background)) {
                    AppNavHost()
                }
            }
        }
    }
}
