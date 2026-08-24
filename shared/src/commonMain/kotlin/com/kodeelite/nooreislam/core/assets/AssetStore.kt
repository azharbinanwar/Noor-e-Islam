package com.kodeelite.nooreislam.core.assets

import com.kodeelite.nooreislam.core.constants.AssetDirs
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/**
 * The assets tree on disk — see dev/STUDIO_ASSETS.md. A file at its final path is a
 * complete download, because writes happen under a .part name and rename on success.
 */
object AssetStore {

    private val root: Path by lazy { Path(appStoragePath(), AssetDirs.ROOT) }

    fun file(dir: String, name: String): Path = Path(root, dir, name)

    fun exists(dir: String, name: String): Boolean = SystemFileSystem.exists(file(dir, name))

    /** Absolute path for image loaders and players. */
    fun pathOf(dir: String, name: String): String = file(dir, name).toString()

    fun delete(dir: String, name: String) {
        SystemFileSystem.delete(file(dir, name), mustExist = false)
    }

    /** Removes every file in [dir] the server no longer lists — parts of kept names survive. */
    fun cleanUp(dir: String, keep: Set<String>) {
        val folder = Path(root, dir)
        if (!SystemFileSystem.exists(folder)) return
        SystemFileSystem.list(folder).forEach { path ->
            val base = path.name.removeSuffix(AssetDirs.PART_SUFFIX)
            if (base !in keep) SystemFileSystem.delete(path, mustExist = false)
        }
    }
}
