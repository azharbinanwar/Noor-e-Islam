package com.kodeelite.nooreislam.feature.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.BellOff
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Compass
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.Globe
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.LayoutGrid
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.Moon
import com.composables.icons.lucide.Palette
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.X
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.AppEdition
import com.kodeelite.nooreislam.core.catalog.Anchor
import com.kodeelite.nooreislam.core.components.AppBottomSheet
import com.kodeelite.nooreislam.core.components.AppSwitch
import com.kodeelite.nooreislam.core.components.AppTextField
import com.kodeelite.nooreislam.core.components.AppTileGroup
import com.kodeelite.nooreislam.core.components.AppTileItem
import com.kodeelite.nooreislam.core.components.LocalDrawerState
import com.kodeelite.nooreislam.core.components.MiniStepper
import com.kodeelite.nooreislam.core.components.SwapPill
import com.kodeelite.nooreislam.core.datetime.HijriMonth
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.displayName
import com.kodeelite.nooreislam.core.enums.TimeFormat
import com.kodeelite.nooreislam.core.locale.Language
import com.kodeelite.nooreislam.core.locale.tr
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.core.platform.Platform
import com.kodeelite.nooreislam.core.platform.appVersion
import com.kodeelite.nooreislam.core.store.LocationStore
import com.kodeelite.nooreislam.core.store.SettingsStore
import com.kodeelite.nooreislam.feature.miqat.store.MiqatCalculationStore
import com.kodeelite.nooreislam.feature.notifications.store.NotificationStore
import com.kodeelite.nooreislam.feature.settings.presentation.components.SettingsSearchResults
import com.kodeelite.nooreislam.feature.tracker.data.ExemptionStore
import com.kodeelite.nooreislam.feature.tracker.data.TrackerStore
import com.kodeelite.nooreislam.feature.tracker.presentation.components.ExemptionStartSheet
import com.kodeelite.nooreislam.feature.tracker.presentation.components.exemptionSubtitle
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.about
import com.kodeelite.nooreislam.resources.all_alerts_off
import com.kodeelite.nooreislam.resources.all_alerts_on
import com.kodeelite.nooreislam.resources.appearance
import com.kodeelite.nooreislam.resources.auto_silence_around_prayer
import com.kodeelite.nooreislam.resources.back
import com.kodeelite.nooreislam.resources.date_format
import com.kodeelite.nooreislam.resources.date_formats
import com.kodeelite.nooreislam.resources.days
import com.kodeelite.nooreislam.resources.general
import com.kodeelite.nooreislam.resources.hijri_calendar
import com.kodeelite.nooreislam.resources.hijri_date_format
import com.kodeelite.nooreislam.resources.hijri_era
import com.kodeelite.nooreislam.resources.language
import com.kodeelite.nooreislam.resources.location
import com.kodeelite.nooreislam.resources.menu
import com.kodeelite.nooreislam.resources.notifications
import com.kodeelite.nooreislam.resources.prayer_and_alerts
import com.kodeelite.nooreislam.resources.prayer_calculation
import com.kodeelite.nooreislam.resources.prayer_exemption
import com.kodeelite.nooreislam.resources.prayer_focus
import com.kodeelite.nooreislam.resources.prayer_streak
import com.kodeelite.nooreislam.resources.quran_text_source
import com.kodeelite.nooreislam.resources.search_settings
import com.kodeelite.nooreislam.resources.settings
import com.kodeelite.nooreislam.resources.show_streaks_best_run_and_on_time_percentage
import com.kodeelite.nooreislam.resources.streak
import com.kodeelite.nooreislam.resources.time_format
import com.kodeelite.nooreislam.resources.version_summary
import com.kodeelite.nooreislam.resources.widgets
import com.kodeelite.nooreislam.resources.widgets_summary
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(open: String? = null) {
    val nav = LocalAppNavigator.current
    val edition = koinInject<AppEdition>()
    val drawerState = LocalDrawerState.current
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    // basic prefs — observe the SettingsStore (resolves PrefsService ?: SettingsDefaults)
    val theme by SettingsStore.theme.collectAsState()
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val gregorianDateFormat by SettingsStore.gregorianDateFormat.collectAsState()
    val hijriDateFormat by SettingsStore.hijriDateFormat.collectAsState()
    val language by SettingsStore.language.collectAsState()
    var sheet by remember { mutableStateOf(open) }
    fun close() { sheet = null }
    var query by remember { mutableStateOf("") }
    var askingExemption by remember { mutableStateOf(false) }
    val tracker = koinInject<TrackerStore>()
    val exemption = koinInject<ExemptionStore>()
    val exemptionOn by exemption.on.collectAsState()
    val focus = LocalFocusManager.current
    var highlight by remember { mutableStateOf(open) }
    val scroll = rememberScrollState()
    val groupTop = remember { mutableStateMapOf<String, Int>() }
    // search lands on the group, then marks the exact row so the eye finds it
    LaunchedEffect(highlight, groupTop[Anchor.groupOf(highlight)]) {
        val y = groupTop[Anchor.groupOf(highlight)] ?: return@LaunchedEffect
        scroll.animateScrollTo((y - 24).coerceAtLeast(0))
        delay(2600.milliseconds)
        highlight = null
    }
    fun anchored(anchor: String) = Modifier.onGloballyPositioned {
        groupTop[anchor] = it.positionInParent().y.toInt()
    }
    // Hijri ± day offset (moon-sighting adjustment) — the calendar page is hidden, so it's tuned here
    val hijriOffset by SettingsStore.hijriOffset.collectAsState()
    val hijri by SettingsStore.hijriDate.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.settings)) },
                navigationIcon = {
                    // the Quran app reached Settings by pushing it (no drawer), so it goes back instead of opening one
                    if (edition == AppEdition.QURAN) {
                        IconButton(onClick = { nav.back() }) { Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), stringResource(Res.string.back)) }
                    } else {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Lucide.Menu, stringResource(Res.string.menu)) }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            Modifier.fillMaxSize().padding(innerPadding).verticalScroll(scroll).padding(16.dp),
        ) {
            val searchable = edition != AppEdition.QURAN
            if (searchable) {
                AppTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = stringResource(Res.string.search_settings),
                    leading = { Icon(Lucide.Search, null, tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(20.dp)) },
                    trailing = {
                        if (query.isNotEmpty()) {
                            Icon(
                                Lucide.X, null, tint = AppTheme.colors.onSurfaceVariant,
                                modifier = Modifier.clickable { query = "" }.size(20.dp),
                            )
                        }
                    },
                )
                Spacer(Modifier.height(16.dp))
            }
            if (searchable && query.isNotBlank()) {
                SettingsSearchResults(query) { feature ->
                    query = ""
                    focus.clearFocus()
                    val target = feature.route
                    if (target is AppRoute.Settings) highlight = feature.anchor else nav.navigate(target)
                }
                return@Column
            }
            AppTileGroup(
                modifier = anchored(Anchor.GENERAL),
                title = stringResource(Res.string.general),
                items = buildList {
                    add(
                        AppTileItem(
                            leadingIcon = Lucide.Palette,
                            title = stringResource(Res.string.appearance),
                            selected = highlight == Anchor.APPEARANCE,
                            subtitle = theme.label(),
                            onClick = { sheet = Anchor.APPEARANCE })
                    )
                    // time format and the Hijri offset only matter for prayer times — not shown in the reading-only app
                    if (edition != AppEdition.QURAN) add(
                        AppTileItem(
                            leadingIcon = Lucide.Clock,
                            title = stringResource(Res.string.time_format),
                            selected = highlight == Anchor.TIME_FORMAT,
                            trailing = { SwapPill(timeFormat.label()) },
                            onClick = { SettingsStore.setTimeFormat(TimeFormat.entries.first { it != timeFormat }) })
                    )
                    add(
                        AppTileItem(
                            leadingIcon = Lucide.Globe,
                            title = stringResource(Res.string.language),
                            selected = highlight == Anchor.LANGUAGE,
                            subtitle = language.label,
                            onClick = { sheet = Anchor.LANGUAGE })
                    )
                    // hidden for now — no Quran-specific widgets exist yet, current gallery is prayer widgets only
                    if (Platform.hasHomeScreenWidgets && edition != AppEdition.QURAN) add(
                        AppTileItem(
                            leadingIcon = Lucide.LayoutGrid,
                            title = stringResource(Res.string.widgets),
                            subtitle = stringResource(Res.string.widgets_summary),
                            onClick = { nav.navigate(AppRoute.Widgets) })
                    )
                    if (edition != AppEdition.QURAN) add(
                        AppTileItem(
                            leadingIcon = Lucide.Calendar,
                            title = stringResource(Res.string.hijri_calendar),
                            selected = highlight == Anchor.HIJRI_CALENDAR,
                            subtitle = "${hijri.day} ${HijriMonth.of(hijri.month).label()} ${hijri.year} ${stringResource(Res.string.hijri_era)}",
                            trailing = {
                                MiniStepper(
                                    hijriOffset,
                                    stringResource(Res.string.days),
                                    { SettingsStore.setHijriOffset(it) },
                                    min = -2,
                                    max = 2
                                )
                            },
                        )
                    )
                },
            )
            // Gregorian/Hijri date formats only matter where a date is actually shown — not in the reading-only app
            if (edition != AppEdition.QURAN) AppTileGroup(
                modifier = anchored(Anchor.DATE_FORMATS),
                title = stringResource(Res.string.date_formats),
                items = listOf(
                    AppTileItem(
                        leadingIcon = Lucide.CalendarDays,
                        title = stringResource(Res.string.date_format),
                            selected = highlight == Anchor.DATE_FORMAT,
                        subtitle = Now.formattedDate(),
                        onClick = { sheet = Anchor.DATE_FORMAT }),
                    AppTileItem(
                        leadingIcon = Lucide.Moon,
                        title = stringResource(Res.string.hijri_date_format),
                            selected = highlight == Anchor.HIJRI_DATE_FORMAT,
                        subtitle = Now.formattedHijri(hijriOffset),
                        onClick = { sheet = Anchor.HIJRI_DATE_FORMAT }),
                ),
            )
            val notificationSettings by NotificationStore.settings.collectAsState()
            val activeCity by LocationStore.activePlace.collectAsState()
            val asrMadhab by MiqatCalculationStore.madhab.collectAsState()
            val streakEnabled by SettingsStore.streakEnabled.collectAsState()
            // turning it on is a decision with settings behind it; turning it off is just off
            fun askExemption(next: Boolean) = if (next) askingExemption = true else exemption.end()
            val calcMethod by MiqatCalculationStore.method.collectAsState()
            val highLat by MiqatCalculationStore.highLatRule.collectAsState()
            AppTileGroup(
                modifier = anchored(Anchor.PRAYER_AND_ALERTS),
                title = stringResource(Res.string.prayer_and_alerts),
                items = buildList {
                    // location and calculation method only feed prayer-time math — not used by the reading-only app
                    if (edition != AppEdition.QURAN) add(
                        AppTileItem(
                            leadingIcon = Lucide.MapPin,
                            title = stringResource(Res.string.location),
                            subtitle = activeCity.name,
                            onClick = { nav.navigate(AppRoute.Location) })
                    )
                    // madhab · method · high-lat — one line, ellipsized by the tile if long
                    if (edition != AppEdition.QURAN) add(
                        AppTileItem(
                            leadingIcon = Lucide.Compass,
                            title = stringResource(Res.string.prayer_calculation),
                            subtitle = "${asrMadhab.label} · ${calcMethod.shortName} · ${highLat.label}",
                            onClick = { nav.navigate(AppRoute.PrayerCalc) })
                    )
                    add(
                        AppTileItem(
                            leadingIcon = Lucide.Bell,
                            title = stringResource(Res.string.notifications),
                            subtitle = stringResource(if (notificationSettings.allAlerts) Res.string.all_alerts_on else Res.string.all_alerts_off),
                            onClick = { nav.navigate(AppRoute.Notifications) })
                    )
                    if (Platform.canControlDnd && edition != AppEdition.QURAN) add(
                        AppTileItem(
                            leadingIcon = Lucide.BellOff,
                            title = stringResource(Res.string.prayer_focus),
                            subtitle = stringResource(Res.string.auto_silence_around_prayer),
                            onClick = { nav.navigate(AppRoute.Focus) })
                    )
                },
            )
            if (edition != AppEdition.QURAN) AppTileGroup(
                modifier = anchored(Anchor.STREAK_GROUP),
                title = stringResource(Res.string.streak),
                items = buildList {
                    add(
                        AppTileItem(
                            leadingIcon = Lucide.Flame,
                            title = stringResource(Res.string.prayer_streak),
                            selected = highlight == Anchor.STREAK,
                            subtitle = stringResource(Res.string.show_streaks_best_run_and_on_time_percentage),
                            trailing = { AppSwitch(streakEnabled, tracker::setStreakEnabled) },
                            onClick = { tracker.setStreakEnabled(!streakEnabled) },
                        )
                    )
                    add(
                        AppTileItem(
                            leadingIcon = Lucide.Pause,
                            title = stringResource(Res.string.prayer_exemption),
                            selected = highlight == Anchor.EXEMPTION,
                            subtitle = exemptionSubtitle(),
                            trailing = { AppSwitch(exemptionOn, ::askExemption) },
                            onClick = { askExemption(!exemptionOn) },
                        )
                    )
                },
            )
            AppTileGroup(
                modifier = anchored(Anchor.ABOUT_GROUP),
                title = stringResource(Res.string.about),
                items = listOf(
                    AppTileItem(
                        title = edition.displayName(),
                        selected = highlight == Anchor.ABOUT,
                        subtitle = stringResource(Res.string.version_summary, appVersion),
                        leadingIcon = Lucide.Info
                    ),
                    // Tanzil's terms: name the source and link out so readers can check for text updates
                    AppTileItem(
                        title = stringResource(Res.string.quran_text_source),
                            selected = highlight == Anchor.TEXT_SOURCE,
                        subtitle = "Tanzil Project · tanzil.net",
                        leadingIcon = Lucide.BookOpen,
                        onClick = { uriHandler.openUri("https://tanzil.net") },
                    ),
                ),
            )
        }
    }

    if (sheet == Anchor.APPEARANCE) ThemePickerSheet(theme, onSelect = { SettingsStore.setTheme(it); close() }, onDismiss = ::close)
    if (sheet == Anchor.DATE_FORMAT) GregorianDateFormatPickerSheet(
        gregorianDateFormat,
        onSelect = { SettingsStore.setGregorianDateFormat(it); close() },
        onDismiss = ::close,
    )
    if (sheet == Anchor.HIJRI_DATE_FORMAT) HijriDateFormatPickerSheet(
        hijriDateFormat,
        hijriToday = hijri,
        onSelect = { SettingsStore.setHijriDateFormat(it); close() },
        onDismiss = ::close,
    )
    if (askingExemption) ExemptionStartSheet(
        onStart = exemption::start,
        onDismiss = { askingExemption = false },
    )

    if (sheet == Anchor.LANGUAGE) AppBottomSheet(onDismiss = ::close, title = stringResource(Res.string.language)) {
        AppTileGroup(
            items = Language.entries.map { lang ->
                AppTileItem(
                    title = lang.label,
                    // each name in the face it will actually be read in
                    titleFontFamily = lang.font,
                    selected = lang == language,
                    trailing = { if (lang == language) Icon(Lucide.Check, null, tint = AppTheme.colors.primary, modifier = Modifier.size(20.dp)) },
                    onClick = { SettingsStore.setLanguage(lang); close() },
                )
            },
        )
    }
}
