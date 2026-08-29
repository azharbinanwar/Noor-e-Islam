package com.kodeelite.nooreislam.feature.studio.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.AlignLeft
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Layers
import com.composables.icons.lucide.LayoutTemplate
import com.composables.icons.lucide.ListOrdered
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Maximize
import com.composables.icons.lucide.Palette
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.TextQuote
import com.composables.icons.lucide.Type
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.mode_align
import com.kodeelite.nooreislam.resources.mode_background
import com.kodeelite.nooreislam.resources.mode_branding
import com.kodeelite.nooreislam.resources.mode_card
import com.kodeelite.nooreislam.resources.mode_content
import com.kodeelite.nooreislam.resources.mode_dates
import com.kodeelite.nooreislam.resources.mode_effects
import com.kodeelite.nooreislam.resources.mode_fonts
import com.kodeelite.nooreislam.resources.mode_gradient
import com.kodeelite.nooreislam.resources.mode_layout
import com.kodeelite.nooreislam.resources.mode_size
import com.kodeelite.nooreislam.resources.mode_style
import com.kodeelite.nooreislam.resources.mode_templates
import com.kodeelite.nooreislam.resources.surahs
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// The studio's editing sections — drives the mode-chip footer and the panel shown for each.
// Localized strings applied.
enum class StudioMode(val labelRes: StringResource, val icon: ImageVector) {
    Templates(Res.string.mode_templates, Lucide.LayoutTemplate),
    Verses(Res.string.surahs, Lucide.ListOrdered),
    Layout(Res.string.mode_layout, Lucide.Maximize),
    BgImage(Res.string.mode_background, Lucide.Image),
    BgGradient(Res.string.mode_gradient, Lucide.Palette),
    Fonts(Res.string.mode_fonts, Lucide.Type),
    TextSize(Res.string.mode_size, Lucide.TextQuote),
    TextStyle(Res.string.mode_style, Lucide.Pencil),
    Align(Res.string.mode_align, Lucide.AlignLeft),
    Content(Res.string.mode_content, Lucide.BookOpen),

    // TODO(studio): stickers not ready — re-enable when the sticker set + placement is done
    // Stickers("Stickers", Lucide.Stamp),
    Card(Res.string.mode_card, Lucide.Layers),
    Effects(Res.string.mode_effects, Lucide.Sparkles),
    Dates(Res.string.mode_dates, Lucide.CalendarDays),

    // TODO(studio): presets (save/reset) not ready — re-enable when preset management is done
    // Presets("Presets", Lucide.Star),
    Branding(Res.string.mode_branding, Lucide.Info);

    val label: String @Composable get() = stringResource(labelRes)

    companion object {
        val DEFAULT = Templates
    }
}
