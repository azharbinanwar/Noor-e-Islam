# 2026-08-31 — quran.db before the MQP and Taj IndoPak columns

`quran.db` as it stood before two trial columns were added to the `ayah` table:
`textIndopakMqp`, then `textIndopakTaj`. `quran_search.db` was not touched, so it is
not snapshotted here.

## What changed after this snapshot

A new `textIndopakMqp` column, all 6236 rows filled verbatim from the Muslim & Quran Pro
app's own database (`backup/mqp_quran.db`, pulled from their APK). Nothing was cleaned on
import: their `**` line-split markers, the `۝۰` marker clusters and their private
font-cluster codes (ا+small-vowel standing for الله, U+FFC0/U+FFC1 letter clusters) are
all kept, because the text is written for their MQ font and only renders whole with it.

The MQP trial was judged and set aside the same day: its text is welded to the
proprietary MQ font (Muhammadi Enterprises) and carries `۝۰` sub-ayah circles no other
text uses. The decision landed on `textIndopakTaj` instead — QUL's Taj-style Indopak
script (resource 90, the same text quran.com's API serves as `text_indopak`), where every
waqf sign is seated on the word itself, which sidesteps the Android standalone-sign bug
by construction. `QuranRepository` reads it where it read `textIndopak`, and
`AyahTextRules.STRIP_INDOPAK_AYAH_MARKER` went false because this script's PUA chars are
waqf seats the font needs, not ayah numbers. Both old columns stay in place; revert is
the two SELECTs plus that flag.

A letters-only comparison of the two columns matched 6101 of 6236 ayat; the other 135
differ only inside those font-cluster codes. The side-by-side check lives at
https://claude.ai/code/artifact/22ac62ab-f1ca-4c85-b019-b8b16e1082b6

## Restoring

    cp backup/2026-08-31-before-mqp-indopak-column/quran.db \
       shared/src/commonMain/composeResources/files/quran/

Then point the two SELECTs in `QuranRepository` back at `textIndopak`.
