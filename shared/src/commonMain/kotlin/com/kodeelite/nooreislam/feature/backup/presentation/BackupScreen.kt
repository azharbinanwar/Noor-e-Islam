package com.kodeelite.nooreislam.feature.backup.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.RefreshCw
import com.composables.icons.lucide.CloudUpload
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RotateCcw
import com.composables.icons.lucide.UserRound
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.datetime.labelRes
import com.kodeelite.nooreislam.core.enums.BackupFrequency
import com.kodeelite.nooreislam.core.locale.tr
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.core.store.BackupStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.actions
import com.kodeelite.nooreislam.resources.auto_backup
import com.kodeelite.nooreislam.resources.backup
import com.kodeelite.nooreislam.resources.back
import com.kodeelite.nooreislam.resources.back_up_now
import com.kodeelite.nooreislam.resources.backup_account_hint
import com.kodeelite.nooreislam.resources.backup_includes
import com.kodeelite.nooreislam.resources.backup_includes_quran
import com.kodeelite.nooreislam.resources.backup_last_never
import com.kodeelite.nooreislam.resources.backup_last_summary
import com.kodeelite.nooreislam.resources.backup_daily_at
import com.kodeelite.nooreislam.resources.backup_over
import com.kodeelite.nooreislam.resources.backup_weekly_at
import com.kodeelite.nooreislam.resources.backup_restore_from_drive
import com.kodeelite.nooreislam.resources.backup_restore_hint
import com.kodeelite.nooreislam.resources.choose_account
import com.kodeelite.nooreislam.resources.google_account
import com.kodeelite.nooreislam.resources.google_drive_backup
import com.kodeelite.nooreislam.resources.last_backup
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

// Google Drive backup: pick an account, back up, restore. Just layout; BackupStore owns the state.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen() {
    val nav = LocalAppNavigator.current
    val quran = koinInject<AppEdition>() == AppEdition.QURAN
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    val account by BackupStore.account.collectAsState()
    val lastAt by BackupStore.lastAt.collectAsState()
    val lastSizeKb by BackupStore.lastSizeKb.collectAsState()
    val frequency by BackupStore.frequency.collectAsState()
    val network by BackupStore.network.collectAsState()
    val time by BackupStore.time.collectAsState()
    val weekday by BackupStore.weekday.collectAsState()
    val busy by BackupStore.busy.collectAsState()
    var pickingAccount by remember { mutableStateOf(false) }
    var pickingFrequency by remember { mutableStateOf(false) }
    var pickingNetwork by remember { mutableStateOf(false) }
    var confirmingRestore by remember { mutableStateOf(false) }
    val idle = busy == BackupStore.Busy.Idle

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.google_drive_backup)) },
                navigationIcon = {
                    IconButton(onClick = { nav.back() }) {
                        Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), stringResource(Res.string.back))
                    }
                },
            )
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text(
                stringResource(if (quran) Res.string.backup_includes_quran else Res.string.backup_includes),
                fontSize = 13.sp, color = c.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            )
            Spacer(Modifier.height(12.dp))

            AppTileGroup(
                title = stringResource(Res.string.google_account),
                items = listOf(
                    AppTileItem(
                        leadingIcon = Lucide.UserRound,
                        title = account ?: stringResource(Res.string.choose_account),
                        subtitle = stringResource(Res.string.backup_account_hint),
                        onClick = { if (idle) pickingAccount = true },
                    )
                ),
            )

            if (account != null) {
                AppTileGroup(
                    title = stringResource(Res.string.backup),
                    items = listOf(
                        AppTileItem(
                            leadingIcon = Lucide.Clock,
                            title = stringResource(Res.string.last_backup),
                            subtitle = lastAt?.let { stringResource(Res.string.backup_last_summary, Now.formattedDateTime(it), sizeLabel(lastSizeKb)) }
                                ?: stringResource(Res.string.backup_last_never),
                        ),
                        AppTileItem(
                            leadingIcon = Lucide.RefreshCw,
                            title = stringResource(Res.string.auto_backup),
                            subtitle = when (frequency) {
                                BackupFrequency.Off -> frequency.label()
                                BackupFrequency.Daily -> stringResource(Res.string.backup_daily_at, Now.formattedTime(time))
                                BackupFrequency.Weekly -> stringResource(Res.string.backup_weekly_at, stringResource(weekday.labelRes), Now.formattedTime(time))
                            },
                                onClick = { if (idle) pickingFrequency = true },
                        ),
                        AppTileItem(
                            leadingIcon = network.icon,
                            title = stringResource(Res.string.backup_over),
                            subtitle = network.label(),
                                onClick = { if (idle) pickingNetwork = true },
                        ),
                    ),
                )
                AppTileGroup(
                    title = stringResource(Res.string.actions),
                    items = listOf(
                        AppTileItem(
                            leadingIcon = Lucide.CloudUpload,
                            title = stringResource(Res.string.back_up_now),
                                trailing = (busy as? BackupStore.Busy.BackingUp)?.let { b ->
                                { CircularProgressIndicator(progress = { b.progress }, modifier = Modifier.size(20.dp), strokeWidth = 2.dp) }
                            },
                            onClick = { if (idle) scope.launch { BackupStore.backUpNow() } },
                        ),
                        AppTileItem(
                            leadingIcon = Lucide.RotateCcw,
                            title = stringResource(Res.string.backup_restore_from_drive),
                            subtitle = stringResource(Res.string.backup_restore_hint),
                            enabled = lastAt != null,
                            trailing = (busy as? BackupStore.Busy.Restoring)?.let { r ->
                                { CircularProgressIndicator(progress = { r.progress }, modifier = Modifier.size(20.dp), strokeWidth = 2.dp) }
                            },
                            onClick = { if (idle) confirmingRestore = true },
                        ),
                    ),
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (pickingAccount) {
        val current = account
        // todo: system account picker; the placeholder email stands in until Drive sign-in lands
        if (current == null) BackupConnectSheet(
            quran = quran,
            onConnect = { BackupStore.setAccount("you@gmail.com"); pickingAccount = false },
            onDismiss = { pickingAccount = false },
        ) else BackupAccountSheet(
            account = current,
            onSwitch = { BackupStore.setAccount("other@gmail.com"); pickingAccount = false },
            onDisconnect = { BackupStore.setAccount(null); pickingAccount = false },
            onDismiss = { pickingAccount = false },
        )
    }

    if (pickingFrequency) BackupFrequencySheet(onDismiss = { pickingFrequency = false })

    if (pickingNetwork) BackupNetworkSheet(
        current = network,
        onSelect = { BackupStore.setNetwork(it); pickingNetwork = false },
        onDismiss = { pickingNetwork = false },
    )

    if (confirmingRestore) RestoreConfirmSheet(
        quran = quran,
        backupDate = lastAt?.let(Now::formattedDateTime) ?: "",
        onRestore = { confirmingRestore = false; scope.launch { BackupStore.restore() } },
        onDismiss = { confirmingRestore = false },
    )
}

private fun sizeLabel(kb: Int): String = if (kb < 1000) "$kb KB" else "${(kb / 100).let { "${it / 10}.${it % 10}" }} MB"
