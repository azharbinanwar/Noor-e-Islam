# 2026-08-28 — Tanzil Uthmani, plus the first IndoPak import

`quran.db` and `quran_search.db` as they stood before being replaced by the earlier
Simple Enhanced text kept in `dev/quran-backup-20260820-224603/`.

## What is in these files

**Tanzil Uthmani** in the `text` column. This is the text swapped in on 2026-08-20. It
carries marks Simple Enhanced never had — the small high and low meems that notate iqlab
(U+06E2, U+06ED), the small waw, the rounded zeros — and alef wasla (ٱ) where the earlier
text has a plain alef. Only 164 of 6236 ayat are byte-identical between the two.

**A `textIndopak` column** on the `ayah` table of both files, all 6236 rows filled from
QUL's Indopak Nastaleeq script (resource 89, by Ayman Siddiqui and R. Siddiqua for
QuranWBW.com). On import the ayah-number glyphs (private use area) and the U+06DF end
circle were stripped, since the reader draws its own badge; waqf marks were kept.
`quran_search.db` holds the same text folded through `normalizeArabic`.

1518 of 6236 ayat differ between the two scripts even after normalization, because IndoPak
writes the alif out where Uthmani uses a superscript mark (صراط against صرط).

## Why it was replaced

Four of the five bundled fonts mis-place the marks the Tanzil Uthmani text introduced.
`tanzil_naskh` has no glyph for the meems or the small waw at all; `tanzil_hafs`,
`tanzil_me_quran` and `tanzil_scheherazade` have the glyphs but anchor them on top of the
letter. Only `tanzil_saleem` positions them properly. Easiest to see on 67:30.

Going back to the earlier text removes the mis-rendering while the IndoPak script work
continues on this branch. The Uthmani question is still open in `dev/ROADMAP.md`, under
"Fix how the Uthmani marks render": either ship a font built for full Uthmani (KFGQPC
Uthmanic Hafs, or Amiri Quran under OFL) and make it the default, or move to Tanzil's
`uthmani-min`, which drops the meems but also the maddah, the wasla alef and the shadda.

## Restoring

    cp backup/2026-08-28-tanzil-uthmani-plus-indopak/*.db \
       shared/src/commonMain/composeResources/files/quran/

The IndoPak column comes back with it. Nothing else in either file was ever touched.
