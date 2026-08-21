package com.kodeelite.nooreislam.core.catalog

import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.BellOff
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Compass
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.Globe
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.LayoutGrid
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Moon
import com.composables.icons.lucide.Palette
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.SquareCheck
import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.core.platform.Platform
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.about
import com.kodeelite.nooreislam.resources.appearance
import com.kodeelite.nooreislam.resources.date_format
import com.kodeelite.nooreislam.resources.developer_sandbox
import com.kodeelite.nooreislam.resources.duas_and_adhkar
import com.kodeelite.nooreislam.resources.excused_days
import com.kodeelite.nooreislam.resources.hijri_calendar
import com.kodeelite.nooreislam.resources.hijri_date_format
import com.kodeelite.nooreislam.resources.home
import com.kodeelite.nooreislam.resources.language
import com.kodeelite.nooreislam.resources.location
import com.kodeelite.nooreislam.resources.notifications
import com.kodeelite.nooreislam.resources.prayer_calculation
import com.kodeelite.nooreislam.resources.prayer_focus
import com.kodeelite.nooreislam.resources.prayer_streak
import com.kodeelite.nooreislam.resources.prayer_times
import com.kodeelite.nooreislam.resources.prayer_tracker
import com.kodeelite.nooreislam.resources.qibla_compass
import com.kodeelite.nooreislam.resources.quran
import com.kodeelite.nooreislam.resources.quran_text_source
import com.kodeelite.nooreislam.resources.reading_marks
import com.kodeelite.nooreislam.resources.settings
import com.kodeelite.nooreislam.resources.tasbih
import com.kodeelite.nooreislam.resources.time_format
import com.kodeelite.nooreislam.resources.widgets

private val MAIN = setOf(AppEdition.MAIN)
private val DRAWER_HOME = setOf(Surface.Drawer, Surface.Home)
private val SETTINGS = setOf(Surface.Settings, Surface.Home)

/** Ids for rows inside the Settings screen, and the group each one sits in. */
object Anchor {
    const val APPEARANCE = "appearance"
    const val TIME_FORMAT = "time_format"
    const val LANGUAGE = "language"
    const val HIJRI_CALENDAR = "hijri_calendar"
    const val DATE_FORMAT = "date_format"
    const val HIJRI_DATE_FORMAT = "hijri_date_format"
    const val STREAK = "streak"
    const val EXCUSED = "excused"
    const val ABOUT = "about"
    const val TEXT_SOURCE = "text_source"

    const val GENERAL = "g_general"
    const val DATE_FORMATS = "g_date_formats"
    const val PRAYER_AND_ALERTS = "g_prayer_and_alerts"
    const val STREAK_GROUP = "g_streak"
    const val ABOUT_GROUP = "g_about"

    fun groupOf(anchor: String?): String? = when (anchor) {
        APPEARANCE, TIME_FORMAT, LANGUAGE, HIJRI_CALENDAR -> GENERAL
        DATE_FORMAT, HIJRI_DATE_FORMAT -> DATE_FORMATS
        STREAK, EXCUSED -> STREAK_GROUP
        ABOUT, TEXT_SOURCE -> ABOUT_GROUP
        else -> null
    }
}

/**
 * Every place a user can be taken. Each entry names the surfaces that list it.
 * `debug` comes from the caller because Koin isn't reachable from a plain val.
 */
