package org.ryno.models

import org.powbot.api.rt4.Prayer

enum class MeleePrayer {
    NONE, PIETY;

    fun toPrayerEffect(): Prayer.Effect? {
        return when (this) {
            PIETY -> Prayer.Effect.PIETY
            NONE -> null
        }
    }
}

enum class RangedPrayer {
    NONE, EAGLE_EYE, RIGOUR;

    fun toPrayerEffect(): Prayer.Effect? {
        return when (this) {
            EAGLE_EYE -> Prayer.Effect.EAGLE_EYE
            RIGOUR -> Prayer.Effect.RIGOUR
            NONE -> null
        }
    }
}