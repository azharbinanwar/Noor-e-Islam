package com.kodeelite.nooreislam.feature.settings.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.constants.AppLinks
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.credits
import org.jetbrains.compose.resources.stringResource

// one entry per project the app stands on — name, what it gave, where it lives
private data class Credit(val name: String, val what: String, val url: String)

private val CREDITS = listOf(
    Credit("Tanzil Project", "Quran text", AppLinks.TANZIL),
    Credit("QuranWBW — Ayman Siddiqui & R. Siddiqua", "IndoPak text & font · © Al Qalam, Ghandhara, KFGQPC", AppLinks.QURAN_WBW),
)

@Composable
fun CreditsSheet(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    AppBottomSheet(onDismiss = onDismiss, title = stringResource(Res.string.credits)) {
        AppTileGroup(
            items = CREDITS.map { c ->
                AppTileItem(
                    title = c.name,
                    subtitle = c.what,
                    onClick = { uriHandler.openUri(c.url) },
                )
            },
        )
    }
}
