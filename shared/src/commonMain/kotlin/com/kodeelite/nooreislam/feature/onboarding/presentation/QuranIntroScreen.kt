package com.kodeelite.nooreislam.feature.onboarding.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.BellRing
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Highlighter
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MoonStar
import com.composables.icons.lucide.Palette
import com.composables.icons.lucide.Type
import com.kodeelite.nooreislam.config.theme.lightAppColors
import com.kodeelite.nooreislam.core.locale.tr
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.noor_e_quran_logo
import com.kodeelite.nooreislam.resources.a_calm_place_to_read
import com.kodeelite.nooreislam.resources.al_mulk_al_kahf_and_any_surah_you_choose
import com.kodeelite.nooreislam.resources.app_name_quran
import com.kodeelite.nooreislam.resources.bookmark_any_ayah
import com.kodeelite.nooreislam.resources.every_reminder_starts_off_until_you_turn_it_on
import com.kodeelite.nooreislam.resources.everything_stays_on_your_device
import com.kodeelite.nooreislam.resources.five_quran_fonts_your_size_and_spacing
import com.kodeelite.nooreislam.resources.fourteen_reading_themes
import com.kodeelite.nooreislam.resources.keep_your_place
import com.kodeelite.nooreislam.resources.next
import com.kodeelite.nooreislam.resources.pick_the_days_and_the_time
import com.kodeelite.nooreislam.resources.read_on_your_own_schedule
import com.kodeelite.nooreislam.resources.resume_exactly_where_you_stopped
import com.kodeelite.nooreislam.resources.set_your_own_reminders
import com.kodeelite.nooreislam.resources.start_reading
import com.kodeelite.nooreislam.resources.works_fully_offline_no_account
import com.kodeelite.nooreislam.resources.write_notes_and_highlight_in_colour
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.launch

// Same shape as the main app's onboarding — pager, dots, one white CTA — on the app's own primary.
// Copy is hardcoded until the pages are settled, then it moves to strings.xml.
private const val INTRO_PAGES = 3

@Composable
fun QuranIntroScreen(onDone: () -> Unit) {
    // always the light palette, whatever the device theme — the intro is a branded moment like the
    // splash, and a fixed green keeps white text readable instead of turning pale in dark mode
    val accent = remember { lightAppColors().primary }
    val pager = rememberPagerState(pageCount = { INTRO_PAGES })
    val scope = rememberCoroutineScope()
    val onLast = pager.currentPage == INTRO_PAGES - 1

    Box(Modifier.fillMaxSize().background(accent)) {
        Column(Modifier.fillMaxSize()) {
            HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { i ->
                when (i) {
                    0 -> IntroPage(Lucide.MoonStar, stringResource(Res.string.app_name_quran), stringResource(Res.string.a_calm_place_to_read), isLogo = true) {
                        IntroBullet(Lucide.Palette, stringResource(Res.string.fourteen_reading_themes))
                        IntroBullet(Lucide.Type, stringResource(Res.string.five_quran_fonts_your_size_and_spacing))
                        IntroBullet(Lucide.BookOpen, stringResource(Res.string.works_fully_offline_no_account))
                    }

                    1 -> IntroPage(Lucide.Bookmark, stringResource(Res.string.keep_your_place), stringResource(Res.string.everything_stays_on_your_device)) {
                        IntroBullet(Lucide.Bookmark, stringResource(Res.string.bookmark_any_ayah))
                        IntroBullet(Lucide.Highlighter, stringResource(Res.string.write_notes_and_highlight_in_colour))
                        IntroBullet(Lucide.Clock, stringResource(Res.string.resume_exactly_where_you_stopped))
                    }

                    else -> IntroPage(Lucide.BellRing, stringResource(Res.string.set_your_own_reminders), stringResource(Res.string.read_on_your_own_schedule)) {
                        IntroBullet(Lucide.BookOpen, stringResource(Res.string.al_mulk_al_kahf_and_any_surah_you_choose))
                        IntroBullet(Lucide.Clock, stringResource(Res.string.pick_the_days_and_the_time))
                        IntroBullet(Lucide.BellRing, stringResource(Res.string.every_reminder_starts_off_until_you_turn_it_on))
                    }
                }
            }

            Column(
                Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    repeat(INTRO_PAGES) { i ->
                        Box(
                            Modifier.height(7.dp).width(if (i == pager.currentPage) 22.dp else 7.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = if (i == pager.currentPage) 1f else 0.3f)),
                        )
                    }
                }
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = 36.dp).height(50.dp)
                        .clip(RoundedCornerShape(14.dp)).background(Color.White)
                        .clickable { if (onLast) onDone() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(if (onLast) Res.string.start_reading else Res.string.next),
                            color = accent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        // the forward chevron flips in RTL; the last page's Check never does
                        Icon(
                            if (onLast) Lucide.Check else tr(Lucide.ChevronRight, Lucide.ChevronLeft),
                            null,
                            tint = accent,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IntroPage(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isLogo: Boolean = false, // the first page carries the app's own mark, the rest carry an icon
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(84.dp).clip(RoundedCornerShape(26.dp)).background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            if (isLogo) {
                Image(
                    painterResource(Res.drawable.noor_e_quran_logo),
                    contentDescription = null,
                    modifier = Modifier.size(46.dp),
                )
            } else {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(38.dp))
            }
        }
        Spacer(Modifier.height(26.dp))
        Text(title, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(34.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
}

@Composable
private fun IntroBullet(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(16.dp))
        }
        Text(text, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
    }
}
