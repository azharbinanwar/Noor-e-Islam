package com.kodeelite.nooreislam.core.enums

import androidx.compose.runtime.Composable
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.qibla_style_classic
import com.kodeelite.nooreislam.resources.qibla_style_compass_rose
import com.kodeelite.nooreislam.resources.qibla_style_modern
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Compass dial styles the user can switch between on the Qibla screen. */
enum class QiblaStyle(val labelRes: StringResource) {
    CompassRose(Res.string.qibla_style_compass_rose),
    Classic(Res.string.qibla_style_classic),
    Modern(Res.string.qibla_style_modern);

    val label: String @Composable get() = stringResource(labelRes)

    companion object {
        fun fromName(name: String?): QiblaStyle? = entries.firstOrNull { it.name == name }
    }
}
