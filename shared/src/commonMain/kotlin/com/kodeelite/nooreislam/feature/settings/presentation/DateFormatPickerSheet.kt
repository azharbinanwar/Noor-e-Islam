package com.kodeelite.nooreislam.feature.settings.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.datetime.HijriDate
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.datetime.formatted
import com.kodeelite.nooreislam.core.enums.DateFormatStyle
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.date_format
import com.kodeelite.nooreislam.resources.hijri_date_format
import org.jetbrains.compose.resources.stringResource

@Composable
fun GregorianDateFormatPickerSheet(
    current: DateFormatStyle,
    onSelect: (DateFormatStyle) -> Unit,
    onDismiss: () -> Unit,
) {
    val primary = AppTheme.colors.primary
    val today = remember { Now.date() }
    AppBottomSheet(onDismiss = onDismiss, title = stringResource(Res.string.date_format)) {
        AppTileGroup(
            modifier = Modifier.padding(top = 4.dp),
            items = DateFormatStyle.entries.map { format ->
                val selected = format == current
                AppTileItem(
                    title = format.label(),
                    subtitle = today.formatted(format),
                    selected = selected,
                    trailing = if (selected) {
                        { Icon(Lucide.Check, null, tint = primary) }
                    } else null,
                    onClick = { onSelect(format) },
                )
            },
        )
    }
}

@Composable
fun HijriDateFormatPickerSheet(
    current: DateFormatStyle,
    hijriToday: HijriDate,
    onSelect: (DateFormatStyle) -> Unit,
    onDismiss: () -> Unit,
) {
    val primary = AppTheme.colors.primary
    AppBottomSheet(onDismiss = onDismiss, title = stringResource(Res.string.hijri_date_format)) {
        AppTileGroup(
            modifier = Modifier.padding(top = 4.dp),
            items = DateFormatStyle.entries.map { format ->
                val selected = format == current
                AppTileItem(
                    title = format.label(),
                    subtitle = hijriToday.formatted(format),
                    selected = selected,
                    trailing = if (selected) {
                        { Icon(Lucide.Check, null, tint = primary) }
                    } else null,
                    onClick = { onSelect(format) },
                )
            },
        )
    }
}
