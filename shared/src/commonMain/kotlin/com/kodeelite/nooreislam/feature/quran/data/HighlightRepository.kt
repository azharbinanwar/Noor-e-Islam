package com.kodeelite.nooreislam.feature.quran.data

import com.kodeelite.nooreislam.core.constants.PrefConst
import com.kodeelite.nooreislam.core.constants.defaults.QuranDefaults
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.prefs.PrefsService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// takes plain surah/ayah; the reader observes `colors` (key -> color) to paint tints.
class HighlightRepository(private val dao: HighlightDao) {
    val colors: Flow<Map<String, HighlightColor>> =
        dao.activeFlow().map { rows -> rows.associate { "${it.surah}:${it.ayah}" to it.color } }
    val active: Flow<List<Highlight>> = dao.activeFlow()

    // the active color for one ayah (null = not highlighted)
    fun colorOf(surah: Int, ayah: Int): Flow<HighlightColor?> = colors.map { it["$surah:$ayah"] }

    // sticky default: the last color the user applied. A scalar setting, so it lives in prefs (like theme/font),
    // seeded from QuranDefaults. ponytail: single value, no table needed.
    var defaultColor: HighlightColor
        get() = PrefsService.getStringOrNull(PrefConst.QURAN_HIGHLIGHT_COLOR)
            ?.let { runCatching { HighlightColor.valueOf(it) }.getOrNull() } ?: QuranDefaults.HIGHLIGHT_COLOR
        private set(v) = PrefsService.putString(PrefConst.QURAN_HIGHLIGHT_COLOR, v.name)

    // apply a highlight; the color used sticks as the new default. quick action omits color → uses the default.
    suspend fun set(surah: Int, ayah: Int, color: HighlightColor = defaultColor) {
        val now = Now.epochMillis()
        val cur = dao.get(surah, ayah)
        dao.upsert(
            (cur ?: Highlight(surah = surah, ayah = ayah, createdAt = now, updatedAt = now))
                .copy(color = color, deletedAt = null, updatedAt = now, synced = false)
        )
        defaultColor = color
    }

    suspend fun remove(surah: Int, ayah: Int) {
        val cur = dao.get(surah, ayah) ?: return
        dao.upsert(cur.copy(deletedAt = Now.epochMillis(), updatedAt = Now.epochMillis(), synced = false))
    }

    // ponytail: server sync deferred — fields + these hooks are here; the pusher/merge lands with login.
    suspend fun pending(): List<Highlight> = dao.pending()
    suspend fun markSynced(surah: Int, ayah: Int) = dao.markSynced(surah, ayah)
}
