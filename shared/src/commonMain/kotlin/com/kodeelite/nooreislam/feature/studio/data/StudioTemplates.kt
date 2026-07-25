package com.kodeelite.nooreislam.feature.studio.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.random.Random

/**
 * Curated one-tap looks for the share studio. A template applies onto the current config, keeping the
 * ayahs — the user gets a finished design to tweak, not a blank canvas. Each look is a different mix of
 * background (photo / gradient / solid), an optional blur, a card (none / translucent / solid, any color),
 * and a matching accent.
 */
data class StudioTemplate(
    val name: String,
    val bg: List<Color>,               // solid/gradient background; also the base tone under a photo
    val textColor: Color,
    val accent: Color,                 // emphasized words + surah glyph
    val imageUrl: String? = null,      // photo background (scrimmed for legibility)
    val card: Color = Color.Transparent, // plate behind the text — full color incl. alpha (Transparent = none)
    val blur: Float = 0f,              // 0 = sharp
    val overlay: Float = 0.2f,         // dark scrim over the photo
    val vignette: Float = 0f,
) {
    fun applyTo(c: StudioConfig): StudioConfig = c.copy(
        showBismillah = true,   // every template shows the bismillah
        bgColor = bg.first(),
        bgGradient = if (imageUrl == null && bg.size > 1) StudioGradient(bg) else null,
        bgImageUrl = imageUrl,
        isDuotone = false,
        pattern = CanvasPattern.None,
        textColor = textColor,
        emphasisColor = accent,
        cardColor = card,
        blurRadius = blur,
        overlayAlpha = if (imageUrl != null) overlay else c.overlayAlpha,
        vignetteIntensity = vignette,
    )
}

// hand-picked looks (varied on purpose: blurred + sharp, black + colored + no card).
// Photo looks reuse ImageStore catalog images so they carry palette metadata (no off-catalog URLs).
private val CURATED = listOf(
    StudioTemplate(
        "Green Dome",
        listOf(Color(0xFF0A1A12)),
        Color.White,
        Color(0xFFF2C14E),
        imageUrl = ImageStore.resolve("green_dome"),
        card = Color.Black.copy(alpha = 0.38f),
        blur = 12f,
        overlay = 0.35f
    ),
    StudioTemplate(
        "Night Peaks",
        listOf(Color(0xFF0B0E14)),
        Color.White,
        Color(0xFFC9B8FF),
        imageUrl = ImageStore.resolve("blue_night"),
        card = Color(0xFF1A1636).copy(alpha = 0.6f),
        blur = 20f,
        overlay = 0.45f,
        vignette = 0.30f
    ),
    StudioTemplate(
        "Golden Hour",
        listOf(Color(0xFF2A1A0E)),
        Color.White,
        Color(0xFFFFD98A),
        imageUrl = ImageStore.resolve("amber"),
        card = Color.Transparent,
        overlay = 0.45f
    ), // sharp, no card, strong scrim
    StudioTemplate(
        "Serene",
        listOf(Color(0xFF10161C)),
        Color.White,
        Color(0xFFA9E5D6),
        imageUrl = ImageStore.resolve("sage"),
        card = Color.Black.copy(alpha = 0.40f),
        blur = 16f,
        overlay = 0.40f
    ),
    StudioTemplate(
        "Sanctuary",
        listOf(Color(0xFF14110A)),
        Color.White,
        Color(0xFFF2C14E),
        imageUrl = ImageStore.resolve("gold_light"),
        card = Color(0xFF241A10).copy(alpha = 0.62f),
        overlay = 0.42f
    ), // sharp, brown plate
    StudioTemplate("Midnight", listOf(Color(0xFF0E1116)), Color.White, Color(0xFFFBC02D), card = Color(0xFF101318)), // solid ink card
    StudioTemplate(
        "Emerald",
        listOf(Color(0xFF0E4D3A), Color(0xFF06251C)),
        Color.White,
        Color(0xFFF2C14E),
        card = Color.Black.copy(alpha = 0.28f),
        vignette = 0.30f
    ),
    StudioTemplate("Parchment", listOf(Color(0xFFF1E4C9)), Color(0xFF4A3A20), Color(0xFF1E6B49)), // no card
    StudioTemplate("Ink", listOf(Color(0xFFFAFAF7)), Color(0xFF1A1A1A), Color(0xFF1E6B49)),        // no card
)

