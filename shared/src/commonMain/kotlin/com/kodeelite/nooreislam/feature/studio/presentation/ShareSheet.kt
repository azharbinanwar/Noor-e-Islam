package com.kodeelite.nooreislam.feature.studio.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppButton
import com.kodeelite.nooreislam.core.components.AppSwitch
import com.kodeelite.nooreislam.core.components.AppTextField
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.add_a_message
import com.kodeelite.nooreislam.resources.include_ayah_text
import com.kodeelite.nooreislam.resources.share
import org.jetbrains.compose.resources.stringResource

/**
 * Review the share caption before sharing: an editable message field, a toggle to include/drop the ayah
 * text (the rest of the message stays), and a Share action. Pressing Share dismisses the sheet, then the
 * caller shows the spinner on the Share button while the image is captured + sent.
 */
@Composable
internal fun ShareSheet(
    ayahText: String,
    otherText: String,   // reference + app line — kept even when the ayah is hidden
    onDismiss: () -> Unit,
    onShare: (caption: String) -> Unit,
) {
    val colors = AppTheme.colors
    var includeAyah by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("$ayahText\n\n$otherText") }

    AppBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(Res.string.share),
        footer = {
            AppButton(stringResource(Res.string.share), onClick = { onShare(message.trim()) }, modifier = Modifier.fillMaxWidth())
        },
    ) {
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.include_ayah_text), color = colors.onSurface, fontSize = 13.sp, modifier = Modifier.weight(1f))
            AppSwitch(includeAyah, { checked ->
                includeAyah = checked
                message = if (checked) "$ayahText\n\n${message.trimStart()}"
                else message.removePrefix(ayahText).trimStart()
            })
        }
        AppTextField(
            value = message,
            onValueChange = { message = it },
            placeholder = stringResource(Res.string.add_a_message),
            singleLine = false,
            modifier = Modifier.heightIn(min = 120.dp),
        )
        Spacer(Modifier.height(8.dp))
    }
}
