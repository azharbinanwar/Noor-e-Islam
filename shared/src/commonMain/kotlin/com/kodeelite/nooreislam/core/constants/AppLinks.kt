package com.kodeelite.nooreislam.core.constants

import com.kodeelite.nooreislam.core.AppEdition

/**
 * Every URL the app hands out, in one place. Paths mirror the live site at noor.kodeelite.com;
 * the store links are the Play listings and stand in for iOS until App Store ids exist.
 */
object AppLinks {
    const val SITE = "https://noor.kodeelite.com"
    const val CONTACT_EMAIL = "contact@kodeelite.com"

    const val TANZIL = "https://tanzil.net"
    const val QURAN_WBW = "https://quranwbw.com"

    fun page(edition: AppEdition) = when (edition) {
        AppEdition.MAIN -> "$SITE/noor-e-islam/"
        AppEdition.QURAN -> "$SITE/noor-e-quran/"
    }

    fun privacy(edition: AppEdition) = when (edition) {
        AppEdition.MAIN -> "$SITE/noor-e-islam/privacy/"
        AppEdition.QURAN -> "$SITE/quran/privacy/"
    }

    fun store(edition: AppEdition) = when (edition) {
        AppEdition.MAIN -> "https://play.google.com/store/apps/details?id=com.kodeelite.nooreislam"
        AppEdition.QURAN -> "https://play.google.com/store/apps/details?id=com.kodeelite.noorequran"
    }
}
