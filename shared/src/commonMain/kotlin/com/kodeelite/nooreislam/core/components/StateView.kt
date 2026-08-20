package com.kodeelite.nooreislam.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kodeelite.nooreislam.config.theme.AppTheme

/**
 * One view for any "nothing to show" state: empty, no-results, or error.
 *   StateView(title = "No prayers logged", message = "Start tracking today", action = { AppButton(...) })
 *   StateView.Loading()
 *   StateView.Loading(title = "Loading bookmarks…")
 */
object StateView {
    @Composable
    operator fun invoke(
        title: String,
        modifier: Modifier = Modifier,
        message: String? = null,
        icon: (@Composable () -> Unit)? = null,
        action: (@Composable () -> Unit)? = null,
        padding: Dp = 24.dp,   // a sheet already pads its content — pass 0.dp there
    ) {
        Column(
            modifier = modifier.fillMaxWidth().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                icon()
                Spacer(Modifier.height(16.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.colors.onSurface,
                textAlign = TextAlign.Center,
            )
            if (message != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            if (action != null) {
                Spacer(Modifier.height(20.dp))
                action()
            }
        }
    }

    @Composable
    fun Loading(title: String? = null, modifier: Modifier = Modifier) {
        Column(
            modifier = modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(color = AppTheme.colors.primary)
            if (title != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.colors.onSurface,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
