# Changelog

Both apps are built from one `shared/` module, so most work lands in both. Every
entry is tagged with the editions it actually ships to:

| Tag | Ships to |
|---|---|
| `[both]` | Noor e Islam and Noor e Quran |
| `[islam]` | Noor e Islam only, so prayer times, qibla, tasbih, focus, tracker, widgets |
| `[quran]` | Noor e Quran only |

The two apps have their own package IDs and version codes and do not release in
lockstep, so their Play listings get separate release notes. At release time,
copy only the lines tagged for that app. Play caps release notes at 500
characters, so keep what a user would notice and drop the rest.

An entry marked **Android** or **iOS** only affects that platform, so it stays out
of the other store's notes. Untagged entries apply to both.

Newest first. Dates are the release date, not the merge date.

## [Unreleased]

### Added

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

### Notes

- Reminder seeds are still the temporary hourly test list. Restore the real list and remove the permission readout per the `todo` markers in `NotificationDefaults.kt` before any store build.

## [1.0.0] - Noor e Quran

First release. Full Quran text with 14 reading themes, adjustable size, spacing
and script; auto-scroll with hold-to-pause; bookmarks, notes, highlights and
collections; resume, jump to any ayah and full-text search; share an ayah as an
image from Studio; reminders for Al-Mulk, Al-Kahf and daily reading; English and
Arabic with full RTL.

Full store copy lives in `dev/PLAY_STORE_LISTING.md`.
