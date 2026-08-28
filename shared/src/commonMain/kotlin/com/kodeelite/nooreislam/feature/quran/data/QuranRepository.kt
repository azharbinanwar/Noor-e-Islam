package com.kodeelite.nooreislam.feature.quran.data

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kodeelite.nooreislam.core.util.NameMatch
import com.kodeelite.nooreislam.core.util.fromArabicIndicDigits
import com.kodeelite.nooreislam.core.util.latinKeys
import com.kodeelite.nooreislam.core.util.nameMatch
import com.kodeelite.nooreislam.core.util.normalizeArabic
import com.kodeelite.nooreislam.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

// Reads quran.db and serves verses/surahs. The db is a faithful copy of each source; AyahTextRules
// is applied here, once, so every consumer downstream gets clean text and no one cleans their own.
object QuranRepository {

    const val TOTAL_AYAHS = 6236

    private const val DB_NAME = "quran.db"
    private const val DB_ASSET = "files/quran/quran.db"

    // read by index, so the column order here is fixed on purpose (kept explicit for stable positions).
    // Every spelling comes back in the row: switching script is then a render-time choice, and no
    // screen has to notice, re-read or invalidate anything.
    private const val COLS = "id,surah,ayah,text,textIndopak,juz,endsRuku,sajda"

    private val lock = Mutex()
    private var conn: SQLiteConnection? = null
    private var surahCache: List<Surah>? = null
    private var juzCache: List<Juz>? = null

    private suspend fun db(): SQLiteConnection = conn ?: run {
        val path = materializeDb(DB_NAME, Res.readBytes(DB_ASSET))
        BundledSQLiteDriver().open(path).also { conn = it }
    }

    /** The whole Quran in order — one read off the main thread; the reader scrolls freely and jumps anywhere. */
    suspend fun all(): List<Ayah> = withContext(Dispatchers.Default) {
        lock.withLock { readAyahs(db(), "SELECT $COLS FROM ayah ORDER BY id") }
    }

    /** One full surah's verses. */
    suspend fun surah(number: Int): List<Ayah> = lock.withLock {
        readAyahs(db(), "SELECT $COLS FROM ayah WHERE surah = ? ORDER BY id", number.toLong())
    }

    /** A single ayah by its canonical ref (for jumps / deep links / bookmarks). */
    suspend fun ayah(surah: Int, ayah: Int): Ayah? = lock.withLock {
        readAyahs(db(), "SELECT $COLS FROM ayah WHERE surah = ? AND ayah = ?", surah.toLong(), ayah.toLong()).firstOrNull()
    }

    /** The 114-row surah table (names, counts, revelation) — read once, for the header and picker. */
    suspend fun surahs(): List<Surah> = surahCache ?: lock.withLock {
        surahCache ?: readSurahs(db()).also { surahCache = it }
    }

    /**
     * Surahs matching what someone typed: a number in any digits, or a name in any of the spellings
     * people actually use — transliterated with or without its article, English, Arabic, and one or
     * two typos off. Ranked so an exact spelling always beats a fuzzy one. Blank returns everything.
     */
    suspend fun findSurahs(query: String): List<Surah> {
        val all = surahs()
        val q = query.trim().fromArabicIndicDigits()
        if (q.isEmpty()) return all
        q.toIntOrNull()?.let { n -> return all.filter { it.number.toString().startsWith(n.toString()) } }

        val keys = latinKeys(q)
        // Latin letters pass through normalizeArabic untouched, so the Arabic side only exists
        // when the query actually carries Arabic — otherwise "surah" pretends to be an Arabic name
        val arabic = if (q.any { it in '؀'..'ۿ' }) q.normalizeArabic().removePrefix("سوره").trim() else ""
        // "surah" alone folds away entirely — nothing left to filter by is not the same as no hits
        if (keys.isEmpty() && arabic.isEmpty()) return all
        val noMatch = NameMatch.entries.size
        return all.mapNotNull { s ->
            val latin = minOf(
                nameMatch(keys, s.nameTransliterated)?.ordinal ?: noMatch,
                nameMatch(keys, s.nameEnglish)?.ordinal ?: noMatch,
            )
            val tier = when {
                latin < noMatch -> latin
                arabic.isNotEmpty() && s.nameArabic.normalizeArabic().contains(arabic) -> NameMatch.PARTIAL.ordinal
                else -> return@mapNotNull null
            }
            s to tier
        }.sortedWith(compareBy({ it.second }, { it.first.number })).map { it.first }
    }

