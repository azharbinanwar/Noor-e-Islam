package com.kodeelite.nooreislam.feature.tracker.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.PrayerTrackerStatus
import com.kodeelite.nooreislam.core.enums.color
import com.kodeelite.nooreislam.core.enums.label
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.clear
import com.kodeelite.nooreislam.resources.mark_prayer
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

/** Shared by Home and the tracker so marking a prayer works the same in both. */
@Composable
fun TrackingSheet(
    prayer: Miqat,
    date: LocalDate,
    current: PrayerTrackerStatus?,
    onSelect: (PrayerTrackerStatus?) -> Unit,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(Res.string.mark_prayer, prayer.label(date)),
    ) {
        AppTileGroup(
            items = PrayerTrackerStatus.entries.map { st ->
                AppTileItem(
                    title = st.label,
                    selected = st == current,
                    leadingIcon = st.icon,
                    leadingColor = st.color,
                    onClick = { onSelect(st) },
                )
            }
        )
        if (current != null) {
            Spacer(Modifier.height(10.dp))
            AppTileGroup(
                items = listOf(
                    AppTileItem(
                        title = stringResource(Res.string.clear),
                        leadingIcon = Lucide.Minus,
                        leadingColor = AppTheme.colors.error,
                        onClick = { onSelect(null) },
                    )
                )
            )
        }
    }
}
