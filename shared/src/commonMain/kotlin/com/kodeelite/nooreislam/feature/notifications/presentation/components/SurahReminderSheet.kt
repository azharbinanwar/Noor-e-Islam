package com.kodeelite.nooreislam.feature.notifications.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.composables.icons.lucide.Hash
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppButtonVariant
import com.kodeelite.nooreislam.core.components.AppTextField
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import org.jetbrains.compose.resources.stringArrayResource
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.enums.TimeFormat
import com.kodeelite.nooreislam.core.locale.tr
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.notifications.data.EVERY_DAY
import com.kodeelite.nooreislam.feature.notifications.data.SurahReminder
import com.kodeelite.nooreislam.feature.quran.data.QuranRepository
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.week_days
import com.kodeelite.nooreislam.resources.delete
import com.kodeelite.nooreislam.resources.start_at_ayah_1_to_x
import com.kodeelite.nooreislam.resources.days_of_the_week
import com.kodeelite.nooreislam.resources.jump_to_surah_field
import com.kodeelite.nooreislam.resources.reminder
import com.kodeelite.nooreislam.resources.name_this_reminder
import com.kodeelite.nooreislam.resources.reminder_time
import com.kodeelite.nooreislam.resources.save
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource

/**
 * Add or edit one reminder. [existing] null means a new one; the delete action only shows when editing.
 * The ayah is optional: leave it blank and the reminder opens the whole surah.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReminderSheet(
    existing: SurahReminder?,
    onSave: (SurahReminder) -> Unit,
    onDelete: (SurahReminder) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val surahs by produceState(emptyList()) { value = QuranRepository.surahs() }

    var surah by remember { mutableStateOf(existing?.surah ?: 67) }
    // null = the whole surah, opened from the start — nothing is forced on the user
    var ayah by remember { mutableStateOf(existing?.ayah) }
    var title by remember { mutableStateOf(existing?.title.orEmpty()) }
    var days by remember { mutableStateOf(existing?.days ?: EVERY_DAY) }
    var pickerOpen by remember { mutableStateOf(false) }
    var timePickerOpen by remember { mutableStateOf(false) }

    val time = rememberTimePickerState(
        initialHour = existing?.hour ?: 22,
        initialMinute = existing?.minute ?: 30,
        is24Hour = timeFormat == TimeFormat.TwentyFour,
    )

    val picked = surahs.firstOrNull { it.number == surah }
    val surahName = picked?.let { tr(it.nameTransliterated, it.nameArabic) } ?: surah.toString()

    AppBottomSheet(onDismiss = onDismiss, title = stringResource(Res.string.reminder)) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            AppTileGroup(
                items = listOf(
                    AppTileItem(
                        title = stringResource(Res.string.jump_to_surah_field),
                        subtitle = surahName,
                        onClick = { pickerOpen = true },
                    ),
                ),
            )

            // optional: blank opens the surah from the start. Digits only, and never past the
            // surah's last ayah, so a reminder can't point at an ayah that doesn't exist.
            AppTextField(
                value = ayah?.toString().orEmpty(),
                onValueChange = { v ->
                    ayah = v.filter { it.isDigit() }.toIntOrNull()?.coerceAtMost(picked?.ayahCount ?: Int.MAX_VALUE)
                },
                placeholder = stringResource(Res.string.start_at_ayah_1_to_x, picked?.ayahCount ?: 1),
                leading = { Icon(Lucide.Hash, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth(),
            )

            AppTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = stringResource(Res.string.name_this_reminder),
                leading = { Icon(Lucide.Pencil, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(Res.string.days_of_the_week),
                    color = colors.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
                val dayLabels = stringArrayResource(Res.array.week_days) // Monday-first, same as DayOfWeek.ordinal
                // fixed 40dp circles, not chips — a pill sized to its letter makes M wide and I narrow,
                // so a row of seven never lines up
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DayOfWeek.entries.forEach { d ->
                        val on = days and (1 shl d.ordinal) != 0
                        Box(
                            Modifier.size(40.dp)
                                .clip(CircleShape)
                                .background(if (on) colors.primary.copy(alpha = 0.14f) else colors.surfaceContainerHigh)
                                .clickable {
                                    val next = days xor (1 shl d.ordinal)
                                    if (next != 0) days = next // never leave a reminder with no day
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                dayLabels[d.ordinal],
                                color = if (on) colors.primary else colors.onSurfaceVariant,
                                fontSize = 13.sp,
                                fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }

            // a row, not an inline dial: the dial swallows drags that belong to the sheet's scroll
            AppTileGroup(
                items = listOf(
                    AppTileItem(
                        title = stringResource(Res.string.reminder_time),
                        subtitle = Now.formattedTime(LocalTime(time.hour, time.minute)),
                        onClick = { timePickerOpen = true },
                    ),
                ),
            )

            AppButton(
                text = stringResource(Res.string.save),
                onClick = {
                    onSave(
                        (existing ?: SurahReminder(surah = surah, days = days, hour = 0, minute = 0, createdAt = 0))
                            .copy(
                                surah = surah,
                                ayah = ayah,
                                title = title.trim(),
                                days = days,
                                hour = time.hour,
                                minute = time.minute,
                                // saving is the user asking for it — no second step to switch it on
                                enabled = true,
                            )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )

            // Seeds stay put — switch them off instead, so they can always be found again.
            existing?.takeUnless { it.isSeed }?.let {
                AppButton(
                    text = stringResource(Res.string.delete),
                    onClick = { onDelete(it) },
                    modifier = Modifier.fillMaxWidth(),
                    variant = AppButtonVariant.Text,
                )
            }
        }
    }

    if (timePickerOpen) AppBottomSheet(onDismiss = { timePickerOpen = false }, title = stringResource(Res.string.reminder_time)) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TimePicker(state = time)
            AppButton(
                text = stringResource(Res.string.save),
                onClick = { timePickerOpen = false },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (pickerOpen) ReminderSurahPickerSheet(
        surahs = surahs,
        onPick = { surah = it.number; pickerOpen = false },
        onDismiss = { pickerOpen = false },
    )
}