fun appCatalog(debug: Boolean): List<AppFeature> = listOf(
    AppFeature(Res.string.home, Lucide.House, AppRoute.Home, surfaces = setOf(Surface.Drawer)),
    AppFeature(Res.string.prayer_times, Lucide.Clock, AppRoute.PrayerTimes, editions = MAIN, surfaces = DRAWER_HOME),
    AppFeature(Res.string.qibla_compass, Lucide.Compass, AppRoute.Qibla, editions = MAIN, surfaces = DRAWER_HOME),
    AppFeature(Res.string.quran, Lucide.BookOpen, AppRoute.Quran, surfaces = DRAWER_HOME),
    AppFeature(Res.string.tasbih, Lucide.Flame, AppRoute.Tasbih, editions = MAIN, surfaces = DRAWER_HOME),
    AppFeature(
        Res.string.prayer_tracker, Lucide.SquareCheck, AppRoute.Tracker,
        editions = MAIN, surfaces = DRAWER_HOME,
        available = { SettingsStore.streakEnabled.value },
    ),
    AppFeature(Res.string.settings, Lucide.Settings, AppRoute.Settings(), surfaces = DRAWER_HOME),
    AppFeature(Res.string.reading_marks, Lucide.BookOpen, AppRoute.ReadingMarks, surfaces = setOf(Surface.Home)),

    AppFeature(
        Res.string.notifications, Lucide.Bell, AppRoute.Notifications, surfaces = SETTINGS,
        keywords = listOf("alerts", "reminders", "notification", "adhan", "azan", "sound", "jamaat", "تنبيهات", "إشعارات", "أذان", "صوت", "جماعة", "اطلاعات", "یاد دہانی", "اذان", "آواز", "جماعت", "alertes", "rappels", "notifications", "son"),
    ),
    AppFeature(
        Res.string.prayer_focus, Lucide.BellOff, AppRoute.Focus,
        editions = MAIN, surfaces = SETTINGS,
        keywords = listOf("silent", "dnd", "do not disturb", "mute", "vibrate", "ringer", "focus", "صامت", "عدم الإزعاج", "كتم", "اهتزاز", "الرنين", "خاموش", "ڈسٹرب نہ کریں", "میوٹ", "ارتعاش", "گھنٹی", "silencieux", "ne pas déranger", "muet", "vibreur", "sonnerie"),
        available = { Platform.canControlDnd },
    ),
    AppFeature(
        Res.string.widgets, Lucide.LayoutGrid, AppRoute.Widgets,
        editions = MAIN, surfaces = SETTINGS,
        keywords = listOf("home screen", "shortcut", "widget", "الشاشة الرئيسية", "اختصار", "أداة", "ہوم اسکرین", "شارٹ کٹ", "ویجٹ", "écran d'accueil", "raccourci", "widget"),
        available = { Platform.hasHomeScreenWidgets },
    ),
    AppFeature(
        Res.string.location, Lucide.MapPin, AppRoute.Location, editions = MAIN, surfaces = SETTINGS,
        keywords = listOf("city", "gps", "place", "coordinates", "timezone", "المدينة", "الموقع", "الإحداثيات", "المنطقة الزمنية", "شہر", "مقام", "جی پی ایس", "ٹائم زون", "ville", "lieu", "coordonnées", "fuseau horaire"),
    ),
    AppFeature(
        Res.string.prayer_calculation, Lucide.Compass, AppRoute.PrayerCalc,
        editions = MAIN, surfaces = SETTINGS,
        keywords = listOf("madhab", "method", "angle", "hanafi", "shafi", "asr", "umm al-qura", "المذهب", "طريقة الحساب", "الزاوية", "حنفي", "شافعي", "أم القرى", "مذہب", "طریقہ", "زاویہ", "حنفی", "شافعی", "méthode", "angle", "hanafite", "chaféite"),
    ),

    AppFeature(
        Res.string.appearance, Lucide.Palette, AppRoute.Settings(), Anchor.APPEARANCE, surfaces = SETTINGS,
        keywords = listOf("theme", "dark mode", "light mode", "colour", "color", "night", "المظهر", "الوضع الداكن", "الوضع الفاتح", "الألوان", "ليلي", "تھیم", "ڈارک موڈ", "لائٹ موڈ", "رنگ", "thème", "mode sombre", "mode clair", "couleur"),
    ),
    AppFeature(
        Res.string.time_format, Lucide.Clock, AppRoute.Settings(), Anchor.TIME_FORMAT, surfaces = SETTINGS,
        keywords = listOf("clock", "12 hour", "24 hour", "am", "pm", "time", "الساعة", "١٢ ساعة", "٢٤ ساعة", "صباحاً", "مساءً", "گھڑی", "وقت", "12 گھنٹے", "24 گھنٹے", "horloge", "heures", "12 heures", "24 heures"),
    ),
    AppFeature(
        Res.string.language, Lucide.Globe, AppRoute.Settings(), Anchor.LANGUAGE, surfaces = SETTINGS,
        keywords = listOf("translation", "english", "arabic", "urdu", "french", "locale", "اللغة", "الترجمة", "العربية", "الإنجليزية", "الأردية", "الفرنسية", "زبان", "ترجمہ", "اردو", "عربی", "انگریزی", "langue", "traduction", "français", "anglais", "arabe"),
    ),
    AppFeature(
        Res.string.hijri_calendar, Lucide.Calendar, AppRoute.Settings(), Anchor.HIJRI_CALENDAR,
        editions = MAIN, surfaces = SETTINGS,
        keywords = listOf("moon sighting", "offset", "adjust", "islamic date", "calendar", "رؤية الهلال", "التقويم", "تعديل", "التاريخ الهجري", "رویت ہلال", "کیلنڈر", "ایڈجسٹ", "ہجری تاریخ", "observation de la lune", "calendrier", "décalage"),
    ),
    AppFeature(
        Res.string.date_format, Lucide.CalendarDays, AppRoute.Settings(), Anchor.DATE_FORMAT, surfaces = SETTINGS,
        keywords = listOf("gregorian", "calendar", "day", "month", "year", "format", "التاريخ الميلادي", "التقويم", "اليوم", "الشهر", "السنة", "تاریخ", "میلادی", "کیلنڈر", "دن", "مہینہ", "grégorien", "calendrier", "jour", "mois", "année"),
    ),
    AppFeature(
        Res.string.hijri_date_format, Lucide.Moon, AppRoute.Settings(), Anchor.HIJRI_DATE_FORMAT, surfaces = SETTINGS,
        keywords = listOf("islamic date", "lunar", "calendar", "hijra", "hijri", "التاريخ الهجري", "القمري", "التقويم", "ہجری تاریخ", "قمری", "کیلنڈر", "date islamique", "lunaire", "calendrier", "hégire"),
    ),
    AppFeature(
        Res.string.prayer_streak, Lucide.Flame, AppRoute.Settings(), Anchor.STREAK,
        editions = MAIN, surfaces = SETTINGS,
        keywords = listOf("habit", "progress", "on time", "best run", "streak", "العادة", "التقدم", "في الوقت", "أفضل سلسلة", "عادت", "پیش رفت", "وقت پر", "سلسلہ", "habitude", "progression", "à l'heure", "meilleure série"),
    ),
    AppFeature(
        Res.string.excused_days, Lucide.Pause, AppRoute.Settings(), Anchor.EXCUSED,
        editions = MAIN, surfaces = SETTINGS,
        keywords = listOf("period", "menstruation", "hayd", "nifas", "exempt", "pause", "الحيض", "النفاس", "الدورة الشهرية", "معذورة", "ماہواری", "حیض", "نفاس", "معذور", "règles", "menstruation", "dispensée"),
        available = { SettingsStore.streakEnabled.value },
    ),
    AppFeature(
        Res.string.about, Lucide.Info, AppRoute.Settings(), Anchor.ABOUT, surfaces = SETTINGS,
        keywords = listOf("version", "build", "app info", "licence", "license", "الإصدار", "حول التطبيق", "معلومات", "الترخيص", "ورژن", "ایپ کی معلومات", "لائسنس", "version", "à propos", "licence"),
    ),
    AppFeature(
        Res.string.quran_text_source, Lucide.BookOpen, AppRoute.Settings(), Anchor.TEXT_SOURCE, surfaces = SETTINGS,
        keywords = listOf("tanzil", "uthmani", "script", "mushaf", "arabic text", "تنزيل", "عثماني", "المصحف", "الرسم", "النص العربي", "تنزیل", "عثمانی", "مصحف", "رسم الخط", "عربی متن", "tanzil", "graphie", "texte arabe"),
    ),

    AppFeature(Res.string.duas_and_adhkar, Lucide.BookOpen, AppRoute.Azkar, surfaces = DRAWER_HOME, available = { debug }),
    AppFeature(Res.string.developer_sandbox, Lucide.Flame, AppRoute.Sandbox, available = { debug }),
)

fun featuresOn(surface: Surface, edition: AppEdition, debug: Boolean): List<AppFeature> =
    appCatalog(debug).filter { it.shownOn(surface, edition) }
