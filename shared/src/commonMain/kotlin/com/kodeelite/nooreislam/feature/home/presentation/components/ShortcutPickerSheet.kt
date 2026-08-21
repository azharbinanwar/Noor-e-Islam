package com.kodeelite.nooreislam.feature.home.presentation.components

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
import com.composables.icons.lucide.Search
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.BuildType
import com.kodeelite.nooreislam.core.catalog.AppFeature
import com.kodeelite.nooreislam.core.catalog.Surface
import com.kodeelite.nooreislam.core.catalog.featuresOn
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppTextField
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.nothing_matches
import com.kodeelite.nooreislam.resources.pick_a_shortcut
import com.kodeelite.nooreislam.resources.search
import com.kodeelite.nooreislam.resources.try_a_feature_name_like_qibla_or_language
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Held a home shortcut — pick what that slot should open instead. */
@Composable
fun ShortcutPickerSheet(current: AppRoute, pinned: List<AppRoute>, onPick: (AppRoute) -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    val edition = koinInject<AppEdition>()
    val debug = koinInject<BuildType>().isDebug
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
        title = stringResource(Res.string.pick_a_shortcut),
        fillHeight = true,
        header = {
            AppTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = stringResource(Res.string.search),
                leading = { Icon(Lucide.Search, null, tint = c.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.padding(bottom = 12.dp),
            )
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
        AppTileGroup(
            items = shown.map { (feature, name) ->
                val taken = feature.target in pinned && feature.target != current
                AppTileItem(
                    title = name,
                    leadingIcon = feature.icon,
                    selected = feature.target == current,
                    enabled = !taken,
                    trailing = if (!taken) null else {
                        { Icon(Lucide.Check, null, tint = c.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
                    },
                    onClick = { onPick(feature.target); onDismiss() },
                )
            },
        )
    }
}
