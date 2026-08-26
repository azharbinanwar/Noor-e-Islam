package com.kodeelite.nooreislam.core.enums

import androidx.compose.runtime.Composable
import com.kodeelite.nooreislam.core.constants.defaults.BackupDefaults
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.backup_daily
import com.kodeelite.nooreislam.resources.backup_off
import com.kodeelite.nooreislam.resources.backup_weekly
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** How often the automatic Drive backup runs. */
enum class BackupFrequency(private val labelRes: StringResource) {
    Off(Res.string.backup_off),
    Daily(Res.string.backup_daily),
    Weekly(Res.string.backup_weekly),
    ;

    @Composable
    fun label(): String = stringResource(labelRes)

    companion object {
        fun from(value: String?): BackupFrequency = entries.firstOrNull { it.name == value } ?: BackupDefaults.FREQUENCY
    }
}
