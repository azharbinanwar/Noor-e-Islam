package com.kodeelite.nooreislam.feature.quran.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.feature.quran.data.QuranRepository
import com.kodeelite.nooreislam.feature.quran.presentation.components.JuzRow

// The 30 juz — each row expands to its surahs; tap opens the reader.
@Composable
fun JuzsTab() {
    val nav = LocalAppNavigator.current
    val juzs by produceState(emptyList()) { value = QuranRepository.juzs() }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(juzs.size, key = { juzs[it].number }) { i ->
            val juz = juzs[i]
            JuzRow(
                juz,
                onOpen = { nav.navigate(AppRoute.QuranReader(juz.startsAt.surah, juz.startsAt.ayah)) },
                // the juz's opening surah may begin mid-way: land where it enters this juz, not at 1:1 of it
                onOpenSurah = {
                    val ayah = if (it.number == juz.startsAt.surah) juz.startsAt.ayah else 1
                    nav.navigate(AppRoute.QuranReader(it.number, ayah))
                },
            )
        }
    }
}
