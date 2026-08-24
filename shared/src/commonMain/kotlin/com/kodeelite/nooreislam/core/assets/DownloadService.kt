package com.kodeelite.nooreislam.core.assets

import com.kodeelite.nooreislam.core.constants.AssetDirs
import com.kodeelite.nooreislam.core.network.ApiClient
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/**
 * Downloads any asset into the tree: stream to `{name}.part`, rename when complete, so
 * a file at its final path is always whole. One service for images, audio, whatever comes.
 * Progress is observable per file; work runs in the app scope and survives leaving the screen.
 */
class DownloadService(
    private val client: ApiClient,
    private val scope: CoroutineScope,
) {
    /** 0f..1f while downloading; absent when idle, done, or failed. */
    private val _progress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val progress: StateFlow<Map<String, Float>> = _progress.asStateFlow()

    private val jobs = mutableMapOf<String, Job>()

    fun isDownloading(dir: String, name: String): Boolean = key(dir, name) in _progress.value

    /** Idempotent: already downloaded or already in flight is a no-op. */
    fun download(url: String, dir: String, name: String) {
        val key = key(dir, name)
        if (AssetStore.exists(dir, name) || jobs[key]?.isActive == true) return

        set(key, 0f)
        jobs[key] = scope.launch {
            val target = AssetStore.file(dir, name)
            val part = Path(target.toString() + AssetDirs.PART_SUFFIX)
            try {
                target.parent?.let { SystemFileSystem.createDirectories(it, mustCreate = false) }
                client.http.prepareGet(url).execute { response ->
                    val total = response.contentLength() ?: 0L
                    val channel = response.bodyAsChannel()
                    SystemFileSystem.sink(part).buffered().use { sink ->
                        val buffer = ByteArray(64 * 1024)
                        var received = 0L
                        while (true) {
                            val read = channel.readAvailable(buffer, 0, buffer.size)
                            if (read == -1) break
                            if (read > 0) {
                                sink.write(buffer, 0, read)
                                received += read
                                if (total > 0) set(key, received.toFloat() / total)
                            }
                        }
                    }
                }
                SystemFileSystem.atomicMove(part, target)
            } catch (cancelled: CancellationException) {
                SystemFileSystem.delete(part, mustExist = false)
                throw cancelled
            } catch (_: Throwable) {
                // a failed download simply leaves no file — the icon stays, the user taps again
                SystemFileSystem.delete(part, mustExist = false)
            } finally {
                clear(key)
                jobs.remove(key)
            }
        }
    }

    private fun key(dir: String, name: String) = "$dir/$name"
    private fun set(key: String, value: Float) = _progress.update { it + (key to value) }
    private fun clear(key: String) = _progress.update { it - key }
}
