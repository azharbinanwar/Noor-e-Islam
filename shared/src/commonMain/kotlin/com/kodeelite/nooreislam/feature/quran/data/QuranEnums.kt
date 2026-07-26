package com.kodeelite.nooreislam.feature.quran.data

import androidx.compose.runtime.Composable
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.revelation_madinah
import com.kodeelite.nooreislam.resources.revelation_makkah
import com.kodeelite.nooreislam.resources.sajda_obligatory
import com.kodeelite.nooreislam.resources.sajda_recommended
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Where a surah was revealed. Same shape as [com.kodeelite.nooreislam.core.enums.Miqat]: carries its own
 *  localized label so screens never hardcode the text. */
enum class Revelation(val labelRes: StringResource) {
    Makkah(Res.string.revelation_makkah),
    Madinah(Res.string.revelation_madinah);

    val label: String @Composable get() = stringResource(labelRes)
}

/** A prostration (sajda) verse — obligatory or recommended. */
@kotlinx.serialization.Serializable
enum class Sajda(val labelRes: StringResource) {
    Recommended(Res.string.sajda_recommended),
    Obligatory(Res.string.sajda_obligatory);

    val label: String @Composable get() = stringResource(labelRes)
}
