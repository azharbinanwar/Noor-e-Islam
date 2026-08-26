package com.kodeelite.nooreislam.core.backup

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.tasks.await
import org.koin.compose.koinInject

/** The only scope the app asks for: its own hidden folder on Drive. */
internal const val DRIVE_APPDATA = "https://www.googleapis.com/auth/drive.appdata"

private fun log(msg: String) = android.util.Log.i("NoorBackup", msg)

@Composable
actual fun rememberGoogleSignIn(): GoogleSignIn {
    val context = LocalContext.current
    val config = koinInject<GoogleSignInConfig>()
    // the consent screen for the Drive scope comes back through an activity result; the waiting request
    // lives in a remembered holder so a recomposition in between cannot lose it
    val pending = remember { arrayOfNulls<CompletableDeferred<AuthorizationResult?>>(1) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        pending[0]?.complete(
            if (result.resultCode == Activity.RESULT_OK && result.data != null)
                runCatching { Identity.getAuthorizationClient(context).getAuthorizationResultFromIntent(result.data) }.getOrNull()
            else null,
        )
    }
    return remember(context, config) {
        object : GoogleSignIn {
            override val available = config.webClientId.isNotBlank()

            override suspend fun connect(): GoogleAccount? {
                if (!available) return null
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(GetSignInWithGoogleOption.Builder(config.webClientId).build())
                    .build()
                val credential = try {
                    CredentialManager.create(context).getCredential(context, request).credential
                } catch (e: GetCredentialCancellationException) {
                    android.util.Log.i("NoorBackup", "sign-in cancelled")
                    return null
                } catch (e: GetCredentialException) {
                    // NoCredentialException here usually means the console, not the phone: wrong SHA-1, or consent screen in Testing
                    android.util.Log.w("NoorBackup", "sign-in failed: ${e.type} ${e.message}")
                    throw GoogleSignInException(e.message ?: e.type)
                }
                if (credential !is CustomCredential || credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) return null
                val google = GoogleIdTokenCredential.createFrom(credential.data)
                return GoogleAccount(email = google.id, name = google.displayName, photoUrl = google.profilePictureUri?.toString())
            }

            override suspend fun driveToken(): String? {
                if (!available) return null
                val client = Identity.getAuthorizationClient(context)
                val request = AuthorizationRequest.builder().setRequestedScopes(listOf(Scope(DRIVE_APPDATA))).build()
                val first = runCatching { client.authorize(request).await() }
                    .onFailure { log("authorize failed: ${it.message}") }.getOrNull() ?: return null
                if (!first.hasResolution()) { log("drive token granted"); return first.accessToken }
                log("drive consent needed")
                val wait = CompletableDeferred<AuthorizationResult?>().also { pending[0] = it }
                launcher.launch(androidx.activity.result.IntentSenderRequest.Builder(first.pendingIntent!!.intentSender).build())
                // a consent page that never reports back (blocked account, killed activity) must not spin forever
                val result = withTimeoutOrNull(3 * 60_000L) { wait.await() }
                pending[0] = null
                if (result == null) log("drive consent gave no result")
                return result?.accessToken
            }

            override suspend fun disconnect() {
                runCatching { CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest()) }
            }
        }
    }
}
