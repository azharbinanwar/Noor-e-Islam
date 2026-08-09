package com.kodeelite.nooreislam.core

// which app this build is — set once at startup (see initKoin), injected via Koin wherever UI needs
// to branch on it (drawer vs no drawer, which settings sections show, landing screen, etc.)
enum class
AppEdition {
    MAIN,
    QURAN,
}
