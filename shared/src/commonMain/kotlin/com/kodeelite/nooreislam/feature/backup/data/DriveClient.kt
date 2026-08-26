package com.kodeelite.nooreislam.feature.backup.data

import com.kodeelite.nooreislam.core.network.ApiClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

/** Google Drive, app-data folder only: one file, found by name, replaced in place. */
class DriveClient(private val client: ApiClient) {

    @Serializable data class DriveFile(val id: String, val name: String, val size: String? = null, val modifiedTime: String? = null)
    @Serializable private data class FileList(val files: List<DriveFile> = emptyList())
    @Serializable private data class NewFile(val name: String, val parents: List<String>)

    class DriveException(message: String) : Exception(message)

    suspend fun find(token: String, name: String): DriveFile? {
        val r = client.http.get(FILES) {
            bearerAuth(token)
            parameter("spaces", "appDataFolder")
            parameter("q", "name = '$name' and trashed = false")
            parameter("fields", "files(id,name,size,modifiedTime)")
        }
        if (!r.status.isSuccess()) throw DriveException(reason(r.bodyAsText()))
        return r.body<FileList>().files.firstOrNull()
    }

    /** Creates or overwrites [name]; the id stays the same across backups. */
    suspend fun upload(token: String, name: String, bytes: ByteArray, onProgress: (Float) -> Unit): DriveFile {
        val existing = find(token, name)
        val id = existing?.id ?: run {
            val r = client.http.post(FILES) {
                bearerAuth(token); contentType(ContentType.Application.Json)
                setBody(NewFile(name, listOf("appDataFolder")))
            }
            if (!r.status.isSuccess()) throw DriveException(reason(r.bodyAsText()))
            r.body<DriveFile>().id
        }
        val r = client.http.patch("$UPLOAD/$id") {
            bearerAuth(token); parameter("uploadType", "media")
            contentType(ContentType.Application.Zip); setBody(bytes)
            onUpload { sent, total -> onProgress(if (total == null || total == 0L) 0f else sent.toFloat() / total) }
        }
        if (!r.status.isSuccess()) throw DriveException(reason(r.bodyAsText()))
        return r.body()
    }

    suspend fun download(token: String, id: String, onProgress: (Float) -> Unit): ByteArray {
        val r = client.http.get("$FILES/$id") {
            bearerAuth(token); parameter("alt", "media")
            onDownload { got, total -> onProgress(if (total == null || total == 0L) 0f else got.toFloat() / total) }
        }
        if (!r.status.isSuccess()) throw DriveException(reason(r.bodyAsText()))
        return r.body()
    }

    suspend fun delete(token: String, id: String) {
        val r = client.http.delete("$FILES/$id") { bearerAuth(token) }
        if (!r.status.isSuccess() && r.status.value != 404) throw DriveException(reason(r.bodyAsText()))
    }

    private fun reason(body: String): String =
        Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1) ?: "Google Drive refused the request"

    private companion object {
        const val FILES = "https://www.googleapis.com/drive/v3/files"
        const val UPLOAD = "https://www.googleapis.com/upload/drive/v3/files"
    }
}
