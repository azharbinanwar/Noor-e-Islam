package com.kodeelite.nooreislam.feature.quran.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.core.locale.tr
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.feature.quran.data.MushafSign
import com.kodeelite.nooreislam.feature.quran.data.MushafSignGroup
import com.kodeelite.nooreislam.feature.quran.presentation.components.MushafSignCard
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.back
import com.kodeelite.nooreislam.resources.reading_marks
import com.kodeelite.nooreislam.resources.tap_any_example_to_open_that_ayah
import com.kodeelite.nooreislam.resources.the_marks_printed_above_the_arabic_text
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingMarksScreen() {
    val nav = LocalAppNavigator.current
    val colors = AppTheme.colors

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.reading_marks)) },
                navigationIcon = {
                    IconButton({ nav.back() }) {
                        Icon(
                            tr(Lucide.ChevronLeft, Lucide.ChevronRight),
                            stringResource(Res.string.back),
                            tint = colors.onSurface,
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    stringResource(Res.string.the_marks_printed_above_the_arabic_text),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }

            MushafSignGroup.entries.forEach { group ->
                item {
                    Column(Modifier.fillMaxWidth().padding(start = 4.dp, top = 10.dp, bottom = 2.dp)) {
                        Text(
                            group.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                        )
                        group.noteRes?.let {
                            Text(
                                stringResource(it),
                                modifier = Modifier.padding(top = 2.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }
                }
                items(MushafSign.entries.filter { it.group == group }) { sign ->
                    MushafSignCard(sign) {
                        sign.example?.let { nav.navigate(AppRoute.QuranReader(it.surah, it.ayah)) }
                    }
                }
            }

            item {
                Text(
                    stringResource(Res.string.tap_any_example_to_open_that_ayah),
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}
