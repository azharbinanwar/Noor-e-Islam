# Noor-e-Islam

A Kotlin Multiplatform (Android + iOS) Islamic companion app — prayer times, Qibla direction, Quran reading with bookmarks/highlights/notes, duas & adhkar, a tasbih bead counter, prayer-focus (auto phone silencing), notifications/reminders, a Quran-verse image-poster studio, home-screen widgets, and prayer tracking.

UI is 100% Compose Multiplatform, shared across platforms from a single `shared` module. Platform-specific code (sensors, location, notifications, widgets, DB drivers) is isolated behind Kotlin `expect`/`actual` declarations.

## Tech stack

| Layer | Choice |
|---|---|
| Language | Kotlin 2.4.0 |
| UI | Compose Multiplatform 1.12.0-alpha02 |
| DI | Koin 4.1.0 |
| Persistence | Room 2.8.4 (SQLite), plus a bundled read-only `quran.db` asset |
| Networking | Ktor 3.5.1 (OkHttp on Android, Darwin on iOS) — currently minimal, most data is local |
| State | `StateFlow`-based "store" singletons per feature (no ViewModel layer; Koin holds them as `single`) |
| Serialization | `kotlinx.serialization` (routes, `StudioConfig`, prefs) |

## Project layout

```
androidApp/     Android entry point (MainActivity) — thin, delegates to shared UI
iosApp/         Xcode project + SwiftUI entry point (iOSApp.swift → MainViewController())
shared/
  src/commonMain/kotlin/com/kodeelite/nooreislam/
    App.kt              root composable — theme + AppNavHost
    config/theme/        AppColors, AppTheme, ThemeChoice (light/dark/system)
    core/                cross-feature infrastructure (see below)
    feature/<name>/      one folder per feature module
  src/androidMain/       Android actuals (sensors, Glance widgets, notification scheduling, DB driver)
  src/iosMain/            iOS actuals (CoreLocation heading, notifications, DB driver)
```

## Architecture patterns

- **DI (`core/di/DI.kt`)** — one Koin `appModule`; feature stores/repositories get registered as `single { }` as they're built. `initKoin()` / `startKoinForIos()` are the platform entry points. `databaseModule` + `platformDatabaseModule()` (expect/actual) wire up Room.
- **Navigation (`core/navigation/`)** — `AppRoute` is a `@Serializable sealed interface` (type-safe Navigation-Compose routes); `AppNavHost.kt` maps each route to a screen; `AppNavigator` is the injected nav handle screens call to push/pop.
- **State stores** — most features expose a `StateFlow`-based singleton store (e.g. `MiqatTimesStore`, `HighlightsStore`, `NotificationStore`) rather than a per-screen ViewModel. Stores either wrap a Room repository or a `PrefsService`-backed key/value setting, and screens `collectAsState()` them directly.
- **Persistence** — two tiers:
  - Room (`core/database/AppDatabase.kt`) for structured, growing data: bookmarks, highlights, notes, scheduled notifications, studio creations.
  - `PrefsService` (simple key/value) for settings/config: theme, calculation method, notification toggles, widget styles, Hijri offset.
  - The Quran text itself is a **read-only bundled SQLite asset** (`quran.db`, 6,236 ayahs), not a Room-managed table.
- **Platform boundary** — sensors (compass heading), GPS, notification scheduling, phone-silencing, widgets (Glance), and the SQLite driver bootstrap are all `expect`/`actual`. Android has real implementations for all of these; iOS coverage varies by feature (see notes below).
- **Design system (`core/components/`)** — shared building blocks used across every feature: `AppTile`/`AppTileGroup` (settings-style list rows), `AppButton`, `AppBottomSheet` (all in-app prompts use this, never raw dialogs), `AppCard`, `AppChip`, `AppDrawer`, `AppActionGroup`, `AppTextField`, `AppSwitch`, `StateView` (empty/error/loading placeholder — `StateView(title, message, icon, action)` for content states, `StateView.Loading(title?)` for a spinner), plus smaller pieces (`MiniStepper`, `PulseDot`, `SwapPill`, a `colorpicker/` HSV picker).

---

## Feature modules

### Prayer times, direction & discipline

