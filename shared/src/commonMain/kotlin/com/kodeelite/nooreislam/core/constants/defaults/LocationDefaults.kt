package com.kodeelite.nooreislam.core.constants.defaults

/** Ship defaults for finding and naming where the user is. */
object LocationDefaults {

    /** ~20 miles. Below this, prayer times shift by under a minute, so a fix is drift, not a move. */
    const val MOVE_THRESHOLD_KM = 32.0

    /** Both platforms can simply never call back, so a fix is abandoned rather than awaited. */
    const val FIX_TIMEOUT_MS = 12_000L

    /** A cached fix older than this is worth replacing with a live one. */
    const val MAX_FIX_AGE_MS = 5 * 60 * 1000L

    /** One search per pause in typing, not per keystroke. */
    const val SEARCH_DEBOUNCE_MS = 350L

    /** Below this, a query matches too much to be worth sending. */
    const val SEARCH_MIN_QUERY = 2

    /** Mapbox caps this at 10, and the server clamps anything higher. */
    const val SEARCH_LIMIT = 10
}
