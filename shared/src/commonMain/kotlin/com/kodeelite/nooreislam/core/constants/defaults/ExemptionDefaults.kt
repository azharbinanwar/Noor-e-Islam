package com.kodeelite.nooreislam.core.constants.defaults

/**
 * Prayer-exemption defaults. [DAYS] is only the first offer: once she picks a length it is remembered
 * and offered next time, and this is the fallback when nothing is remembered yet.
 */
object ExemptionDefaults {
    const val DAYS = 7          // the usual hayd, and well inside every school's maximum
    const val MAX_DAYS = 50     // covers nifas at 40 with room, and stops "on forever"
    const val PAUSE_ALERTS = true
    const val PAUSE_FOCUS = true
}