#### `miqat` — prayer-time engine
The core of the app. `MiqatEngine` (domain) is a pure, stateless implementation of the PrayTimes solar-position algorithm (Julian date, solar declination, hour-angle trig) computing Fajr/Sunrise/Ishraq/Zawal/Dhuhr/Asr/Sunset/Maghrib/Isha/Midnight/LastThird/Imsak for a date + place + settings, including high-latitude rules (MiddleNight/SeventhNight/AngleBased). `MiqatTimesStore` is documented as the **only caller** of the engine — it combines calculation settings, active location, and the live clock into a `StateFlow<List<MiqatTime>>`, recomputes automatically, and rolls over at midnight. `MiqatCalculationStore` persists method/madhab/high-lat rule/custom angles/per-prayer minute offsets, defaulting the method by country code. `MiqatTimesScreen.kt` is the calendar-first UI (month grid + times for the selected day, with nested Midnight/Last Third under Isha, and a Sehri/Imsak info sheet gated to Ramadan). This store also underpins `focus` and `notifications` scheduling.

#### `qibla` — compass direction to Makkah
`QiblaScreen.kt` shows a live compass dial with bearing/distance computed by pure math in `domain/Qibla.kt` (`qiblaBearing`, haversine `distanceToMakkahKm`). `rememberHeading()` is an `expect` composable — Android reads the rotation-vector sensor, iOS reads `CLLocationManager` heading. Multiple dial visual styles (`QiblaDialClassical`, `QiblaDialModern`) are switchable via `QiblaStyleStore`/`QiblaStyleSheet`. `CompassCalibration.kt` shows an animated figure-8 (Gerono lemniscate) hint when sensor accuracy is low. All dial/calibration art is hand-drawn `Canvas`, no image assets.

#### `tracker` — prayer log / streaks
`TrackerScreen.kt`: a month calendar with per-day colored dots per prayer (on-time/jamaat/qaza/missed, cycled by tapping), plus streak/best/on-time% stats. `PrayerTrackingStore` (core) only persists **today's** statuses in a `StateFlow`; the calendar/history/stats on screen are local mock data (`mockStatus()`) — least-persisted feature in the app, flagged for a future Room-backed repository.

#### `focus` — auto phone silencing around prayers
Lets users configure per-prayer silence/vibrate windows (offset + duration + mode). `PrayerFocusStore` (core/store) persists `FocusConfig` per prayer (Fajr defaults to Vibrate, others Silent). `FocusWindows`/`FocusScheduling` (core/focus) turn configs into concrete time windows and reactively call `PhoneSilencer.rescheduleAll()` whenever prayer times or configs change. `PhoneSilencer` is `expect`/`actual`: Android has a real foreground service + broadcast receivers (`PhoneSilenceService`, `FocusReceivers`); iOS has a stub actual. Jumu'ah on Fridays suppresses the duplicate plain-Dhuhr window. Has a hidden dev test screen (7-tap gesture on the description text).

#### `notifications` — reminders & alerts
The most heavily engineered store in the app. `NotificationStore` persists settings for per-prayer alerts (remind-before/at-time/jamaat-after), Jumu'ah, Surah Al-Mulk, Al-Kahf, morning/evening dhikr, and nafil (Tahajjud/Ishraq) — each field written individually to `PrefsService`. `NotificationScheduler` reactively rebuilds on any change to today's prayer times or settings: expands settings into `NotificationEvent`s over a rolling horizon, caps at a fixed 63-slot budget, cancels + reschedules atomically under a `Mutex`, writes to the OS via `LocalNotifier` (expect/actual), and mirrors what was scheduled into a real Room table (`ScheduledNotificationEntity`/`ScheduledNotificationDao`) — the only Room-backed store found outside Quran/Studio. Has its own hidden dev test screen and test slots that bypass the master alert toggle.

#### `hijri` — Islamic calendar date
Thin feature: shows today's Hijri + Gregorian date with a manual ±2 day adjustment (`HijriCalendarScreen.kt`). No dedicated store — reads `Now.hijri(offset)` and writes the offset straight to `PrefsService`. The offset is global and affects the Hijri date shown app-wide, not just this screen.

### Quran

