package org.ryno.utils

import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.model.Skill
import org.ryno.Constants
import org.ryno.State
import org.ryno.models.MeleePrayer
import org.ryno.models.RangedPrayer

object CombatUtils {
    fun getInteractingGorilla(): Npc {
        return Npcs.stream()
            .name(Constants.DEMONIC_GORILLA_NAME)
            .within(Constants.GORILLAS_AREA)
            .interactingWithMe()
            .nearest().first()
    }

    fun getActivePrayer(overheadId: Int): Prayer.Effect? {
        return when (overheadId) {
            Constants.MELEE_OVERHEAD_ID -> Prayer.Effect.PROTECT_FROM_MELEE
            Constants.MISSILES_OVERHEAD_ID -> Prayer.Effect.PROTECT_FROM_MISSILES
            Constants.MAGIC_OVERHEAD_ID -> Prayer.Effect.PROTECT_FROM_MAGIC
            else -> null
        }
    }

    fun hasFullHealth(): Boolean {
        return Players.local().healthPercent() == 100
    }

    fun hasFullPrayer(): Boolean {
        return Prayer.prayerPoints() == Skills.realLevel(Skill.Prayer)
    }

    fun getCorrectOverheadPrayer(gorillaAttackStyle: Constants.AttackStyles): Prayer.Effect? {
        return when (gorillaAttackStyle) {
            Constants.AttackStyles.MAGIC -> Prayer.Effect.PROTECT_FROM_MAGIC
            Constants.AttackStyles.RANGED -> Prayer.Effect.PROTECT_FROM_MISSILES
            Constants.AttackStyles.MELEE -> Prayer.Effect.PROTECT_FROM_MELEE
            else -> null
        }
    }

    fun getCorrectCombatPrayer(playerAttackStyle: Constants.AttackStyles, meleePrayer: MeleePrayer, rangedPrayer: RangedPrayer): Prayer.Effect? {
        return when (playerAttackStyle) {
            Constants.AttackStyles.MELEE -> meleePrayer.toPrayerEffect()
            Constants.AttackStyles.RANGED -> rangedPrayer.toPrayerEffect()
            else -> null
        }
    }

    fun getCorrectAttackStyleForPrayer(prayer: Prayer.Effect?): Constants.AttackStyles {
        return when (prayer) {
            Prayer.Effect.PROTECT_FROM_MAGIC -> Constants.AttackStyles.MELEE
            Prayer.Effect.PROTECT_FROM_MISSILES -> Constants.AttackStyles.MELEE
            Prayer.Effect.PROTECT_FROM_MELEE -> Constants.AttackStyles.RANGED
            else -> State.playerAttackStyle
        }
    }

    fun hasSpecialAttackWeaponEquipped(specialAttackWeaponId: Int): Boolean {
        return Equipment.itemAt(Equipment.Slot.MAIN_HAND).id == specialAttackWeaponId
    }

    fun isHealingSpecialAttack(specialAttackWeaponId: Int): Boolean {
        return when (specialAttackWeaponId) {
            Constants.TOXIC_BLOWPIPE_ID -> true
            Constants.SARADOMIN_GODSWORD_ID -> true
            else -> false
        }
    }
}