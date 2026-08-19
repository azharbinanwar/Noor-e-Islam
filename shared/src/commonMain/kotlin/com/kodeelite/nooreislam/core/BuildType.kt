package com.kodeelite.nooreislam.core

// which build this is — set once at startup (see initKoin), injected via Koin wherever UI needs
// to hide a dev-only affordance (the sandbox screen, the long-press theme flip). Mirrors [AppEdition].
enum class BuildType {
    DEBUG,
    RELEASE;

    val isDebug: Boolean get() = this == DEBUG
}
