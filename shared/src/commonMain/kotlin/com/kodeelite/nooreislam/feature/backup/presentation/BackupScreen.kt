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
import androidx.compose.runtime.LaunchedEffect
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
import com.composables.icons.lucide.CloudOff
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.RefreshCw
import com.composables.icons.lucide.CloudUpload
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.CloudDownload
import com.composables.icons.lucide.UserRound
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.backup.BackupScheduler
import com.kodeelite.nooreislam.core.backup.rememberGoogleSignIn
import com.kodeelite.nooreislam.feature.backup.data.BackupRepository
import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.AppTileVariant
import com.kodeelite.nooreislam.core.components.LocalNotice
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.datetime.labelRes
import com.kodeelite.nooreislam.core.enums.BackupFrequency
import com.kodeelite.nooreislam.core.locale.tr
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.core.store.BackupStore
import com.kodeelite.nooreislam.core.util.asFileSize
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.actions
import com.kodeelite.nooreislam.resources.auto_backup
import com.kodeelite.nooreislam.resources.backup
import com.kodeelite.nooreislam.resources.back
import com.kodeelite.nooreislam.resources.back_up_now
import com.kodeelite.nooreislam.resources.backup_account_hint
import com.kodeelite.nooreislam.resources.backup_connect_failed
import com.kodeelite.nooreislam.resources.backup_deleted
import com.kodeelite.nooreislam.resources.backup_failed
import com.kodeelite.nooreislam.resources.backup_no_drive_access
import com.kodeelite.nooreislam.resources.backup_no_drive_access_sub
import com.kodeelite.nooreislam.resources.backup_nothing_to_restore
import com.kodeelite.nooreislam.resources.backup_offline
import com.kodeelite.nooreislam.resources.backup_offline_sub
import com.kodeelite.nooreislam.resources.backup_nothing_to_restore_sub
import com.kodeelite.nooreislam.resources.backup_connect_unfinished
import com.kodeelite.nooreislam.resources.backup_connect_unfinished_sub
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
import org.jetbrains.compose.resources.getString
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
    val repo = koinInject<BackupRepository>()
    val signIn = rememberGoogleSignIn()
    var foundRemote by remember { mutableStateOf<BackupStore.RemoteBackup?>(null) }
    var offerFirstBackup by remember { mutableStateOf(false) }
    var confirmingDelete by remember { mutableStateOf(false) }
    val notice = LocalNotice.current
    suspend fun report(outcome: BackupRepository.Outcome, deleted: Boolean = false) {
        when (outcome) {
            BackupRepository.Outcome.Done -> if (deleted) notice.show(
                title = getString(Res.string.backup_deleted),
                icon = Lucide.Trash2, variant = AppTileVariant.Success,
            )
            BackupRepository.Outcome.NoToken -> notice.show(
                title = getString(Res.string.backup_no_drive_access),
                message = getString(Res.string.backup_no_drive_access_sub),
                icon = Lucide.CloudOff, variant = AppTileVariant.Warning,
            )
            BackupRepository.Outcome.Offline -> notice.show(
                title = getString(Res.string.backup_offline),
                message = getString(Res.string.backup_offline_sub),
                icon = Lucide.CloudOff, variant = AppTileVariant.Warning,
            )
            BackupRepository.Outcome.NothingToRestore -> notice.show(
                title = getString(Res.string.backup_nothing_to_restore),
                message = getString(Res.string.backup_nothing_to_restore_sub),
                icon = Lucide.CloudOff, variant = AppTileVariant.Warning,
            )
            is BackupRepository.Outcome.Failed -> notice.show(
                title = getString(Res.string.backup_failed),
                message = outcome.reason.ifBlank { null },
                icon = Lucide.CloudOff, variant = AppTileVariant.Error,
            )
        }
    }
    fun connect() = scope.launch {
        when (val result = repo.connect(signIn)) {
            BackupRepository.Connect.Connected -> {
                val found = repo.checkRemote(signIn)
                if (found == null) offerFirstBackup = true
                else if (BackupStore.lastAt.value == null) foundRemote = found
            }
            // Google reports its own failures as a cancel too, so a cancel is never silent
            BackupRepository.Connect.Cancelled -> notice.show(
                title = getString(Res.string.backup_connect_unfinished),
                message = getString(Res.string.backup_connect_unfinished_sub),
                icon = Lucide.CloudOff,
                variant = AppTileVariant.Warning,
            )
            is BackupRepository.Connect.Failed -> notice.show(
                title = getString(Res.string.backup_connect_failed),
                message = result.reason.ifBlank { null },
                icon = Lucide.CloudOff,
                variant = AppTileVariant.Error,
            )
        }
    }
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
    val remote by BackupStore.remote.collectAsState()
    val idle = busy == BackupStore.Busy.Idle
    LaunchedEffect(Unit) { if (account != null && remote == null) repo.checkRemote(signIn) }

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
                        leadingIcon = if (account?.photoUrl == null) Lucide.UserRound else null,
                        leading = account?.photoUrl?.let { url ->
                            { AsyncImage(url, null, Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop) }
                        },
                        title = account?.name ?: account?.email ?: stringResource(Res.string.choose_account),
                        subtitle = account?.email ?: stringResource(Res.string.backup_account_hint),
                        trailing = if (busy == BackupStore.Busy.Connecting || busy == BackupStore.Busy.Deleting) {
                            { CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) }
                        } else null,
                        onClick = { if (idle) pickingAccount = true },
                    )
                ),
            )

            if (account != null) {
                AppTileGroup(
                    title = stringResource(Res.string.actions),
                    items = listOf(
                        AppTileItem(
                            leadingIcon = Lucide.CloudUpload,
                            title = stringResource(Res.string.back_up_now),
                                trailing = (busy as? BackupStore.Busy.BackingUp)?.let { b ->
                                { CircularProgressIndicator(progress = { b.progress }, modifier = Modifier.size(20.dp), strokeWidth = 2.dp) }
                            },
                            // in-app when the consent may still be needed; otherwise the worker, so leaving the screen cannot cut it short
                            onClick = { if (idle) scope.launch { if (remote == null && lastAt == null) report(repo.backUpNow(signIn)) else BackupScheduler.runNow() } },
                        ),
                        AppTileItem(
                            leadingIcon = Lucide.CloudDownload,
                            title = stringResource(Res.string.backup_restore_from_drive),
                            subtitle = stringResource(Res.string.backup_restore_hint),
                            enabled = lastAt != null || remote != null,
                            trailing = (busy as? BackupStore.Busy.Restoring)?.let { r ->
                                { CircularProgressIndicator(progress = { r.progress }, modifier = Modifier.size(20.dp), strokeWidth = 2.dp) }
                            },
                            onClick = { if (idle) confirmingRestore = true },
                        ),
                    ),
                )
                AppTileGroup(
                    title = stringResource(Res.string.backup),
                    items = listOf(
                        AppTileItem(
                            leadingIcon = Lucide.Clock,
                            title = stringResource(Res.string.last_backup),
                            subtitle = lastAt?.let { stringResource(Res.string.backup_last_summary, Now.formattedDateTime(it), lastSizeKb.asFileSize()) }
                                ?: remote?.let { stringResource(Res.string.backup_last_summary, Now.formattedDateTime(it.atMillis), it.sizeKb.asFileSize()) }
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
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (pickingAccount) {
        val current = account
        if (current == null) BackupConnectSheet(
            quran = quran,
            onConnect = { pickingAccount = false; connect() },
            onDismiss = { pickingAccount = false },
        ) else BackupAccountSheet(
            account = current.email,
            hasBackup = lastAt != null || remote != null,
            onDelete = { pickingAccount = false; confirmingDelete = true },
            onSwitch = { pickingAccount = false; connect() },
            onDisconnect = { pickingAccount = false; scope.launch { repo.disconnect(signIn) } },
            onDismiss = { pickingAccount = false },
        )
    }

    if (pickingFrequency) BackupFrequencySheet(onDismiss = { pickingFrequency = false })

    if (pickingNetwork) BackupNetworkSheet(
        current = network,
        onSelect = { BackupStore.setNetwork(it); pickingNetwork = false },
        onDismiss = { pickingNetwork = false },
    )

    if (confirmingDelete) account?.let { acct ->
        DeleteBackupSheet(
            account = acct.email,
            onDelete = { confirmingDelete = false; scope.launch { report(repo.deleteRemote(signIn), deleted = true) } },
            onDismiss = { confirmingDelete = false },
        )
    }

    if (offerFirstBackup) NoBackupSheet(
        onBackUp = { offerFirstBackup = false; scope.launch { report(repo.backUpNow(signIn)) } },
        onDismiss = { offerFirstBackup = false },
    )

    foundRemote?.let { found ->
        BackupFoundSheet(
            date = Now.formattedDateTime(found.atMillis),
            size = found.sizeKb.asFileSize(),
            onRestore = { foundRemote = null; scope.launch { report(repo.restore(signIn)) } },
            onDismiss = { foundRemote = null },
        )
    }

    if (confirmingRestore) RestoreConfirmSheet(
        quran = quran,
        backupDate = (lastAt ?: remote?.atMillis)?.let(Now::formattedDateTime) ?: "",
        onRestore = { confirmingRestore = false; scope.launch { report(repo.restore(signIn)) } },
        onDismiss = { confirmingRestore = false },
    )
}

