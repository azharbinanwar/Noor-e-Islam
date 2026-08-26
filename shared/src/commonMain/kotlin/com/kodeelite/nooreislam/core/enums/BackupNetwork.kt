package com.kodeelite.nooreislam.core.enums

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ArrowDownUp
import com.composables.icons.lucide.Wifi
import com.kodeelite.nooreislam.core.constants.defaults.BackupDefaults
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.backup_over_any
import com.kodeelite.nooreislam.resources.backup_over_wifi
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Which connection the automatic backup may use. Named choices, so "off" can never read as "no backup". */
enum class BackupNetwork(private val labelRes: StringResource, val icon: ImageVector) {
    Any(Res.string.backup_over_any, Lucide.ArrowDownUp),
    WifiOnly(Res.string.backup_over_wifi, Lucide.Wifi),
    ;

    @Composable
    fun label(): String = stringResource(labelRes)

    companion object {
        fun from(value: String?): BackupNetwork = entries.firstOrNull { it.name == value } ?: BackupDefaults.NETWORK
    }
}
