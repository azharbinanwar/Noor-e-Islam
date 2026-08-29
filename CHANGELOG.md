# Changelog

Both apps are built from one `shared/` module, so most work lands in both. Every
entry is tagged with the editions it actually ships to:

| Tag | Ships to |
|---|---|
| `[both]` | Noor e Islam and Noor e Quran |
| `[islam]` | Noor e Islam only, so prayer times, qibla, tasbih, focus, tracker, widgets |
| `[quran]` | Noor e Quran only |

The two apps have their own package IDs but share one version number, so a single
heading covers both. Their store listings still get separate release notes: at
release time copy only the lines tagged for that app. Play caps release notes at
500 characters, so keep what a user would notice and drop the rest.

An entry marked **Android** or **iOS** only affects that platform, so it stays out
of the other store's notes. Untagged entries apply to both.

Newest first. Dates are the release date, not the merge date.

## [Unreleased]

## [1.0.0+12] - 2026-08-29

### Added

- `[both]` The Quran can be read in IndoPak, the script the subcontinent learned on, chosen in the reading settings beside the fonts. A fresh install in Pakistan, India, Bangladesh and nearby opens in it; everyone else keeps the Arab-world script, and one tap switches. Search understands both spellings, so a verse typed either way is found.
- `[both]` The studio makes passages, not just single verses: a Surahs panel picks any surah — searchable by name in any spelling, number in any digits, or with a typo — and grows a range of up to ten ayat, each ending with its ornate number, the reference reading 95:1-4.
- `[both]` The studio opens on its own, from the drawer, a home shortcut, or the Quran page's quick actions, landing on a fresh random ayah each time for a daily post. Reset clears the canvas outright and the first tap anywhere starts the next design.
- `[both]` An ayah can be copied or shared as text: Copy puts the verse and its reference on the clipboard, Share as text opens the same review the image share gets — edit the message, then pick the app.
- `[both]` Settings grows Support and About: rate, share the app, contact with the version pre-filled, the website, each app's privacy policy, and a Credits sheet naming the projects behind the app — Tanzil, and the QuranWBW IndoPak text and font.
- `[islam]` Home shortcuts hold up to five, and swapping one keeps its place instead of sending the newcomer to the end.

### Fixed

- `[both]` **iOS.** The reader drew a different lam-alif than the studio in the same font: the theme's typography was leaking into the mushaf paragraph. The reader now passes its own style, and both screens agree.
- `[both]` **iOS.** An updated app kept reading the Quran database it was first installed with; the bundled file now replaces the old copy when it changes.
- `[both]` At-Tin and Al-Qadr kept their basmalah in the reader: Tanzil doubles the ب there after a tanween-ending surah, and the removal check missed the variant. All 112 now strip; the rule lives in one place and reaches bookmarks, previews and the studio too.
- `[both]` A waqf mark ending an ayah floated to the wrong side of the studio's canvas; the ayah now renders right-to-left like the reader's page.
- `[islam]` The day now turns over at Fajr rather than at midnight, which is when the prayer day actually ends. Isha prayed after 12 used to land on the wrong date, so it could not be marked, the streak reset while you were still praying the night before, and the tracker offered a new day that had not begun. Home and the tracker both show the day still open, with its own date in place of "Today", and everything logged goes to the day it belongs to.

### Changed

- `[islam]` Prayer rows on home no longer label the next prayer "soon" or "upcoming". The time is on the row and the prayer under way already pulses, so the word only repeated what both already said.

## [1.0.0+11] - 2026-08-26

### Added

