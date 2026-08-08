package com.kodeelite.nooreislam.feature.quran.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kodeelite.nooreislam.core.components.AppSectionLabel
import com.kodeelite.nooreislam.core.components.TilePosition
import com.kodeelite.nooreislam.core.navigation.AppRoute
import com.kodeelite.nooreislam.core.navigation.LocalAppNavigator
import com.kodeelite.nooreislam.feature.quran.data.QuranRepository
import com.kodeelite.nooreislam.feature.quran.data.QuranStore
import com.kodeelite.nooreislam.feature.quran.data.Surah
import com.kodeelite.nooreislam.feature.quran.presentation.components.SurahActionSheet
import com.kodeelite.nooreislam.feature.quran.presentation.components.SurahItem
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.favorites
import com.kodeelite.nooreislam.resources.surahs_label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

// Surah listing — favorites pinned on top, then the rest; long-press a row for its actions
@Composable
fun SurahsTab() {
    val nav = LocalAppNavigator.current
    val store = koinInject<QuranStore>()
    val surahs by produceState(emptyList()) { value = QuranRepository.surahs() }
    val favorites by store.favorites.collectAsState()
    var actionSurah by remember { mutableStateOf<Surah?>(null) }

    val favs = surahs.filter { it.number in favorites }
    val rest = surahs.filterNot { it.number in favorites }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (favs.isNotEmpty()) {
            item { SectionLabel(stringResource(Res.string.favorites)) }
            items(favs.size) { i ->
                SurahItem(
                    favs[i],
                    TilePosition.at(i, favs.size),
                    onLongClick = { actionSurah = favs[i] }) { nav.navigate(AppRoute.QuranReader(favs[i].number, 1)) }
            }
            item { SectionLabel(stringResource(Res.string.surahs_label)) }
        }
        items(rest.size) { i ->
            SurahItem(
                rest[i],
                TilePosition.at(i, rest.size),
                onLongClick = { actionSurah = rest[i] }) { nav.navigate(AppRoute.QuranReader(rest[i].number, 1)) }
        }
    }

    actionSurah?.let { s ->
        SurahActionSheet(
            surah = s,
            favorite = s.number in favorites,
            onToggleFavorite = { store.toggleFavorite(s.number) },
            onOpen = { nav.navigate(AppRoute.QuranReader(s.number, 1)) },
            onDismiss = { actionSurah = null },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color = AppTheme.colors.primary,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp),
    )
}
