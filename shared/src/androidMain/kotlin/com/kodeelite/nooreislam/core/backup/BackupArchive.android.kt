package com.kodeelite.nooreislam.core.backup

import android.content.Context
import com.kodeelite.nooreislam.core.constants.AppConst
import com.kodeelite.nooreislam.core.constants.defaults.BackupDefaults
import com.kodeelite.nooreislam.core.database.AppDatabase
import com.kodeelite.nooreislam.core.datetime.Now
import com.kodeelite.nooreislam.core.platform.AppCtx
import com.kodeelite.nooreislam.core.platform.appVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import org.koin.core.context.GlobalContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@Serializable
private data class Manifest(val format: Int, val app: String, val appVersion: String, val dbVersion: Int, val createdAt: Long, val device: String)

actual object BackupArchive {
    private const val FORMAT = 1
    private const val MANIFEST = "manifest.json"
    private const val PREFS = "prefs.json"
    private val json = Json { ignoreUnknownKeys = true }

    // the database and its journal, copied as they lie: no checkpoint needed, SQLite folds the WAL in on open
    private fun dbFiles(ctx: Context): List<File> {
        val main = ctx.getDatabasePath(AppConst.DATABASE_NAME)
        return listOf(main, File(main.path + "-wal"), File(main.path + "-shm"))
    }

    // multiplatform-settings' no-arg factory writes the app's default SharedPreferences
    private fun prefs(ctx: Context) = ctx.getSharedPreferences(ctx.packageName + "_preferences", Context.MODE_PRIVATE)

    actual suspend fun create(): BackupFile = withContext(Dispatchers.IO) {
        val ctx = AppCtx.context
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            zip.putNextEntry(ZipEntry(MANIFEST))
            zip.write(json.encodeToString(Manifest(FORMAT, ctx.packageName, appVersion, AppConst.DB_VERSION, Now.epochMillis(), android.os.Build.MODEL)).encodeToByteArray())
            zip.closeEntry()
            zip.putNextEntry(ZipEntry(PREFS))
            val map = prefs(ctx).all.filterKeys { !BackupDefaults.excludesPref(it) }.mapValues { (_, v) ->
                when (v) {
                    is Boolean -> JsonPrimitive(v)
                    is Int -> JsonPrimitive(v)
                    is Long -> JsonPrimitive(v)
                    is Float -> JsonPrimitive(v)
                    else -> JsonPrimitive(v.toString())
                }
            }
            zip.write(JsonObject(map).toString().encodeToByteArray())
            zip.closeEntry()
            dbFiles(ctx).filter { it.exists() }.forEach { f ->
                zip.putNextEntry(ZipEntry("db/" + f.name)); f.inputStream().use { it.copyTo(zip) }; zip.closeEntry()
            }
        }
        BackupFile(out.toByteArray())
    }

    actual suspend fun restore(bytes: ByteArray): Unit = withContext(Dispatchers.IO) {
        val ctx = AppCtx.context
        val entries = mutableMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var e = zip.nextEntry
            while (e != null) { entries[e.name] = zip.readBytes(); e = zip.nextEntry }
        }
        val manifest = entries[MANIFEST]?.let { runCatching { json.decodeFromString<Manifest>(it.decodeToString()) }.getOrNull() }
            ?: throw BackupFormatException("not a Noor backup")
        if (manifest.dbVersion > AppConst.DB_VERSION) throw BackupFormatException("made by a newer version of the app")
        if (manifest.app.removeSuffix(".dev") != ctx.packageName.removeSuffix(".dev")) throw BackupFormatException("made by a different app")

        // close Room before swapping the files under it; the restart reopens it
        runCatching { GlobalContext.getOrNull()?.get<AppDatabase>()?.close() }
        dbFiles(ctx).forEach { it.delete() }
        entries.filterKeys { it.startsWith("db/") }.forEach { (name, data) ->
            File(ctx.getDatabasePath(AppConst.DATABASE_NAME).parentFile, name.removePrefix("db/")).writeBytes(data)
        }

        val restored = entries[PREFS]?.let { json.parseToJsonElement(it.decodeToString()) as? JsonObject } ?: JsonObject(emptyMap())
        // this phone's own device-state keys survive; everything else is replaced by the backup's
        val sp = prefs(ctx)
        val kept = sp.all.filterKeys { BackupDefaults.excludesPref(it) }
        sp.edit().clear().apply {
            kept.forEach { (k, v) ->
                when (v) { is Boolean -> putBoolean(k, v); is Int -> putInt(k, v); is Long -> putLong(k, v); is Float -> putFloat(k, v); is String -> putString(k, v); else -> Unit }
            }
            restored.filterKeys { !BackupDefaults.excludesPref(it) }.forEach { (k, v) ->
                val p = v.jsonPrimitive
                when {
                    p.isString -> putString(k, p.content)
                    p.booleanOrNull != null -> putBoolean(k, p.booleanOrNull!!)
                    p.intOrNull != null -> putInt(k, p.intOrNull!!)
                    p.longOrNull != null -> putLong(k, p.longOrNull!!)
                    else -> p.content.toFloatOrNull()?.let { putFloat(k, it) }
                }
            }
        }.commit()
        Unit
    }
}
