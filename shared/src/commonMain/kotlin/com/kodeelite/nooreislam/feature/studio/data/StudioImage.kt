package com.kodeelite.nooreislam.feature.studio.data

import androidx.compose.ui.graphics.Color

/**
 * A studio background image: a stable [id], its [url] source, and two suggested-color lists sampled
 * from the photo — [colors] (base tones) and [onColors] (readable colors to sit on top). Pickers show
 * these so text / card colors stay matched to the image. When images become downloadable, add a
 * local-path field and let [ImageStore.resolve] prefer it — nothing in the UI or store changes.
 */
data class StudioImage(
    val id: String,
    val url: String,
    val colors: List<Color>,    // base tones, accent-first
    val onColors: List<Color>,  // readable colors for text over the image / card
) {
    val accent: Color get() = colors.first()
}

/**
 * Owns the background-image catalog and (later) the file lifecycle: local-check, download, manage.
 * The studio store never does this work — it asks ImageStore for images / a resolved source.
 */
object ImageStore {
    // Image shown when the studio opens with no explicit pick.
    const val DEFAULT_ID = "green_dome"

    val catalog: List<StudioImage> = listOf(
        StudioImage(
            "green_dome", "https://images.unsplash.com/photo-1724191078796-8a997b989f43?w=800&q=70&auto=format&fit=crop",
            colors = listOf(Color(0xFF1F6F4A), Color(0xFFAFC7DE), Color(0xFFC9A24B), Color(0xFF0F1B26), Color(0xFF1A2C3B)),
            onColors = listOf(Color(0xFFFFFFFF), Color(0xFF12232F), Color(0xFF241C08), Color(0xFFF2F0E8), Color(0xFFE7ECF2)),
        ),
        StudioImage(
            "gold_light", "https://images.unsplash.com/photo-1590075865003-e48277faa558?w=800&q=70&auto=format&fit=crop",
            colors = listOf(Color(0xFFC9A24B), Color(0xFF7FA8C9), Color(0xFF3E7C6F), Color(0xFFF1EDE3), Color(0xFFE4DCC8)),
            onColors = listOf(Color(0xFF241C08), Color(0xFF0E1E29), Color(0xFFFFFFFF), Color(0xFF1F3A4D), Color(0xFF2A2618)),
        ),
        StudioImage(
            "rose_dark", "https://images.unsplash.com/photo-1542816417-0983c9c9ad53?w=800&q=70&auto=format&fit=crop",
            colors = listOf(Color(0xFFC77B8B), Color(0xFF5B6478), Color(0xFF8B93A6), Color(0xFF14151A), Color(0xFF22242C)),
            onColors = listOf(Color(0xFFFFFFFF), Color(0xFFF1E9DE), Color(0xFF14151A), Color(0xFFF1E9DE), Color(0xFFE5DED4)),
        ),
        StudioImage(
            "forest", "https://images.unsplash.com/photo-1433086966358-54859d0ed716?w=800&q=70&auto=format&fit=crop",
            colors = listOf(Color(0xFF5C8A64), Color(0xFFA9C9AE), Color(0xFFDCE8DD), Color(0xFF0C1F16), Color(0xFF1F3B2C)),
            onColors = listOf(Color(0xFF0C1F16), Color(0xFF16281C), Color(0xFF1F3B2C), Color(0xFFF2F5EE), Color(0xFFE4EEE6)),
        ),
        StudioImage(
            "amber", "https://images.unsplash.com/photo-1620053580376-3de604e91953?w=800&q=70&auto=format&fit=crop",
            colors = listOf(Color(0xFFE8A33D), Color(0xFFC1502E), Color(0xFF7A2E1E), Color(0xFF1C1008), Color(0xFF3A2013)),
            onColors = listOf(Color(0xFF241606), Color(0xFFFDF1E3), Color(0xFFFBE4D6), Color(0xFFFDF1E3), Color(0xFFF5E3D2)),
        ),
        StudioImage(
            "blue_night", "https://images.unsplash.com/photo-1662770603921-f13c8d78bc17?w=800&q=70&auto=format&fit=crop",
            colors = listOf(Color(0xFF8CA9C9), Color(0xFF3A5578), Color(0xFFE7ECF5), Color(0xFF0C1220), Color(0xFF16233B)),
            onColors = listOf(Color(0xFF0C1524), Color(0xFFE7ECF5), Color(0xFF16233B), Color(0xFFE7ECF5), Color(0xFFDCE4F0)),
        ),
        StudioImage(
            "sage", "https://images.unsplash.com/photo-1536152470836-b943b246224c?w=800&q=70&auto=format&fit=crop",
            colors = listOf(Color(0xFF6E8C72), Color(0xFFE8DEB0), Color(0xFF9FB8A2), Color(0xFF0B1A1D), Color(0xFF14262A)),
            onColors = listOf(Color(0xFF0C1A0E), Color(0xFF2E2A14), Color(0xFF122016), Color(0xFFEFF3EF), Color(0xFFE3ECE4)),
        ),
        StudioImage(
            "teal", "https://plus.unsplash.com/premium_photo-1767721104598-d9c16da8b0a4?w=800&q=70&auto=format&fit=crop",
            colors = listOf(Color(0xFF3F5F66), Color(0xFF0D1F1A), Color(0xFF7FA0A6), Color(0xFF0E1B20), Color(0xFF1C3540)),
            onColors = listOf(Color(0xFFF0F4F2), Color(0xFFDCE8E4), Color(0xFF0A1618), Color(0xFFF0F4F2), Color(0xFFE4EDEC)),
        ),
    )

    val default: StudioImage get() = catalog.first { it.id == DEFAULT_ID }

    val urls: List<String> get() = catalog.map { it.url }

    fun byUrl(url: String?): StudioImage? = url?.let { u -> catalog.firstOrNull { it.url == u } }

    /** Source to load for [id] — today the remote url; later a local downloaded path if present. */
    fun resolve(id: String): String? = catalog.firstOrNull { it.id == id }?.url

    // File lifecycle — stubs for now; real expect/actual download + local storage land later.
    fun isDownloaded(id: String): Boolean = false
    // suspend fun download(id: String) { /* fetch bytes → save to app dir → mark downloaded */ }
}