private val NAMES = listOf(
    "Dusk", "Oasis", "Lantern", "Tranquil", "Aurora", "Sable", "Meadow", "Ember", "Horizon", "Mirage", "Solace", "Zephyr",
    "Verdant", "Nocturne", "Dune", "Halcyon", "Cove", "Ridge", "Vellum", "Amber",
)

// A fresh batch of good-looking looks: each is built from a real palette — a catalog photo or a freshly
// generated tasteful gradient — so accent + text always match the background. Randomized (blur, card, which
// palette color) for variety, seeded so "more" reshuffles. Never a random/odd combination.
fun generatedTemplates(count: Int = 10, seed: Int = 7, nameStart: Int = 0): List<StudioTemplate> {
    val rng = Random(seed)
    val gradients = GradientStore.generate(count = count, seed = seed + 101)   // fresh tasteful gradients
    return List(count) { i ->
        val name = NAMES[(nameStart + i) % NAMES.size]
        if (rng.nextBoolean()) {
            // photo look — accent from the image's vivid tones, varied card + optional blur
            val img = ImageStore.catalog.random(rng)
            val darkTone = img.colors.getOrNull(3) ?: Color.Black
            val card = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f), darkTone.copy(alpha = 0.6f)).random(rng)
            val blurred = rng.nextFloat() < 0.4f
            StudioTemplate(
                name = name,
                bg = listOf(Color.Black),
                textColor = Color.White,
                accent = img.colors.take(3).random(rng),        // primary/secondary/tertiary — the vivid ones
                imageUrl = img.url,
                card = card,
                blur = if (blurred) 8f + rng.nextInt(12) else 0f,
                overlay = if (card == Color.Transparent) 0.45f else 0.30f + rng.nextFloat() * 0.10f,
                vignette = if (rng.nextBoolean()) 0.30f else 0f,
            )
        } else {
            // gradient look — fresh tasteful gradient; its on-color is light so it reads card-less on the dark stops
            val g = gradients[i]
            StudioTemplate(
                name = name,
                bg = g.stops,
                textColor = g.onColors.firstOrNull() ?: Color.White,
                accent = g.colors.firstOrNull() ?: Color.White,
                card = if (rng.nextBoolean()) Color.Transparent else Color.Black.copy(alpha = 0.24f),
                overlay = 0f,
                vignette = if (rng.nextBoolean()) 0.30f else 0f,
            )
        }
    }
}

// ── default presets: one per catalog image + one per gradient, colors taken from each palette ──
// every default is a good combination (readable text + matched accent), never a random mix.
private fun imageTemplate(name: String, img: StudioImage, i: Int): StudioTemplate {
    val darkTone = img.colors.getOrNull(3) ?: Color.Black    // a dark tone from the image's palette
    val card = when (i % 3) {                                 // vary the plate so looks aren't identical
        0 -> Color.Black.copy(alpha = 0.35f)
        1 -> Color.Transparent                               // no card, rely on the scrim
        else -> darkTone.copy(alpha = 0.6f)
    }
    val blurred = i % 2 == 1                                  // alternate sharp / blurred
    return StudioTemplate(
        name = name,
        bg = listOf(Color.Black),
        textColor = Color.White,                             // scrimmed photo → light text stays legible
        accent = img.colors.firstOrNull() ?: Color(0xFFF2C14E),
        imageUrl = img.url,
        card = card,
        blur = if (blurred) 14f else 0f,
        overlay = if (card == Color.Transparent) 0.45f else 0.32f,
        vignette = 0.2f,
    )
}

private fun gradientTemplate(name: String, g: StudioGradient): StudioTemplate {
    val text = g.onColors.firstOrNull() ?: Color.White
    // gradients run light→dark, so a single text color blends on one end — a contrasting plate fixes it
    val card = if (text.luminance() > 0.5f) Color.Black.copy(alpha = 0.26f) else Color.White.copy(alpha = 0.32f)
    return StudioTemplate(
        name = name,
        bg = g.stops,
        textColor = text,                                    // the gradient's own readable on-color
        accent = g.colors.firstOrNull() ?: Color.White,
        card = card,
        overlay = 0f,
        vignette = 0.2f,
    )
}

val STUDIO_TEMPLATES: List<StudioTemplate> =
    ImageStore.catalog.mapIndexed { i, img -> imageTemplate(NAMES[i % NAMES.size], img, i) } +
            GradientStore.presets.mapIndexed { i, g -> gradientTemplate(NAMES[(i + ImageStore.catalog.size) % NAMES.size], g) } +
            CURATED   // hand-tuned blurred/sharp variants kept for variety
