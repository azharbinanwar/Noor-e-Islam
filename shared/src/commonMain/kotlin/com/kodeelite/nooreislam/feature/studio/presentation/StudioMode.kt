package com.kodeelite.nooreislam.feature.studio.presentation

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.AlignLeft
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Layers
import com.composables.icons.lucide.LayoutTemplate
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Maximize
import com.composables.icons.lucide.Palette
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.TextQuote
import com.composables.icons.lucide.Type

// The studio's editing sections — drives the mode-chip footer and the panel shown for each.
// Label is plain for now; a localization pass comes later.
enum class StudioMode(val label: String, val icon: ImageVector) {
    Templates("Templates", Lucide.LayoutTemplate),
    Layout("Layout", Lucide.Maximize),
    BgImage("Backgrnd", Lucide.Image),
    BgGradient("Gradient", Lucide.Palette),
    Fonts("Fonts", Lucide.Type),
    TextSize("Size", Lucide.TextQuote),
    TextStyle("Style", Lucide.Pencil),
    Align("Align", Lucide.AlignLeft),
    Content("Content", Lucide.BookOpen),

    // TODO(studio): stickers not ready — re-enable when the sticker set + placement is done
    // Stickers("Stickers", Lucide.Stamp),
    Card("Card", Lucide.Layers),
    Effects("Effects", Lucide.Sparkles),
    Dates("Dates", Lucide.CalendarDays),

    // TODO(studio): presets (save/reset) not ready — re-enable when preset management is done
    // Presets("Presets", Lucide.Star),
    Branding("Details", Lucide.Info);

    companion object {
        val DEFAULT = Templates
    }
}