- `[both]` **Android.** Every backup, including the first after linking, runs as the background job with its quiet notification. Restore ends on a Restored sheet with a Restart button instead of restarting on its own.
- `[both]` Google Drive backup can be found from Settings search and pinned as a home shortcut. A shortcut that only exists in development builds no longer holds a slot in the release app.
- `[both]` **Android.** Google Drive backup. Link a Google account from Settings and the app keeps one copy of your prayer tracker, bookmarks, notes, highlights, collections and settings in a private Drive folder only the app can see. Back up now, or let it run daily or weekly at a time you pick, over Wi‑Fi only if you prefer. On a new phone, linking the same account finds the backup and offers to restore it; restore replaces what is on the phone and restarts the app. The account sheet can switch accounts or delete the Drive copy. Runs as a quiet background job that shows a silent progress notification and tells you only when something needs a hand.
- `[both]` Studio backgrounds now come from our own server instead of shipping inside the app. The picker shows instant tiny previews with each photo's matched colors; tap the download icon and a progress ring fetches the full image once, into the app's own storage where gallery cleanups cannot touch it. Everything picked or defaulted works fully offline afterwards.
- `[both]` Each background that is not yet on the device shows its size on the download pill, so a 3 MB photo is a choice, not a surprise. View all opens a two-wide grid of larger tiles, big enough to actually judge a photo before fetching it.
- `[both]` When the studio has no backgrounds to show — a fresh install with no connection — the Background row says so and a tap retries, instead of sitting there empty.
- `[islam]` The app asks where you are on first launch and sets your real city, instead of quietly sitting on Makkah until you found the location screen yourself. If the answer is no, home shows one warning tile saying what it is missing — the device's location switch first, then the app's permission — and it disappears the moment either is fixed or a city is picked by hand.
- `[islam]` The home header's map pin becomes a small spinner while your location is being checked, so the city on screen is visibly being verified rather than silently assumed.
- `[both]` Reading text can be centred instead of justified, from a two-icon Alignment row in the reader's appearance sheet. Justify stays the default and matches a printed mushaf, but with no kashida it stretches word gaps wide at large text sizes, so centred is there for anyone who reads that way.
- `[islam]` **Android.** The notifications screen warns when the phone is set to restrict this app in the background, the state that stops reminders arriving. The Quran app already had this; the app whose alerts are time-critical did not.
- `[islam]` Prayer Focus explains what Do Not Disturb access is before handing you to system settings, and Silent is only applied once that access exists — picking it no longer leaves the setting saying Silent while the phone quietly vibrates instead.
- `[both]` The location screen warns when location is refused, instead of the request silently doing nothing.
- `[both]` The app now notices when location is switched off on the device, which is separate from the app being allowed to use it. Granting permission to an app that can still see nothing left "Use current location" doing nothing at all. On Android, tapping it offers the system's own "Turn on location?" prompt without leaving the app, falling back to the settings page where that prompt is unavailable.
- `[both]` Saving an ayah image says what happened: saved, couldn't save, or photo access declined. Declining used to stop the spinner and say nothing, so the button looked broken.

### Changed

- `[both]` The studio opens on a photo you have downloaded, or a curated gradient when there is none — never a black canvas, and never a silent full-size fetch over your data. Templates likewise only dress themselves in photos that are already on the device.
- `[both]` Hiding or deleting a background on the server removes its file from devices on their next sync, so retired art does not keep occupying storage.
- `[islam]` A place is named by the app's own geocoding service instead of a bundled list of 49,000 cities, which was 2.7 MB and returned misleading names. City search now needs a connection; the phone's own geocoder steps in when the service cannot answer. Names can be corrected server-side without an app update.
- `[islam]` Search results rank your own country's cities first — searching "Hyderabad" from Pakistan puts Hyderabad, Sindh above Hyderabad, India — while everywhere else stays in the list. On a fresh install the phone's region decides, so this works before any city is saved.
- `[islam]` Prayer times' clock timezone comes from the device rather than a city table. The prayer calculation itself never used it — it works from coordinates — so times are unchanged; only the label source moved.
- `[both]` Every permission warning now appears together in one "Needs attention" card, coloured by how much it matters: red for what the feature cannot work without, amber for what keeps it reliable. Previously each warning was its own card with its own heading, and each was discovered only after fixing the last one.
- `[islam]` **Android.** Prayer Focus no longer runs a background service. The phone is muted by an exact alarm at the prayer, the pinned notification with +5 min, Prayed and Unmute is a plain notification the system holds on its own, and a second exact alarm restores the sound. Fewer permissions, and nothing for aggressive phones to kill mid-window.
- `[islam]` Prayer Focus lists its prayers the way the notification screen does — one row each, with the window behind an options sheet, rather than six separate cards with their settings inline.
- `[both]` The reader's alignment picker and the focus silence picker are the same control, so they look and behave alike.
- `[both]` Holding a home shortcut opens one sheet for the whole row: your current shortcuts sit on top, every feature below, and you tap to add or remove until you have the two to four you want. Done keeps it, Cancel forgets it. Before, a hold only swapped that one slot.
- `[islam]` The moon brightens as it fills: pure white when full, only a touch softer as a crescent, instead of a thin moon looking half switched off.
- `[islam]` The sun and moon on the home header follow the prayer times instead of a sunrise-to-sunset curve. Each prayer pins its body to a fixed point on one closed loop, and the clock only decides how fast the gap between two points is crossed, so Maghrib looks the same in a nine-hour December day as in a sixteen-hour June one, anywhere in the world. The loop closes, so nothing snaps at the ends of the day.

### Fixed

