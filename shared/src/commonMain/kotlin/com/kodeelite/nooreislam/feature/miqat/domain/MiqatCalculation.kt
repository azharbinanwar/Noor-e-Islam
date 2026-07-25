package com.kodeelite.nooreislam.feature.miqat.domain

import com.kodeelite.nooreislam.core.enums.CalculationMethod
import com.kodeelite.nooreislam.core.enums.HighLatRule
import com.kodeelite.nooreislam.core.enums.Madhab
import com.kodeelite.nooreislam.core.enums.Miqat

/** Frozen calc settings — the engine's input. Plain data, no logic. */
data class MiqatCalculation(
    val method: CalculationMethod,
    val madhab: Madhab,
    val highLatRule: HighLatRule,
    val fajrAngle: Int,
    val ishaAngle: Int,
    val adjustments: Map<Miqat, Int>,
)
