package com.kodeelite.nooreislam.core.store

import com.kodeelite.nooreislam.config.theme.ThemeChoice
import com.kodeelite.nooreislam.core.constants.PrefConst
import com.kodeelite.nooreislam.core.constants.defaults.SettingsDefaults
import com.kodeelite.nooreislam.core.datetime.HijriDate
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.datetime.toHijri
import com.kodeelite.nooreislam.core.enums.DateFormatStyle
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.enums.TimeFormat
import com.kodeelite.nooreislam.core.locale.Language
import com.kodeelite.nooreislam.core.prefs.PrefsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

/**
 * General app settings — theme, language, clock format, Hijri offset. Same shape as the other stores:
 * seed each flow from [PrefsService] once (`getX ?: SettingsDefaults`), then consumers observe the flow;
 * setters persist via the generic key-value passthroughs AND emit, so a change anywhere updates everyone.
 */
object SettingsStore {

    private val _theme = MutableStateFlow(
        PrefsService.getStringOrNull(PrefConst.THEME)?.let { ThemeChoice.fromValue(it) } ?: SettingsDefaults.theme,
    )
    val theme: StateFlow<ThemeChoice> = _theme.asStateFlow()

    private val _language = MutableStateFlow(
        PrefsService.getStringOrNull(PrefConst.LANGUAGE)?.let { Language.fromCode(it) } ?: SettingsDefaults.language,
    )
    val language: StateFlow<Language> = _language.asStateFlow()

    private val _timeFormat = MutableStateFlow(
        PrefsService.getStringOrNull(PrefConst.TIME_FORMAT)?.let { TimeFormat.fromValue(it) } ?: SettingsDefaults.timeFormat,
    )
    val timeFormat: StateFlow<TimeFormat> = _timeFormat.asStateFlow()

    private val _gregorianDateFormat = MutableStateFlow(
        PrefsService.getStringOrNull(PrefConst.GREGORIAN_DATE_FORMAT)?.let { DateFormatStyle.fromValue(it) }
            ?: SettingsDefaults.gregorianDateFormat,
    )
    val gregorianDateFormat: StateFlow<DateFormatStyle> = _gregorianDateFormat.asStateFlow()

    private val _hijriDateFormat = MutableStateFlow(
        PrefsService.getStringOrNull(PrefConst.HIJRI_DATE_FORMAT)?.let { DateFormatStyle.fromValue(it) }
            ?: SettingsDefaults.hijriDateFormat,
    )
    val hijriDateFormat: StateFlow<DateFormatStyle> = _hijriDateFormat.asStateFlow()

    /** Which time the Ramadan "Sehri" label follows — Fajr (the ruling) or Imsak (the precaution). */
    private val _sehriReference = MutableStateFlow(
        PrefsService.getStringOrNull(PrefConst.SEHRI_REFERENCE)?.let { name -> Miqat.entries.firstOrNull { it.name == name } }
            ?: SettingsDefaults.sehriReference,
    )
    val sehriReference: StateFlow<Miqat> = _sehriReference.asStateFlow()

    private val _hijriOffset = MutableStateFlow(PrefsService.getInt(PrefConst.HIJRI_OFFSET, SettingsDefaults.HIJRI_OFFSET))
    val hijriOffset: StateFlow<Int> = _hijriOffset.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Today's Hijri date with the offset applied; follows Now, so it rolls with the clock. */
    val hijriDate: StateFlow<HijriDate> = combine(
        Now.now.map { it.date }.distinctUntilChanged(),
        _hijriOffset,
    ) { date, off -> toHijri(date.plus(off, DateTimeUnit.DAY)) }
        .stateIn(scope, SharingStarted.Eagerly, Now.hijri(_hijriOffset.value))

    fun setTheme(value: ThemeChoice) {
        PrefsService.putString(PrefConst.THEME, value.value)
        _theme.value = value
    }

    /** True once the intro has been through. Read at start-up to choose the first screen. */
    fun introSeen(): Boolean = PrefsService.getBoolean(PrefConst.INTRO_SEEN, false)

    fun markIntroSeen() = PrefsService.putBoolean(PrefConst.INTRO_SEEN, true)

    fun setLanguage(value: Language) {
        PrefsService.putString(PrefConst.LANGUAGE, value.code)
        _language.value = value
    }

    fun setTimeFormat(value: TimeFormat) {
        PrefsService.putString(PrefConst.TIME_FORMAT, value.value)
        _timeFormat.value = value
    }

    fun setGregorianDateFormat(value: DateFormatStyle) {
        PrefsService.putString(PrefConst.GREGORIAN_DATE_FORMAT, value.value)
        _gregorianDateFormat.value = value
    }

    fun setHijriDateFormat(value: DateFormatStyle) {
        PrefsService.putString(PrefConst.HIJRI_DATE_FORMAT, value.value)
        _hijriDateFormat.value = value
    }

    fun setSehriReference(value: Miqat) {
        PrefsService.putString(PrefConst.SEHRI_REFERENCE, value.name)
        _sehriReference.value = value
    }

    fun setHijriOffset(value: Int) {
        PrefsService.putInt(PrefConst.HIJRI_OFFSET, value)
        _hijriOffset.value = value
    }
}
