package com.kodeelite.nooreislam.feature.tracker.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppButtonVariant
import com.kodeelite.nooreislam.core.components.AppChip
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.datetime.formatted
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.miqat.domain.currentPrayer
import com.kodeelite.nooreislam.feature.miqat.store.MiqatTimesStore
import com.kodeelite.nooreislam.feature.tracker.data.ExemptionPeriod
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.cancel
import com.kodeelite.nooreislam.resources.date_stays_exempt
import com.kodeelite.nooreislam.resources.days_stay_exempt_from
import com.kodeelite.nooreislam.resources.resume_from
import com.kodeelite.nooreislam.resources.resume_prayers
import com.kodeelite.nooreislam.resources.today_stays_exempt_up_to_this_prayer
import kotlinx.datetime.daysUntil
import org.jetbrains.compose.resources.stringResource

/**
 * Closing an exemption early. It says what the days already spent become, then asks the one thing
 * it cannot work out: which prayer today is owed again. A prayer already past cannot be resumed
 * from, so only what remains of the day is offered.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExemptionEndSheet(
    period: ExemptionPeriod,
    askPrayer: Boolean,
    onEnd: (resumeFrom: Miqat?) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = AppTheme.colors
    val today = Now.date()
    val todayTimes by MiqatTimesStore.today.collectAsState()
    val dateFormat by SettingsStore.gregorianDateFormat.collectAsState()
    val current = todayTimes.currentPrayer(Now.time())
    // what is left of today, since a prayer whose time has gone cannot be resumed from
    val remaining = Miqat.PRAYERS.filter { current == null || it.ordinal >= current.ordinal }
    var from by remember { mutableStateOf(current) }

    val kept = period.startDate.daysUntil(today) // days before today, all of them exempt

    AppBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(Res.string.resume_prayers),
        footer = {
            AppButton(
                stringResource(Res.string.resume_prayers),
                onClick = { onEnd(if (askPrayer) from else null); onDismiss() },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            AppButton(
                stringResource(Res.string.cancel),
                onClick = onDismiss,
                variant = AppButtonVariant.Text,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Text(
            when {
                kept <= 0 -> stringResource(Res.string.today_stays_exempt_up_to_this_prayer)
                kept == 1 -> stringResource(Res.string.date_stays_exempt, period.startDate.formatted(dateFormat))
                else -> stringResource(Res.string.days_stay_exempt_from, kept, period.startDate.formatted(dateFormat))
            },
            fontSize = 13.sp,
            color = c.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 16.dp),
        )

        if (!askPrayer) return@AppBottomSheet
        Text(
            stringResource(Res.string.resume_from),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = c.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            remaining.forEach { p ->
                AppChip(p.label(), selected = from == p, onClick = { from = p }, icon = p.icon)
            }
        }
    }
}
