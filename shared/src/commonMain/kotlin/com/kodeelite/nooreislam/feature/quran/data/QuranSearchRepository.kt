package com.kodeelite.nooreislam.feature.quran.data

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kodeelite.nooreislam.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

// Reads quran_search.db — a separate file from quran.db on purpose: same `ayah` table shape (so the
// same Ayah model applies with zero changes), but `text` is pre-normalized (harakat/tatweel stripped,
// alef variants unified) at build time. quran.db itself is never touched; if the normalization rules
// ever need fixing, only this file gets regenerated and replaced.
object QuranSearchRepository {

    private const val DB_NAME = "quran_search.db"
    private const val DB_ASSET = "files/quran/quran_search.db"
    private const val COLS = "id,surah,ayah,text,juz,endsRuku,sajda"

    private val lock = Mutex()
    private var conn: SQLiteConnection? = null
    private var cache: List<Ayah>? = null

    private suspend fun db(): SQLiteConnection = conn ?: run {
        val path = materializeDb(DB_NAME, Res.readBytes(DB_ASSET))
        BundledSQLiteDriver().open(path).also { conn = it }
    }

    /** Every ayah, text already normalized — read once, then just an in-memory substring filter per query. */
    suspend fun all(): List<Ayah> = cache ?: withContext(Dispatchers.Default) {
        lock.withLock { cache ?: readAyahs(db()).also { cache = it } }
    }

    private fun readAyahs(c: SQLiteConnection): List<Ayah> {
        val st = c.prepare("SELECT $COLS FROM ayah ORDER BY id")
        try {
            val out = ArrayList<Ayah>(QuranRepository.TOTAL_AYAHS)
            while (st.step()) {
                out += Ayah(
                    id = st.getLong(0).toInt(),
                    surah = st.getLong(1).toInt(),
                    ayah = st.getLong(2).toInt(),
                    text = st.getText(3),
                    juz = st.getLong(4).toInt(),
                    endsRuku = st.getLong(5) != 0L,
                    sajda = null, // not needed for search; the real Ayah (with sajda) is fetched via QuranRepository on open
                )
            }
            return out
        } finally {
            st.close()
        }
    }
}
