package com.kodeelite.nooreislam.feature.settings.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.BuildType
import com.kodeelite.nooreislam.core.catalog.AppFeature
import com.kodeelite.nooreislam.core.catalog.Surface
import com.kodeelite.nooreislam.core.catalog.featuresOn
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.nothing_matches
import com.kodeelite.nooreislam.resources.try_a_feature_name_like_qibla_or_language
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Matches on the name and on keywords, so "madhab" reaches Prayer Calculation. */
@Composable
fun SettingsSearchResults(query: String, onOpen: (AppFeature) -> Unit) {
    val edition = koinInject<AppEdition>()
    val debug = koinInject<BuildType>().isDebug
    val matches by produceState(emptyList<Pair<AppFeature, String>>(), query, edition, debug) {
        val needle = query.trim()
        value = if (needle.isBlank()) emptyList() else featuresOn(Surface.Settings, edition, debug)
            .map { it to getString(it.name) }
            .filter { (feature, name) ->
                name.contains(needle, ignoreCase = true) ||
                    feature.keywords.any { it.contains(needle, ignoreCase = true) }
            }
            .sortedBy { (_, name) -> if (name.startsWith(needle, ignoreCase = true)) 0 else 1 }
    }

    if (matches.isEmpty()) {
        StateView(
            title = stringResource(Res.string.nothing_matches, query.trim()),
            message = stringResource(Res.string.try_a_feature_name_like_qibla_or_language),
        )
        return
    }

    AppTileGroup(
        modifier = Modifier.fillMaxWidth(),
        items = matches.map { (feature, name) ->
            AppTileItem(title = name, leadingIcon = feature.icon, onClick = { onOpen(feature) })
        },
    )
}
