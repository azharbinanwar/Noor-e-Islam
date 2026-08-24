package com.kodeelite.nooreislam.core.platform

import coil3.PlatformContext

/** Coil's context for code that has no Composable to read LocalPlatformContext from. */
expect fun imageContext(): PlatformContext
