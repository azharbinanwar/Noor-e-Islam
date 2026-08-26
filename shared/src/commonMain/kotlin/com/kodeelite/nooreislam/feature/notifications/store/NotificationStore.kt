package com.kodeelite.nooreislam.feature.notifications.store

import com.kodeelite.nooreislam.core.constants.PrefConst
import com.kodeelite.nooreislam.core.constants.PrefConst.Field
import com.kodeelite.nooreislam.core.enums.Miqat
import com.kodeelite.nooreislam.core.prefs.PrefsService
import com.kodeelite.nooreislam.feature.notifications.store.NotificationStore.settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.kodeelite.nooreislam.core.constants.defaults.NotificationDefaults as N

/** One prayer's saved alert. Keyed by the lowercased prayer name (Jumu'ah uses the key "jumuah"). */
data class PrayerAlertConfig(
    val enabled: Boolean,
    val remindBeforeOn: Boolean,
    val remindBefore: Int,
    val atTime: Boolean,
    val jamaat: Boolean,
    val jamaatAfter: Int,
)

data class JumuahConfig(val enabled: Boolean, val remindBeforeOn: Boolean, val remindBefore: Int, val jamaat: Boolean, val jamaatAfter: Int)
data class MulkConfig(val enabled: Boolean, val afterIsha: Int)
data class KahfConfig(val enabled: Boolean, val hour: Int, val minute: Int)
data class DailyReadingConfig(val enabled: Boolean, val hour: Int, val minute: Int)
data class DhikrConfig(val morningEnabled: Boolean, val afterFajr: Int, val eveningEnabled: Boolean, val afterAsr: Int)
data class NafilConfig(val tahajjud: Boolean, val ishraq: Boolean)

/** The whole Notifications screen state in one object, so the screen reads a single flow. */
data class NotificationSettings(
    val allAlerts: Boolean,
    val prayers: Map<String, PrayerAlertConfig>,
    val jumuah: JumuahConfig,
    val mulk: MulkConfig,
    val kahf: KahfConfig,
    val dailyReading: DailyReadingConfig,
    val dhikr: DhikrConfig,
    val nafil: NafilConfig,
)

/** Persists notification settings (per-field prefs), seeded from [NotificationDefaults]. Screens read [settings]. */
object NotificationStore {

    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<NotificationSettings> = _settings.asStateFlow()

    // ── load ─────────────────────────────────────────────────
    private fun load() = NotificationSettings(
        allAlerts = PrefsService.getBoolean(PrefConst.ALL_ALERTS, N.ALL_ALERTS),
        prayers = Miqat.PRAYERS.associate { it.key to loadPrayer(it.key) },
        jumuah = JumuahConfig(
            enabled = PrefsService.getBoolean(PrefConst.alert(Miqat.jumuahKey, Field.ENABLED), N.Jumuah.ENABLED),
            remindBeforeOn = PrefsService.getBoolean(PrefConst.alert(Miqat.jumuahKey, Field.REMIND_BEFORE_ON), N.Jumuah.REMIND_BEFORE_ON),
            remindBefore = PrefsService.getInt(PrefConst.alert(Miqat.jumuahKey, Field.REMIND_BEFORE), N.Jumuah.REMIND_BEFORE),
            jamaat = PrefsService.getBoolean(PrefConst.alert(Miqat.jumuahKey, Field.JAMAAT), N.Jumuah.JAMAAT),
            jamaatAfter = PrefsService.getInt(PrefConst.alert(Miqat.jumuahKey, Field.JAMAAT_AFTER), N.Jumuah.JAMAAT_AFTER),
        ),
        mulk = MulkConfig(
            enabled = PrefsService.getBoolean(PrefConst.SURAH_MULK, N.Mulk.ENABLED),
            afterIsha = PrefsService.getInt(PrefConst.SURAH_MULK_AFTER, N.Mulk.AFTER_ISHA),
        ),
        kahf = KahfConfig(
            enabled = PrefsService.getBoolean(PrefConst.SURAH_KAHF, N.Kahf.ENABLED),
            hour = PrefsService.getInt(PrefConst.SURAH_KAHF_HOUR, N.Kahf.HOUR),
            minute = PrefsService.getInt(PrefConst.SURAH_KAHF_MINUTE, N.Kahf.MINUTE),
        ),
        dailyReading = DailyReadingConfig(
            enabled = PrefsService.getBoolean(PrefConst.DAILY_READING, N.DailyReading.ENABLED),
            hour = PrefsService.getInt(PrefConst.DAILY_READING_HOUR, N.DailyReading.HOUR),
            minute = PrefsService.getInt(PrefConst.DAILY_READING_MINUTE, N.DailyReading.MINUTE),
        ),
        dhikr = DhikrConfig(
            morningEnabled = PrefsService.getBoolean(PrefConst.DHIKR_MORNING, N.Dhikr.MORNING_ENABLED),
            afterFajr = PrefsService.getInt(PrefConst.DHIKR_MORNING_AFTER, N.Dhikr.AFTER_FAJR),
            eveningEnabled = PrefsService.getBoolean(PrefConst.DHIKR_EVENING, N.Dhikr.EVENING_ENABLED),
            afterAsr = PrefsService.getInt(PrefConst.DHIKR_EVENING_AFTER, N.Dhikr.AFTER_ASR),
        ),
        nafil = NafilConfig(
            tahajjud = PrefsService.getBoolean(PrefConst.NAFIL_TAHAJJUD, N.Nafil.TAHAJJUD),
            ishraq = PrefsService.getBoolean(PrefConst.NAFIL_ISHRAQ, N.Nafil.ISHRAQ),
        ),
    )

