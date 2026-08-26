package com.kodeelite.nooreislam.core.focus

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import com.kodeelite.nooreislam.core.constants.PrefConst
import com.kodeelite.nooreislam.core.platform.AppCtx
import com.kodeelite.nooreislam.core.prefs.PrefsService

// One place for the phone's silence state: save the prior ringer, apply Silent/Vibrate, and put it back.
object Ringer {
    private const val NONE = -1
    private val am get() = AppCtx.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val nm get() = AppCtx.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun hasDndAccess(): Boolean = nm.isNotificationPolicyAccessGranted
    fun hasSaved() = PrefsService.getInt(PrefConst.FOCUS_SAVED_RINGER, NONE) != NONE

    // Mute to the given mode, saving the prior ringer once. Silent needs DND access, else falls back to Vibrate.
    // Returns true if it went fully Silent.
    fun mute(mode: String): Boolean {
        if (PrefsService.getInt(PrefConst.FOCUS_SAVED_RINGER, NONE) == NONE) {
            PrefsService.putInt(PrefConst.FOCUS_SAVED_RINGER, am.ringerMode)
        }
        val silent = mode == SilenceMode.Silent.name && hasDndAccess()
        val target = if (silent) AudioManager.RINGER_MODE_SILENT else AudioManager.RINGER_MODE_VIBRATE
        if (am.ringerMode != target) setMode(target) // already there (extend/heal restart) -> no blip
        return silent
    }

    // Put the ringer back to the saved value (or Normal when forced). Returns true if it changed anything.
    fun restore(forceNormal: Boolean = false): Boolean {
        val saved = PrefsService.getInt(PrefConst.FOCUS_SAVED_RINGER, NONE)
        if (saved == NONE && !forceNormal) return false
        setMode(if (saved != NONE) saved else AudioManager.RINGER_MODE_NORMAL)
        PrefsService.putInt(PrefConst.FOCUS_SAVED_RINGER, NONE)
        return true
    }

    // leaving or entering Silent needs DND access; without it Android throws, and a refused mode must never take the app down
    private fun setMode(mode: Int) {
        val before = am.ringerMode
        try {
            am.ringerMode = mode
        } catch (e: SecurityException) {
            if (mode == AudioManager.RINGER_MODE_SILENT) runCatching { am.ringerMode = AudioManager.RINGER_MODE_VIBRATE }
        }
        android.util.Log.i("MiqatFocus", "ringer $before -> wanted $mode, now ${am.ringerMode}")
    }
}
