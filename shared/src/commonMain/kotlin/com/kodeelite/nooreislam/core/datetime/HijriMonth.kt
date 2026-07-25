package com.kodeelite.nooreislam.core.datetime

import androidx.compose.runtime.Composable
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.dhu_al_hijjah
import com.kodeelite.nooreislam.resources.dhu_al_qadah
import com.kodeelite.nooreislam.resources.jumada_al_awwal
import com.kodeelite.nooreislam.resources.jumada_al_thani
import com.kodeelite.nooreislam.resources.muharram
import com.kodeelite.nooreislam.resources.rabi_al_awwal
import com.kodeelite.nooreislam.resources.rabi_al_thani
import com.kodeelite.nooreislam.resources.rajab
import com.kodeelite.nooreislam.resources.ramadan
import com.kodeelite.nooreislam.resources.safar
import com.kodeelite.nooreislam.resources.shaban
import com.kodeelite.nooreislam.resources.shawwal
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** The twelve Hijri months, in order. Localized name carried on the enum (en/ar). */
enum class HijriMonth(val labelRes: StringResource) {
    Muharram(Res.string.muharram),
    Safar(Res.string.safar),
    RabiAlAwwal(Res.string.rabi_al_awwal),
    RabiAlThani(Res.string.rabi_al_thani),
    JumadaAlAwwal(Res.string.jumada_al_awwal),
    JumadaAlThani(Res.string.jumada_al_thani),
    Rajab(Res.string.rajab),
    Shaban(Res.string.shaban),
    Ramadan(Res.string.ramadan),
    Shawwal(Res.string.shawwal),
    DhuAlQadah(Res.string.dhu_al_qadah),
    DhuAlHijjah(Res.string.dhu_al_hijjah),
    ;

    @Composable
    fun label(): String = stringResource(labelRes)

    companion object {
        /** [number] is 1–12. */
        fun of(number: Int) = entries[number - 1]
    }
}
