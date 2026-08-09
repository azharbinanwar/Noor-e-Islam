package com.kodeelite.nooreislam.feature.quran.presentation.components

import androidx.compose.runtime.Composable

/** Keeps the display awake while composed and [enabled] is true. Android: FLAG_KEEP_SCREEN_ON. iOS: idle timer disabled. */
@Composable
expect fun KeepScreenOn(enabled: Boolean)
