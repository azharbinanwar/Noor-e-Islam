package com.kodeelite.nooreislam.core.catalog

import androidx.compose.ui.graphics.vector.ImageVector
import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.navigation.AppRoute
import org.jetbrains.compose.resources.StringResource

/** Where a feature can appear. A feature with none is reachable, but nothing lists it. */
enum class Surface { Drawer, Settings, Home }

/** One place a user can be taken. Read by the drawer, settings search and the home pin picker. */
data class AppFeature(
    val name: StringResource,
    val icon: ImageVector,
    val route: AppRoute,
    // a row inside [route] rather than the screen itself — the screen scrolls to it and opens it
    val anchor: String? = null,
    val editions: Set<AppEdition> = AppEdition.entries.toSet(),
    val surfaces: Set<Surface> = emptySet(),
    // words the name doesn't say but people type, in any language the app ships
    val keywords: List<String> = emptyList(),
    val available: () -> Boolean = { true },
) {
    /** Where tapping this goes — a settings row carries its anchor into the route. */
    val target: AppRoute
        get() = if (route is AppRoute.Settings && anchor != null) AppRoute.Settings(anchor) else route

    fun isIn(edition: AppEdition): Boolean = edition in editions

    fun offeredIn(edition: AppEdition): Boolean = isIn(edition) && available()

    fun shownOn(surface: Surface, edition: AppEdition): Boolean =
        surface in surfaces && offeredIn(edition)
}
