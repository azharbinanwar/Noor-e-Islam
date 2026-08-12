package com.kodeelite.nooreislam.feature.notifications.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppTextField
import com.kodeelite.nooreislam.core.components.StateView
import com.kodeelite.nooreislam.core.components.TilePosition
import com.kodeelite.nooreislam.feature.quran.data.Surah
import com.kodeelite.nooreislam.feature.quran.presentation.components.SurahItem
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.no_surahs_found
import com.kodeelite.nooreislam.resources.search
import com.kodeelite.nooreislam.resources.surahs_label
import com.kodeelite.nooreislam.resources.try_a_different_search
import org.jetbrains.compose.resources.stringResource

/**
 * Picks the surah for a reminder — surah only, no ayah. The reader's own picker has to return an
 * ayah, so reusing it would force one on every reminder; here an ayah is optional and typed
 * separately, and leaving it empty means the whole surah.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSurahPickerSheet(surahs: List<Surah>, onPick: (Surah) -> Unit, onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    var query by remember { mutableStateOf("") }
    val shown = if (query.isBlank()) surahs else surahs.filter {
        it.nameTransliterated.contains(query, ignoreCase = true) ||
                it.nameEnglish.contains(query, ignoreCase = true) ||
                it.nameArabic.contains(query) ||
                it.number.toString() == query.trim()
    }

    AppBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(Res.string.surahs_label),
        fillHeight = true,
        // pinned: the search stays put while the 114 rows scroll under it
        header = {
            AppTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = stringResource(Res.string.search),
                leading = { Icon(Lucide.Search, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        if (shown.isEmpty()) {
            StateView(
                title = stringResource(Res.string.no_surahs_found),
                message = stringResource(Res.string.try_a_different_search),
                modifier = Modifier.padding(top = 32.dp),
            )
        } else {
            Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                shown.forEachIndexed { i, s ->
                    SurahItem(s, TilePosition.at(i, shown.size), onClick = { onPick(s) })
                }
            }
        }
    }
}
