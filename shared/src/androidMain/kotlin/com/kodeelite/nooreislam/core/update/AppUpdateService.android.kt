package com.kodeelite.nooreislam.core.update

import android.content.Intent
import androidx.core.net.toUri
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.kodeelite.nooreislam.core.platform.AppCtx
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// Play only reports updates for builds it installed itself, so this stays quiet on dev and sideloaded
// builds. Testing it means uploading to a track and installing from Play (internal app sharing works).
actual object AppUpdateService {

    private val manager by lazy { AppUpdateManagerFactory.create(AppCtx.context) }

    actual suspend fun isUpdateAvailable(): Boolean = suspendCoroutine { cont ->
        runCatching {
            manager.appUpdateInfo
                .addOnSuccessListener {
                    cont.resume(
                        it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                            it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE),
                    )
                }
                .addOnFailureListener { cont.resume(false) }
        }.onFailure { cont.resume(false) }
    }

    actual fun startUpdate() {
        val activity = AppCtx.activity
        if (activity == null) {
            openPlayListing()
            return
        }
        runCatching {
            manager.appUpdateInfo
                .addOnSuccessListener { info ->
                    runCatching {
                        manager.startUpdateFlow(info, activity, AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build())
                    }.onFailure { openPlayListing() }
                }
                .addOnFailureListener { openPlayListing() }
        }.onFailure { openPlayListing() }
    }

    // Last resort when Play's own flow can't run — the store page still gets the update done.
    private fun openPlayListing() {
        val ctx = AppCtx.context
        runCatching {
            ctx.startActivity(
                Intent(Intent.ACTION_VIEW, "market://details?id=${ctx.packageName}".toUri())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
}