    private fun loadPrayer(key: String) = PrayerAlertConfig(
        enabled = PrefsService.getBoolean(PrefConst.alert(key, Field.ENABLED), N.Prayer.ENABLED),
        remindBeforeOn = PrefsService.getBoolean(PrefConst.alert(key, Field.REMIND_BEFORE_ON), N.Prayer.REMIND_BEFORE_ON),
        remindBefore = PrefsService.getInt(PrefConst.alert(key, Field.REMIND_BEFORE), N.Prayer.REMIND_BEFORE),
        atTime = PrefsService.getBoolean(PrefConst.alert(key, Field.AT_TIME), N.Prayer.AT_TIME),
        jamaat = PrefsService.getBoolean(PrefConst.alert(key, Field.JAMAAT), N.Prayer.JAMAAT),
        jamaatAfter = PrefsService.getInt(PrefConst.alert(key, Field.JAMAAT_AFTER), N.Prayer.JAMAAT_AFTER),
    )

    // ── master ───────────────────────────────────────────────
    /**
     * Every prayer ships off, so switching the master on with nothing under it would be a switch that
     * does nothing. When no prayer is on yet, the five daily ones start alerting at their time. A setup
     * that already has something on is left exactly as it is.
     */
    fun setAllAlerts(v: Boolean) {
        PrefsService.putBoolean(PrefConst.ALL_ALERTS, v)
        val seed = v && _settings.value.prayers.values.none { it.enabled }
        if (seed) Miqat.PRAYERS.forEach {
            PrefsService.putBoolean(PrefConst.alert(it.key, Field.ENABLED), true)
            PrefsService.putBoolean(PrefConst.alert(it.key, Field.AT_TIME), true)
        }
        update { s ->
            s.copy(
                allAlerts = v,
                prayers = if (!seed) s.prayers else s.prayers.mapValues { (_, cfg) -> cfg.copy(enabled = true, atTime = true) },
            )
        }
    }

    // ── per prayer ───────────────────────────────────────────
    /**
     * A prayer with none of its three options set has nothing to say, so the row switch stays in step
     * with them: switching the row on picks at-prayer-time when nothing is remembered, and clearing the
     * last option switches the row off. Options set earlier survive a trip through off and come back.
     */
    fun setPrayerEnabled(key: String, v: Boolean) {
        val cfg = _settings.value.prayers.getValue(key)
        val seed = v && !cfg.remindBeforeOn && !cfg.atTime && !cfg.jamaat
        PrefsService.putBoolean(PrefConst.alert(key, Field.ENABLED), v)
        if (seed) PrefsService.putBoolean(PrefConst.alert(key, Field.AT_TIME), true)
        updatePrayer(key) { it.copy(enabled = v, atTime = seed || it.atTime) }
    }

    fun setPrayerRemindBeforeOn(key: String, v: Boolean) = putPrayerOption(key, Field.REMIND_BEFORE_ON, v) { it.copy(remindBeforeOn = v) }
    fun setPrayerRemindBefore(key: String, v: Int) = putPrayerInt(key, Field.REMIND_BEFORE, v) { it.copy(remindBefore = v) }
    fun setPrayerAtTime(key: String, v: Boolean) = putPrayerOption(key, Field.AT_TIME, v) { it.copy(atTime = v) }
    fun setPrayerJamaat(key: String, v: Boolean) = putPrayerOption(key, Field.JAMAAT, v) { it.copy(jamaat = v) }
    fun setPrayerJamaatAfter(key: String, v: Int) = putPrayerInt(key, Field.JAMAAT_AFTER, v) { it.copy(jamaatAfter = v) }

    // ── Jumu'ah (prayer-shaped, keyed "jumuah") ──────────────
    fun setJumuahEnabled(v: Boolean) {
        PrefsService.putBoolean(PrefConst.alert(Miqat.jumuahKey, Field.ENABLED), v); update { it.copy(jumuah = it.jumuah.copy(enabled = v)) }
    }

    fun setJumuahRemindBeforeOn(v: Boolean) {
        PrefsService.putBoolean(
            PrefConst.alert(Miqat.jumuahKey, Field.REMIND_BEFORE_ON),
            v
        ); update { it.copy(jumuah = it.jumuah.copy(remindBeforeOn = v)) }
    }

    fun setJumuahRemindBefore(v: Int) {
        PrefsService.putInt(PrefConst.alert(Miqat.jumuahKey, Field.REMIND_BEFORE), v); update { it.copy(jumuah = it.jumuah.copy(remindBefore = v)) }
    }

