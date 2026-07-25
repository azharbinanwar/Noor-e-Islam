package com.example.miqatapp.feature.studio.data

import kotlinx.serialization.Serializable

// Where the surah attribution block (surah name + ref, one row) sits on the card.
// Top / Bottom only for now — no left/right. None hides it.
@Serializable
enum class SurahPlacement(val label: String) {
    None("Off"),
    Top("Top"),
    Bottom("Bottom"),
}