    /** The 30 juz, each with the ayah it starts at and the surahs it spans — for the juz list. */
    suspend fun juzs(): List<Juz> = juzCache ?: lock.withLock {
        juzCache ?: run {
            val c = db()
            val surahList = surahCache ?: readSurahs(c).also { surahCache = it }
            readJuzs(c, surahList).also { juzCache = it }
        }
    }

    // ── internals ──────────────────────────────────────────────────────────

    private fun readAyahs(c: SQLiteConnection, sql: String, vararg args: Long): List<Ayah> {
        val st = c.prepare(sql)
        try {
            args.forEachIndexed { i, v -> st.bindLong(i + 1, v) }
            val out = ArrayList<Ayah>()
            while (st.step()) {
                val surah = st.getLong(1).toInt()
                val ayah = st.getLong(2).toInt()
                out += Ayah(
                    id = st.getLong(0).toInt(),
                    surah = surah,
                    ayah = ayah,
                    textTanzil = AyahTextRules.cleanTanzil(st.getText(3), surah, ayah),
                    textIndopak = AyahTextRules.cleanIndopak(st.getText(4)),
                    juz = st.getLong(5).toInt(),
                    endsRuku = st.getLong(6) != 0L,
                    sajda = if (st.isNull(7)) null else sajdaOf(st.getText(7)),
                )
            }
            return out
        } finally {
            st.close()
        }
    }

    private fun readSurahs(c: SQLiteConnection): List<Surah> {
        val st =
            c.prepare("SELECT number,nameArabic,nameTransliterated,nameEnglish,ayahCount,rukuCount,revelation,revelationOrder,startId FROM surah ORDER BY number")
        try {
            val out = ArrayList<Surah>(114)
            while (st.step()) {
                out += Surah(
                    number = st.getLong(0).toInt(),
                    nameArabic = st.getText(1),
                    nameTransliterated = st.getText(2),
                    nameEnglish = st.getText(3),
                    ayahCount = st.getLong(4).toInt(),
                    rukuCount = st.getLong(5).toInt(),
                    revelation = if (st.getText(6) == "Medinan") Revelation.Madinah else Revelation.Makkah,
                    revelationOrder = st.getLong(7).toInt(),
                    startId = st.getLong(8).toInt(),
                )
            }
            return out
        } finally {
            st.close()
        }
    }

    private fun readJuzs(c: SQLiteConnection, surahs: List<Surah>): List<Juz> {
        val st =
            c.prepare("SELECT j.number, a.id, a.surah, a.ayah, a.text, a.textIndopak, a.juz, a.endsRuku, a.sajda FROM juz j JOIN ayah a ON a.id = j.startId ORDER BY j.number")
        val starts = ArrayList<Pair<Int, Ayah>>(30)
        try {
            while (st.step()) {
                val surah = st.getLong(2).toInt()
                val ayah = st.getLong(3).toInt()
                starts += st.getLong(0).toInt() to Ayah(
                    id = st.getLong(1).toInt(),
                    surah = surah,
                    ayah = ayah,
                    textTanzil = AyahTextRules.cleanTanzil(st.getText(4), surah, ayah),
                    textIndopak = AyahTextRules.cleanIndopak(st.getText(5)),
                    juz = st.getLong(6).toInt(),
                    endsRuku = st.getLong(7) != 0L,
                    sajda = if (st.isNull(8)) null else sajdaOf(st.getText(8)),
                )
            }
        } finally {
            st.close()
        }
        // each juz spans up to the ayah before the next juz; the last runs to the end (surah ranges overlap-tested)
        return starts.mapIndexed { i, (number, startsAt) ->
            val end = starts.getOrNull(i + 1)?.second?.id?.minus(1) ?: TOTAL_AYAHS
            val spanned = surahs.filter { it.startId <= end && it.startId + it.ayahCount - 1 >= startsAt.id }
            Juz(number, startsAt, spanned)
        }
    }

    private fun sajdaOf(t: String): Sajda = if (t == "obligatory") Sajda.Obligatory else Sajda.Recommended
}
