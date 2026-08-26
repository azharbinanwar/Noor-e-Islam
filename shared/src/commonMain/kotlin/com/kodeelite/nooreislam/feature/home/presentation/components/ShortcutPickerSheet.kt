package com.kodeelite.nooreislam.feature.home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Search
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.BuildType
import com.kodeelite.nooreislam.core.catalog.AppFeature
import com.kodeelite.nooreislam.core.catalog.Surface
import com.kodeelite.nooreislam.core.catalog.featuresOn
import com.kodeelite.nooreislam.core.components.ActionWidth
import com.kodeelite.nooreislam.core.components.AppActionGroup
import com.kodeelite.nooreislam.core.components.AppActionItem
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppButtonVariant
import com.kodeelite.nooreislam.core.components.AppTextField
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.core.store.HomeShortcutStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.add
import com.kodeelite.nooreislam.resources.cancel
import com.kodeelite.nooreislam.resources.done
import com.kodeelite.nooreislam.resources.nothing_matches
import com.kodeelite.nooreislam.resources.home_shortcuts
import com.kodeelite.nooreislam.resources.home_shortcuts_hint
import com.kodeelite.nooreislam.resources.search
import com.kodeelite.nooreislam.resources.try_a_feature_name_like_qibla_or_language
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * Held a home shortcut — the pinned bar sits on top, every feature below. Tap a bar item to drop it,
 * a list item to toggle it; empty slots show as Add, and the list dims once the bar is full.
 * Edits stay in the sheet until Done; Cancel or a swipe away leaves home as it was.
 */
@Composable
fun ShortcutPickerSheet(onDismiss: () -> Unit) {
    val c = AppTheme.colors
    val edition = koinInject<AppEdition>()
    val debug = koinInject<BuildType>().isDebug
    // a shortcut this build cannot offer (a dev-only screen pinned in debug) must not hold a slot here
    val offered = remember(edition, debug) { featuresOn(Surface.Home, edition, debug).map { it.target }.toSet() }
    var pinned by remember { mutableStateOf(HomeShortcutStore.pinned.value.filter { it in offered }) }
    var query by remember { mutableStateOf("") }

    val shown by produceState(emptyList<Pair<AppFeature, String>>(), query, edition, debug) {
        val needle = query.trim()
        val all = featuresOn(Surface.Home, edition, debug).map { it to getString(it.name) }
        value = if (needle.isBlank()) all else all.filter { (feature, name) ->
            name.contains(needle, ignoreCase = true) ||
                feature.keywords.any { it.contains(needle, ignoreCase = true) }
        }
    }

    AppBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(Res.string.home_shortcuts),
        subtitle = stringResource(Res.string.home_shortcuts_hint, HomeShortcutStore.MIN, HomeShortcutStore.MAX),
        fillHeight = true,
        header = {
            val all = featuresOn(Surface.Home, edition, debug)
            val bar = pinned.mapNotNull { route -> all.firstOrNull { it.target == route } }.map { feature ->
                AppActionItem(
                    label = stringResource(feature.name),
                    icon = feature.icon,
                    selected = true,
                    onClick = { if (pinned.size > HomeShortcutStore.MIN) pinned = pinned - feature.target },
                )
            }
            // empty slots stay visible so the bar reads as "room for more"
            val empty = List(HomeShortcutStore.MAX - bar.size) {
                AppActionItem(label = stringResource(Res.string.add), icon = Lucide.Plus, onClick = {})
            }
            AppActionGroup(
                items = bar + empty,
                width = ActionWidth.Fill,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
            )
            AppTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = stringResource(Res.string.search),
                leading = { Icon(Lucide.Search, null, tint = c.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.padding(bottom = 12.dp),
            )
        },
        footer = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppButton(text = stringResource(Res.string.cancel), onClick = onDismiss, variant = AppButtonVariant.Outline, modifier = Modifier.weight(1f))
                AppButton(text = stringResource(Res.string.done), onClick = { HomeShortcutStore.set(pinned); onDismiss() }, modifier = Modifier.weight(1f))
            }
        },
    ) {
        if (shown.isEmpty()) {
            StateView(
                title = stringResource(Res.string.nothing_matches, query.trim()),
                message = stringResource(Res.string.try_a_feature_name_like_qibla_or_language),
                modifier = Modifier.padding(top = 32.dp),
            )
            return@AppBottomSheet
        }
        val full = pinned.size >= HomeShortcutStore.MAX
        AppTileGroup(
            items = shown.map { (feature, name) ->
                val taken = feature.target in pinned
                AppTileItem(
                    title = name,
                    leadingIcon = feature.icon,
                    selected = taken,
                    enabled = if (taken) pinned.size > HomeShortcutStore.MIN else !full,
                    trailing = if (!taken) null else {
                        { Icon(Lucide.Check, null, tint = c.primary, modifier = Modifier.size(18.dp)) }
                    },
                    onClick = { pinned = if (taken) pinned - feature.target else pinned + feature.target },
                )
            },
        )
    }
}
