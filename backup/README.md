# backup

Snapshots of bundled database assets, taken before a change that rewrites them.

One folder per snapshot, named `<date>-<what-it-holds>`, each with its own README saying
what the files were, what replaced them, and why. Restore by copying the files back over
`shared/src/commonMain/composeResources/files/quran/`.

Tracked on purpose. `/dev/` is gitignored, so a backup left there is one machine crash
away from gone, and never reaches anyone else on the project.

Never keep a backup inside `composeResources`. Everything under that directory is bundled
into the app, so a stray copy ships to every user.

## Snapshots

- [2026-08-28-tanzil-uthmani-plus-indopak](2026-08-28-tanzil-uthmani-plus-indopak/) — the
  Tanzil Uthmani text, plus the first IndoPak import. Replaced by the earlier Simple
  Enhanced text.
- [2026-08-31-before-mqp-indopak-column](2026-08-31-before-mqp-indopak-column/) — quran.db
  before the `textIndopakMqp` trial column was filled from the Muslim & Quran Pro database
  (`mqp_quran.db` beside this folder, with their `mqp_noorehuda.ttf`).