#### `quran` — reading, bookmarks, highlights, notes
The largest module. `QuranReaderScreen.kt` loads all 6,236 ayahs once into a `LazyColumn` grouped into ruku blocks, auto-jumps to the opened surah:ayah, and drives selection/highlighting/notes/reader-settings. `QuranIndexScreen.kt` hosts five tabs in a `HorizontalPager` (`SurahsTab`, `JuzsTab`, `BookmarksTab`, `HighlightsTab`, `NotesTab`) under a collapsing header.

Quran text itself comes from `QuranRepository`, which reads a bundled SQLite asset (`quran.db`) via the `androidx.sqlite` bundled driver (copied to a writable path first via an expect/actual `materializeDb`), exposing `surahs()` (114), `juzs()` (30, each with computed ayah range), `surah()`, `ayah()`.

Bookmarks, highlights, and notes are each a Room entity → DAO → repository → `StateFlow` store, all following the same soft-delete + `synced` flag shape (future server sync):
- `Bookmark` → `BookmarksDao` → `BookmarksRepository` → `BookmarksStore`
- `Highlight` (with a `HighlightColor` enum column, sticky "last used color") → `HighlightsDao` → `HighlightsRepository` → `HighlightsStore` (`StateFlow<Map<String, HighlightColor>>` for O(1) lookup while scrolling)
- `Note` → `NotesDao` → `NotesRepository` → `NotesStore`

Reader preferences (font size, script, theme, favorited surahs) live in `QuranStore`, backed by `PrefsService`.

**Custom text rendering (`AyahPassage.kt`)** — renders a whole ruku as one flowing, RTL, justified Arabic paragraph in a single `Text`. Selection/highlight tinting is baked directly into `SpanStyle(background = ...)` per-ayah character range rather than drawn afterward with `drawBehind`. This is deliberate: on Android, justified text and its `SpanStyle` backgrounds paint in the same layout pass, so the tint always lands exactly on the justified glyphs — a rectangle reconstructed after the fact from `TextLayoutResult` queries was found to drift on wrapped/justified lines after several attempts. The abandoned `drawBehind` approach is kept, clearly parked and unused, in `AyahPassageV2.kt` for reference. Hit-testing maps a tap to a text offset (`getOffsetForPosition`) against precomputed per-ayah ranges; bookmark/note markers are plain Unicode glyphs (`⚑`/`✎`, in `QuranSymbols.kt`) baked into the same text run, not `InlineTextContent` placeholders, to avoid BiDi run-boundary ambiguity.

### Duas & dhikr

#### `duas` — supplications catalog
`DuasScreen.kt` (single file) holds the hub (search, Dua-of-the-Day, section list), a session reader with a live completion ring, favoriting, and a way to add custom duas. `Dua`/`DuaSection` model a hardcoded in-file `CATALOG` (Morning/Evening/After-Prayer/Everyday/Tasbihat) — explicitly a mock dataset pending a real Hisnul-Muslim-style database. `DuaStore` (in-memory, no `StateFlow`) holds favorite IDs and user-added duas. Arabic is force-rendered RTL regardless of app locale. Exposes `AzkarCollectionReader`/`AzkarListReader` entry points reused by `azkar`.

#### `azkar` — remembrance hub & custom collections
`AzkarScreen.kt` is a browsing hub (tabs: All/Dua/Zikr/Tasbih, favorites, per-category previews); `CreateCollectionScreen.kt` lets users build a named collection with per-item rep counts. Reuses `duas`' `Dua` model and `DuaStore` rather than owning data — its own `UserCollections` (in-memory `mutableStateListOf`) is not yet persisted. Queues items into `TasbihRun` to hand off counting to the `tasbih` feature.

#### `tasbih` — bead counter
`TasbihHubScreen.kt` (catalog: categories, favorites, saved sets, multi-select set builder) → `TasbihScreen.kt` (counting screen with three swappable surfaces: `BeadCounter`, `TapCounter`, `FocusCounter`) → `TasbihHistoryScreen.kt` (past sessions/stats). `TasbihStore`/`TasbihRun` are in-memory objects, explicitly flagged (`ponytail:` comments) as pending a DB move; history is mock, not persisted across launches. `BeadCounter` is a fully custom `Canvas` bead ring — drag-to-count physics, hand-drawn 3D-shaded gradient/marble/star bead materials, no bitmap assets.

### Home & onboarding

