package com.kodeelite.nooreislam.core.constants.defaults

import com.kodeelite.nooreislam.core.enums.QiblaStyle

/** Ship defaults for the qibla compass. */
object QiblaDefaults {
    val STYLE = QiblaStyle.CompassRose

    /** How close the heading must come before the screen says you are facing the qibla. */
    const val ALIGN_TOLERANCE_DEG = 5f
}
