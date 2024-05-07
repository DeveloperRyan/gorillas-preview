package org.ryno.models

import org.ryno.Constants

enum class SpecialAttackWeapon(val itemId: Int, val style: Constants.AttackStyles) {
    NONE(-1, Constants.AttackStyles.NONE),
    TOXIC_BLOWPIPE(Constants.TOXIC_BLOWPIPE_ID, Constants.AttackStyles.RANGED),
    SARADOMIN_GODSWORD(Constants.SARADOMIN_GODSWORD_ID, Constants.AttackStyles.MELEE),
    ARCLIGHT(Constants.ARCLIGHT_ID, Constants.AttackStyles.MELEE),
    DRAGON_WARHAMMER(Constants.DRAGON_WARHAMMER_ID, Constants.AttackStyles.MELEE);

    companion object {
        fun fromItemId(id: Int): SpecialAttackWeapon {
            return values().firstOrNull { it.itemId == id } ?: NONE
        }
    }
}