- `[both]` Background previews were blank on the live server: the host's bot check turned away the image library's own connection. Images now travel on the app's usual connection, and the catalog names each file by its full address.
- `[islam]` **iOS.** "Use my location" never asked for permission, so on a fresh install it waited forever on an answer iOS was never going to send. It now asks, and gives up after twelve seconds instead of hanging.
- `[islam]` **Android.** A location fix from days ago is no longer trusted as where you are: anything older than five minutes triggers a live request, and the stale fix is only a last resort. Travellers stopped seeing yesterday's city.
- `[both]` A database written by a newer build no longer crashes the app on every launch. It is moved aside, the app starts fresh and says so — the same recovery a damaged file already had. A missing migration between known versions is still a hard failure, so a bad release cannot quietly wipe anyone's notes.
- `[islam]` The prayer times screen no longer offers a settings button that did nothing.
- `[both]` The location permission message names the app you are actually running rather than always saying Noor e Islam.
- `[islam]` The sun sets at Maghrib. It used to hang above the ridge for the better part of an hour afterwards: the old curve put it at the horizon line rather than under it, and what read as a lingering sun was a hard-edged glow ring drawn well after the disc should have gone. The disc now exists only between the two points where the peaks cut the loop, and below them a soft wash is all that remains, lighting Fajr and Maghrib without ever showing a sun.

## [1.0.0+6] - 2026-08-19

### Added

- `[both]` The app offers an update when the store has a newer build, once per launch, in a sheet that dismisses as "not now". Android hands over to Play's own update screen; iOS asks the App Store lookup endpoint and opens the listing, since Apple ships no update API. Each app checks its own package, so the two editions never see each other's releases. *(Play only reports updates for builds it installed, so this stays quiet on sideloaded and dev builds — test it through Internal App Sharing.)*
- `[both]` Urdu and French, bringing the app to four languages. Each language names its own typeface on its `Language` entry, so Arabic and Urdu render in IBM Plex Sans Arabic while English and French use Poppins, and a future language only adds a line.
- `[quran]` Search shows how many results it found, above the list and updating as you type.
- `[quran]` Matched words are tinted in each search result. Matching is word-level, since the search text is normalised and the displayed text keeps its tashkeel, so character offsets would not line up.

### Changed

- `[quran]` Search finds an ayah whatever keyboard typed the query. Arabic-script letters that look alike but differ in Unicode — Urdu ہ ی ک against Arabic ه ي ك — now fold to one shape, along with tashkeel, tatweel and the zero-width joiners Urdu and Persian keyboards insert invisibly. Both the stored text and the query pass through the same function, so they cannot drift.
- `[quran]` Surah names match however they are spelled, typos included. Baqarah, Baqara, Bakara and baqar all reach Al-Baqara. Hits rank by tier — exact, then variant or prefix, then one or two edits — so a typo never outranks a real match.
- `[quran]` Every search result is reachable. The list is lazy and the 50-result cap is gone, so a query with 500 matches scrolls through all 500.
- `[quran]` Opening an ayah puts that ayah at the top of the screen, rather than in the middle, at the bottom, or off screen when it sits near the end of a long ruku. A surah's opening ayahs land against the top, where the ornate name and bismillah give them room; every other ayah sits a little below it.
- `[both]` Debug builds install as `.dev` with their own icon slot, replacing `.debug`.

### Fixed

- `[quran]` The Quran's closing ayahs can be read. The reader reserved space above its text but none below, so the last ayah had nothing to scroll into and the quick actions covered it when tapped.

- `[both]` **Android.** The Tasbih counter no longer crashes in Arabic. Its round-and-total line declared integer placeholders while the screen passes formatted strings, which threw on every render of that screen in Arabic.
- `[both]` Arabic is complete and consistent with the other languages: two missing strings translated, six strings with no English counterpart and no use in code removed.
- `[quran]` Reminders fire at the time they are set for. A cold start now rebuilds the schedule from the database instead of an unhydrated flow, seeding is serialised so it cannot double-write, and opening the app refills the scheduling window. *(Android also holds the broadcast open until the rebuild finishes.)*
- `[quran]` **Android.** Granting exact alarms re-arms reminders that were already armed inexactly, rather than leaving them imprecise until something else triggered a reschedule.
- `[both]` **Android.** The exact-alarm warning banner no longer reports the permission as granted when it is not. A permission with no runtime dialog now returns a real denial instead of an optimistic one.
- `[quran]` **Android.** The needs-attention banner keys on background restriction, which is the flag Funtouch actually exposes, so Vivo devices get a prompt that can be acted on instead of one that leads nowhere.
- `[quran]` **Android.** Surah and daily-reading reminders post on a channel that exists. The Quran edition registers only its own channels, deletes stale ones, and no longer routes unmapped reminders to a missing channel.

## [1.0.0] - Noor e Quran

First release. Full Quran text with 14 reading themes, adjustable size, spacing
and script; auto-scroll with hold-to-pause; bookmarks, notes, highlights and
collections; resume, jump to any ayah and full-text search; share an ayah as an
image from Studio; reminders for Al-Mulk, Al-Kahf and daily reading; English and
Arabic with full RTL.

Full store copy lives in `dev/PLAY_STORE_LISTING.md`.
