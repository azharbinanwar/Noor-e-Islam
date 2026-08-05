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

// Quran reader settings (feature-local): each flow seeded from PrefsService, setters persist + emit.
// User data (highlights/bookmarks/notes) moved to their own specialized stores.
class QuranStore(
    private val scope: CoroutineScope,
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
}
