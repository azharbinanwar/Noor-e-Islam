package com.kodeelite.nooreislam.feature.studio.data

import androidx.compose.runtime.Composable
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.bottom
import com.kodeelite.nooreislam.resources.off
import com.kodeelite.nooreislam.resources.top
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// Where the surah attribution block (surah name + ref, one row) sits on the card.
// Top / Bottom only for now, no left/right. None hides it.
@Serializable
enum class SurahPlacement(val labelRes: StringResource) {
    None(Res.string.off),
    Top(Res.string.top),
    Bottom(Res.string.bottom),
    ;

    val label: String @Composable get() = stringResource(labelRes)

    companion object {
        val DEFAULT = Top
    }
}
