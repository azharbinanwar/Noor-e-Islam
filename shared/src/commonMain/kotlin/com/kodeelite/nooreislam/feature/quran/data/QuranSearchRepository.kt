package com.kodeelite.nooreislam.feature.quran.data

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kodeelite.nooreislam.core.util.normalizeArabic
import com.kodeelite.nooreislam.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

// Reads quran_search.db — a separate file from quran.db on purpose: same `ayah` table shape (so the
// same Ayah model applies with zero changes), but `text` is pre-normalized at build time. quran.db
// itself is never touched.
//
// normalizeArabic runs over that text again on load. The build-time pass is older and narrower, and
// re-running is idempotent, so this is what actually guarantees the stored side and the query side
// fold identically — they call one function. Widening the rules never needs the asset regenerated.
//
// One row carries every script, because what a reader types is their own spelling and not whichever
// text happens to be on screen. The spellings genuinely differ after normalizing — Uthmani folds to
// صرط where Simple and Indopak keep صراط — so a single column would miss most of the Quran for
// anyone typing the other one. Search covers scripts the reader is not currently reading in, on
// purpose: results are looked up by id and drawn in whatever script is set.
object QuranSearchRepository {

    private const val DB_NAME = "quran_search.db"
    private const val DB_ASSET = "files/quran/quran_search.db"

    // read by index, so the order is fixed on purpose; the script columns trail so adding one is one entry
    private const val COLS = "id,surah,ayah,juz,endsRuku,text,textUthmani,textIndopak"
    private const val FIRST_SCRIPT = 5
    private val COL_COUNT = COLS.count { it == ',' } + 1

    // separates the scripts inside one searchable string. A query can never contain it: normalizeArabic
    // folds every whitespace run to a single space, so no match can straddle two scripts.
    private const val SCRIPT_SEP = '\n'

    private val lock = Mutex()
    private var conn: SQLiteConnection? = null
    private var cache: List<Ayah>? = null

    private suspend fun db(): SQLiteConnection = conn ?: run {
        val path = materializeDb(DB_NAME, Res.readBytes(DB_ASSET))
        BundledSQLiteDriver().open(path).also { conn = it }
    }

    /**
     * Every ayah, every script, already normalized — read once, then just an in-memory substring filter
     * per query. [Ayah.text] here is not one verse to show; it is every spelling of that verse, joined.
     */
    suspend fun all(): List<Ayah> = cache ?: withContext(Dispatchers.Default) {
        lock.withLock { cache ?: readAyahs(db()).also { cache = it } }
    }

    private fun readAyahs(c: SQLiteConnection): List<Ayah> {
        val st = c.prepare("SELECT $COLS FROM ayah ORDER BY id")
        try {
            val out = ArrayList<Ayah>(QuranRepository.TOTAL_AYAHS)
            while (st.step()) {
                val scripts = StringBuilder()
                for (i in FIRST_SCRIPT until COL_COUNT) {
                    if (st.isNull(i)) continue
                    if (scripts.isNotEmpty()) scripts.append(SCRIPT_SEP)
                    scripts.append(st.getText(i).normalizeArabic())
                }
                out += Ayah(
                    id = st.getLong(0).toInt(),
                    surah = st.getLong(1).toInt(),
                    ayah = st.getLong(2).toInt(),
                    text = scripts.toString(),
                    juz = st.getLong(3).toInt(),
                    endsRuku = st.getLong(4) != 0L,
                    sajda = null, // not needed for search; the real Ayah (with sajda) is fetched via QuranRepository on open
                )
            }
            return out
        } finally {
            st.close()
        }
    }
}