    fun setJumuahJamaat(v: Boolean) {
        PrefsService.putBoolean(PrefConst.alert(Miqat.jumuahKey, Field.JAMAAT), v); update { it.copy(jumuah = it.jumuah.copy(jamaat = v)) }
    }

    fun setJumuahJamaatAfter(v: Int) {
        PrefsService.putInt(PrefConst.alert(Miqat.jumuahKey, Field.JAMAAT_AFTER), v); update { it.copy(jumuah = it.jumuah.copy(jamaatAfter = v)) }
    }

    // ── Surahs ───────────────────────────────────────────────
    fun setMulkEnabled(v: Boolean) {
        PrefsService.putBoolean(PrefConst.SURAH_MULK, v); update { it.copy(mulk = it.mulk.copy(enabled = v)) }
    }

    fun setMulkAfter(v: Int) {
        PrefsService.putInt(PrefConst.SURAH_MULK_AFTER, v); update { it.copy(mulk = it.mulk.copy(afterIsha = v)) }
    }

    fun setKahfEnabled(v: Boolean) {
        PrefsService.putBoolean(PrefConst.SURAH_KAHF, v); update { it.copy(kahf = it.kahf.copy(enabled = v)) }
    }

    fun setKahfTime(hour: Int, minute: Int) {
        PrefsService.putInt(PrefConst.SURAH_KAHF_HOUR, hour)
        PrefsService.putInt(PrefConst.SURAH_KAHF_MINUTE, minute)
        update { it.copy(kahf = it.kahf.copy(hour = hour, minute = minute)) }
    }

    fun setDailyReadingEnabled(v: Boolean) {
        PrefsService.putBoolean(PrefConst.DAILY_READING, v); update { it.copy(dailyReading = it.dailyReading.copy(enabled = v)) }
    }

    fun setDailyReadingTime(hour: Int, minute: Int) {
        PrefsService.putInt(PrefConst.DAILY_READING_HOUR, hour)
        PrefsService.putInt(PrefConst.DAILY_READING_MINUTE, minute)
        update { it.copy(dailyReading = it.dailyReading.copy(hour = hour, minute = minute)) }
    }

    // ── Dhikr ────────────────────────────────────────────────
    fun setMorningEnabled(v: Boolean) {
        PrefsService.putBoolean(PrefConst.DHIKR_MORNING, v); update { it.copy(dhikr = it.dhikr.copy(morningEnabled = v)) }
    }

    fun setMorningAfter(v: Int) {
        PrefsService.putInt(PrefConst.DHIKR_MORNING_AFTER, v); update { it.copy(dhikr = it.dhikr.copy(afterFajr = v)) }
    }

    fun setEveningEnabled(v: Boolean) {
        PrefsService.putBoolean(PrefConst.DHIKR_EVENING, v); update { it.copy(dhikr = it.dhikr.copy(eveningEnabled = v)) }
    }

    fun setEveningAfter(v: Int) {
        PrefsService.putInt(PrefConst.DHIKR_EVENING_AFTER, v); update { it.copy(dhikr = it.dhikr.copy(afterAsr = v)) }
    }

    // ── Nafil ────────────────────────────────────────────────
    fun setTahajjud(v: Boolean) {
        PrefsService.putBoolean(PrefConst.NAFIL_TAHAJJUD, v); update { it.copy(nafil = it.nafil.copy(tahajjud = v)) }
    }

    fun setIshraq(v: Boolean) {
        PrefsService.putBoolean(PrefConst.NAFIL_ISHRAQ, v); update { it.copy(nafil = it.nafil.copy(ishraq = v)) }
    }

    // ── plumbing ─────────────────────────────────────────────
    private fun update(f: (NotificationSettings) -> NotificationSettings) {
        _settings.value = f(_settings.value)
    }

    private fun updatePrayer(key: String, g: (PrayerAlertConfig) -> PrayerAlertConfig) =
        update { it.copy(prayers = it.prayers + (key to g(it.prayers.getValue(key)))) }

    private fun putBool(pref: String, v: Boolean, f: (NotificationSettings) -> NotificationSettings) {
        PrefsService.putBoolean(pref, v); update(f)
    }

    /** One of the three alert options — writes it, then lets the row switch follow whether any survive. */
    private fun putPrayerOption(key: String, field: String, v: Boolean, g: (PrayerAlertConfig) -> PrayerAlertConfig) {
        val next = g(_settings.value.prayers.getValue(key))
        val enabled = next.remindBeforeOn || next.atTime || next.jamaat
        PrefsService.putBoolean(PrefConst.alert(key, field), v)
        PrefsService.putBoolean(PrefConst.alert(key, Field.ENABLED), enabled)
        updatePrayer(key) { next.copy(enabled = enabled) }
    }

    private fun putPrayerInt(key: String, field: String, v: Int, g: (PrayerAlertConfig) -> PrayerAlertConfig) {
        PrefsService.putInt(PrefConst.alert(key, field), v); updatePrayer(key, g)
    }
}
