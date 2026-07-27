package com.kodeelite.nooreislam.feature.quran.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.Highlighter
import com.composables.icons.lucide.Layers
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.StickyNote
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.quran_tab_bookmarks
import com.kodeelite.nooreislam.resources.quran_tab_highlights
import com.kodeelite.nooreislam.resources.quran_tab_juz
import com.kodeelite.nooreislam.resources.quran_tab_notes
import com.kodeelite.nooreislam.resources.quran_tab_surah
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// index tabs — same shape as Miqat: icon + labelRes + a @Composable label
internal enum class QuranTab(val icon: ImageVector, val labelRes: StringResource) {
    Surahs(Lucide.BookOpen, Res.string.quran_tab_surah),
    Juzs(Lucide.Layers, Res.string.quran_tab_juz),
    Bookmarks(Lucide.Bookmark, Res.string.quran_tab_bookmarks),
    Notes(Lucide.StickyNote, Res.string.quran_tab_notes),
    Highlights(Lucide.Highlighter, Res.string.quran_tab_highlights);

    val label: String @Composable get() = stringResource(labelRes)
}