#### `home` — landing screen
`HomeScreen.kt` (the shipping screen) composes a collapsing `PrayerSceneHeader` over `StreakCard`/`TodayPrayers`/`MulkReminderCard`/`DailyVerseCard`, with a GPS "did you move?" prompt (`LocationMoveSheet`) that only appears after travel is detected, never automatically. `MosqueScene.kt` computes and renders a live animated sky/mosque scene from time-of-day + prayer times. No dedicated store — reads `LocationStore`, `MiqatCalculationStore`, `MiqatTimesStore` directly. `HomeAltScreen.kt` and `PrayerAnimationScreen.kt` are design explorations with mock/static data, not wired into navigation.

#### `onboarding` — first-run flow
`OnboardingScreen.kt`: a 4-page `HorizontalPager` (Welcome, Madhab, Reminder pitch, Location pitch) with an onboarding-only color palette (explicitly not theme tokens). Completes by navigating to Home with `popUpTo(Onboarding) { inclusive = true }`. The madhab (Hanafi/Shafi) choice is local `remember` state only — not visibly wired to `MiqatCalculationStore.setMadhab` in this screen, worth checking if it's handled elsewhere.

### Studio

#### `studio` — verse-image poster creator
`StudioScreen.kt` is a full canvas editor: background (photo/gradient), font/size/style/alignment, card styling, effects (blur/vignette/overlay/duotone), branding (date stamps, watermark), aspect ratio; exports via `rememberGraphicsLayer()` capture to PNG for share/save. `StudioConfig` is one large `@Serializable` data class covering every editable property, persisted as JSON. `StudioCreation` (Room entity + DAO + repository) stores saved designs and a single resumable draft slot. `StudioStore` is a screen-scoped undo/redo history holder with debounced commits for continuous gestures (drag/pinch) vs. immediate commits for discrete actions.

### Settings & platform glue

#### `settings` — configuration hub
`SettingsScreen.kt` is the main list; drill-downs include `LocationScreen.kt` (GPS + offline ~49k-city catalog, nearest-city snap, calculation-method switch prompt on manual pick), `MiqatCalculationScreen.kt` (method/madhab/high-lat rule/angles/per-prayer adjustments), `WidgetGalleryScreen.kt` (browse/customize/add widgets), `ThemePickerSheet.kt`. Doesn't own data itself — reads/writes `SettingsStore`, `LocationStore`, `MiqatCalculationStore`, and per-widget `WidgetConfig`. City search and geocoding are fully offline.

#### `widget` — home-screen widgets
Not a user-navigated screen — the data/glue layer behind real home-screen widgets. `WidgetSnapshot` is a flat, pre-formatted "today" model (locale/clock-format/Arabic labels baked in ahead of time) written by `WidgetPublisher` whenever prayer times/format/language change, so the widget process itself does minimal work at draw time. `WidgetConfig` persists per-instance color/opacity style. Android has 7 concrete Glance widgets (Times/Bar/Card/Next/Tile/Icon/Minimal), an alarm receiver for scheduled refreshes, and a pin-request actual; iOS has a single stub actual pending real WidgetKit integration.

#### `sandbox` — internal design QA screen
Not user-facing. A single scrollable screen rendering every design-system component (buttons, tiles, action groups, highlight colors, tracker/miqat chips, color palette) in both light and dark theme side-by-side, for visual regression checking during development.

---

## Known gaps / mock data

Flagged here so it's clear what's real persistence vs. placeholder, per explicit `ponytail:` comments in the code:

- **`tracker`** — only today's status survives (`StateFlow`); calendar history and stats are mock (`mockStatus()`).
- **`tasbih`** — saved sets, favorites, and session history are in-memory only, reset on relaunch.
- **`duas`/`azkar`** — the dua catalog is a hardcoded in-file dataset (not a DB); user-built azkar collections are in-memory only.
- **`onboarding`** — madhab selection UI has no visible write to `MiqatCalculationStore` in the onboarding screen itself.
- **`widget`** — iOS widget support is a stub actual; Android is fully implemented.
- **`focus`** — iOS phone-silencing actual exists but Android is the more complete implementation (foreground service + receivers).

---

## Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar, or:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open [/iosApp](./iosApp) in Xcode and run from there.

### Running tests

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
