package com.kodeelite.nooreislam.feature.quran.data

import com.kodeelite.nooreislam.core.constants.PrefConst
import com.kodeelite.nooreislam.core.constants.defaults.QuranDefaults
import com.kodeelite.nooreislam.core.prefs.PrefsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

// Quran reader settings (feature-local): each flow seeded from PrefsService, setters persist + emit.
// User data (highlights/bookmarks/notes) moved to their own specialized stores.
class QuranStore(
    scope: CoroutineScope,
) {
    private val _fontSize = MutableStateFlow(PrefsService.getInt(PrefConst.QURAN_FONT_SP, QuranDefaults.FONT_SP))
    val fontSize: StateFlow<Int> = _fontSize.asStateFlow()

    fun setFontSize(value: Int) {
        PrefsService.putInt(PrefConst.QURAN_FONT_SP, value)
        _fontSize.value = value
    }

    private val _lineSpacing = MutableStateFlow(PrefsService.getInt(PrefConst.QURAN_LINE_SPACING, QuranDefaults.LINE_SPACING_PERCENT))
    val lineSpacing: StateFlow<Int> = _lineSpacing.asStateFlow()

    fun setLineSpacing(value: Int) {
        PrefsService.putInt(PrefConst.QURAN_LINE_SPACING, value)
        _lineSpacing.value = value
    }

    // ready-to-multiply-by-fontSize ratio — UI never touches QuranDefaults, it just reads this
    val lineHeightRatio: StateFlow<Float> = _lineSpacing
        .map { QuranDefaults.BASE_LINE_HEIGHT_RATIO * (it / 100f) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), QuranDefaults.BASE_LINE_HEIGHT_RATIO)

    private val _font = MutableStateFlow(
        PrefsService.getStringOrNull(PrefConst.QURAN_FONT)
            ?.let { runCatching { QuranFont.valueOf(it) }.getOrNull() } ?: QuranDefaults.FONT,
    )
    val font: StateFlow<QuranFont> = _font.asStateFlow()

    fun setFont(value: QuranFont) {
        PrefsService.putString(PrefConst.QURAN_FONT, value.name)
        _font.value = value
    }

    private val _theme = MutableStateFlow(
        PrefsService.getStringOrNull(PrefConst.QURAN_THEME)
            ?.let { runCatching { QuranTheme.valueOf(it) }.getOrNull() } ?: QuranDefaults.THEME,
    )
    val theme: StateFlow<QuranTheme> = _theme.asStateFlow()

    fun setTheme(value: QuranTheme) {
        PrefsService.putString(PrefConst.QURAN_THEME, value.name)
        _theme.value = value
    }

    private val _favorites = MutableStateFlow(
        PrefsService.getStringOrNull(PrefConst.QURAN_FAVORITES)
            ?.split(",")?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet(),
    )
    val favorites: StateFlow<Set<Int>> = _favorites.asStateFlow()

    fun toggleFavorite(surah: Int) {
        val next = _favorites.value.toMutableSet().apply { if (!add(surah)) remove(surah) }
        PrefsService.putString(PrefConst.QURAN_FAVORITES, next.joinToString(","))
        _favorites.value = next
    }

    private val _recentJumps = MutableStateFlow(
        PrefsService.getStringOrNull(PrefConst.QURAN_RECENT_JUMPS)
            ?.split(",")?.mapNotNull { pair ->
                val (s, a) = pair.split(":").takeIf { it.size == 2 } ?: return@mapNotNull null
                val surah = s.toIntOrNull() ?: return@mapNotNull null
                val ayah = a.toIntOrNull() ?: return@mapNotNull null
                surah to ayah
            } ?: emptyList(),
    )
    val recentJumps: StateFlow<List<Pair<Int, Int>>> = _recentJumps.asStateFlow()

    // most recent first, no duplicate surah+ayah, capped — same lightweight PrefsService list as favorites
    fun recordJump(surah: Int, ayah: Int) {
        val next = (listOf(surah to ayah) + _recentJumps.value.filterNot { it.first == surah && it.second == ayah })
            .take(QuranDefaults.RECENT_JUMPS_LIMIT)
        PrefsService.putString(PrefConst.QURAN_RECENT_JUMPS, next.joinToString(",") { "${it.first}:${it.second}" })
        _recentJumps.value = next
        setJumpToLastSurah(surah)
    }

    // sticky default for Jump To's surah field — whichever surah the user picked (or jumped to) last
    private val _jumpToLastSurah = MutableStateFlow(PrefsService.getInt(PrefConst.QURAN_JUMP_TO_LAST_SURAH, QuranDefaults.JUMP_TO_DEFAULT_SURAH))
    val jumpToLastSurah: StateFlow<Int> = _jumpToLastSurah.asStateFlow()

    fun setJumpToLastSurah(surah: Int) {
        PrefsService.putInt(PrefConst.QURAN_JUMP_TO_LAST_SURAH, surah)
        _jumpToLastSurah.value = surah
    }

    private val _autoScrollSpeed = MutableStateFlow(PrefsService.getInt(PrefConst.QURAN_AUTO_SCROLL_SPEED, QuranDefaults.AUTO_SCROLL_SPEED))
    val autoScrollSpeed: StateFlow<Int> = _autoScrollSpeed.asStateFlow()

    fun setAutoScrollSpeed(value: Int) {
        PrefsService.putInt(PrefConst.QURAN_AUTO_SCROLL_SPEED, value.coerceIn(QuranDefaults.MIN_AUTO_SCROLL_SPEED, QuranDefaults.MAX_AUTO_SCROLL_SPEED))
        _autoScrollSpeed.value = value.coerceIn(QuranDefaults.MIN_AUTO_SCROLL_SPEED, QuranDefaults.MAX_AUTO_SCROLL_SPEED)
    }

    fun increaseAutoScrollSpeed() = setAutoScrollSpeed(_autoScrollSpeed.value + 1)
    fun decreaseAutoScrollSpeed() = setAutoScrollSpeed(_autoScrollSpeed.value - 1)

    // ready-to-scrollBy pixel step for the current speed step — UI never touches QuranDefaults, it just reads this
    private fun pxPerTickFor(step: Int) = QuranDefaults.AUTO_SCROLL_STEP_MULTIPLIERS[step - 1] * QuranDefaults.AUTO_SCROLL_PX_PER_TICK
    val autoScrollPxPerTick: StateFlow<Float> = _autoScrollSpeed
        .map { pxPerTickFor(it) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), pxPerTickFor(QuranDefaults.AUTO_SCROLL_SPEED))

    val autoScrollTickInterval: Duration = QuranDefaults.AUTO_SCROLL_TICK_MS.milliseconds

    // session-only — never persisted, so the reader always opens paused regardless of how it was left
    private val _autoScrollEnabled = MutableStateFlow(false)
    val autoScrollEnabled: StateFlow<Boolean> = _autoScrollEnabled.asStateFlow()

    fun toggleAutoScroll() {
        _autoScrollEnabled.value = !_autoScrollEnabled.value
    }

    fun stopAutoScroll() {
        _autoScrollEnabled.value = false
    }

    private val _keepScreenOn = MutableStateFlow(PrefsService.getBoolean(PrefConst.QURAN_KEEP_SCREEN_ON, QuranDefaults.KEEP_SCREEN_ON))
    val keepScreenOn: StateFlow<Boolean> = _keepScreenOn.asStateFlow()

    fun toggleKeepScreenOn() {
        val next = !_keepScreenOn.value
        PrefsService.putBoolean(PrefConst.QURAN_KEEP_SCREEN_ON, next)
        _keepScreenOn.value = next
    }

    // "Resume" target — only ever written after a real reading dwell (see QuranReaderScreen), so a
    // quick search-and-glance visit never clobbers where the user actually left off. One pref key,
    // "surah:ayah" — getLastRead/saveLastRead are the only two places that encode/decode that string;
    // everywhere else just sees one Pair.
    private val _lastRead = MutableStateFlow(getLastRead())
    val lastRead: StateFlow<Pair<Int, Int>?> = _lastRead.asStateFlow()

    private fun getLastRead(): Pair<Int, Int>? =
        PrefsService.getStringOrNull(PrefConst.QURAN_LAST_READ)
            ?.split(":")
            ?.takeIf { it.size == 2 }
            ?.let { (s, a) -> s.toIntOrNull()?.let { surah -> a.toIntOrNull()?.let { ayah -> surah to ayah } } }

    private fun saveLastRead(surah: Int, ayah: Int) {
        PrefsService.putString(PrefConst.QURAN_LAST_READ, "$surah:$ayah")
    }

    fun recordLastRead(surah: Int, ayah: Int) {
        saveLastRead(surah, ayah)
        _lastRead.value = surah to ayah
    }
}
