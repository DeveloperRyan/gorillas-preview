package org.ryno.models

import org.powbot.api.rt4.Item
import org.powbot.api.rt4.Skills
import org.powbot.api.rt4.walking.model.Skill
import org.ryno.Constants
import org.ryno.Constants.ATTACK_POTIONS
import org.ryno.Constants.COMBAT_POTIONS
import org.ryno.Constants.DIVINE_COMBAT_POTIONS
import org.ryno.Constants.DIVINE_RANGING_POTIONS
import org.ryno.Constants.PRAYER_POTIONS
import org.ryno.Constants.RANGING_POTIONS
import org.ryno.Constants.STRENGTH_POTIONS
import org.ryno.Constants.SUPER_RESTORE_POTIONS
import org.ryno.utils.BankUtils.getCorrectPotionFromBank
import org.ryno.utils.InventoryUtils.getInventoryItem
import org.ryno.utils.InventoryUtils.getInventoryItemCount
import org.ryno.utils.InventoryUtils.getInventoryItems

private val SUPER_ATTACK_BASE_NAME = Constants.SUPER_ATTACK_POTION_NAME
private val SUPER_STRENGTH_BASE_NAME = Constants.SUPER_STRENGTH_POTION_NAME
private val SUPER_COMBAT_BASE_NAME = Constants.SUPER_COMBAT_POTION_NAME
private val DIVINE_SUPER_COMBAT_BASE_NAME = Constants.DIVINE_SUPER_COMBAT_POTION_NAME
private val RANGING_BASE_NAME = Constants.RANGING_POTION_NAME
private val DIVINE_RANGING_BASE_NAME = Constants.DIVINE_RANGING_POTION_NAME
private val PRAYER_BASE_NAME = Constants.PRAYER_POTION_NAME
private val SUPER_RESTORE_BASE_NAME = Constants.SUPER_RESTORE_POTION_NAME

sealed class Potion(open val count: Int) {
    abstract fun shouldDrink(): Boolean
    abstract fun shouldWithdraw(): Boolean
    abstract fun shouldDeposit(): Boolean
    abstract fun getPotionToWithdraw(): Item
    abstract fun getPotionsToDeposit(): List<Item>
    abstract fun getPotionToDrink(): Item


    // NOTE: All potions are in an int array of size 4; 0 = (1) dose, 1 = (2) dose, 2 = (3) dose, 3 = (4) dose
    // This makes it a little cleaned to pass a slice of the array to get the correct potion
    // Rather than passing individual potion IDs
    data class CombatPotion(val type: Constants.CombatPotionType, override val count: Int) : Potion(count) {
        override fun shouldDrink() = when (type) {
            Constants.CombatPotionType.SUPER_ATTACK -> Skills.level(Skill.Attack) - Skills.realLevel(Skill.Attack) < 2
            Constants.CombatPotionType.SUPER_STRENGTH -> Skills.level(Skill.Strength) - Skills.realLevel(Skill.Strength) < 2
            Constants.CombatPotionType.SUPER_COMBAT -> Skills.level(Skill.Attack) - Skills.realLevel(Skill.Attack) < 2
            Constants.CombatPotionType.DIVINE_SUPER_COMBAT -> Skills.level(Skill.Attack) - Skills.realLevel(Skill.Attack) < 2
            else -> false
        }

        override fun shouldWithdraw() = when (type) {
            Constants.CombatPotionType.SUPER_ATTACK -> getInventoryItemCount(*ATTACK_POTIONS.sliceArray(2..3)) < count
            Constants.CombatPotionType.SUPER_STRENGTH -> getInventoryItemCount(*STRENGTH_POTIONS.sliceArray(2..3)) < count
            Constants.CombatPotionType.SUPER_COMBAT -> getInventoryItemCount(*COMBAT_POTIONS.sliceArray(2..3)) < count
            Constants.CombatPotionType.DIVINE_SUPER_COMBAT -> getInventoryItemCount(*DIVINE_COMBAT_POTIONS.sliceArray(2..3)) < count
            else -> false
        }

        override fun shouldDeposit() = when (type) {
            Constants.CombatPotionType.SUPER_ATTACK -> getInventoryItemCount(*ATTACK_POTIONS) > count || getInventoryItemCount(
                *ATTACK_POTIONS.sliceArray(0..1)
            ) > 0

            Constants.CombatPotionType.SUPER_STRENGTH -> getInventoryItemCount(*STRENGTH_POTIONS) > count || getInventoryItemCount(
                *STRENGTH_POTIONS.sliceArray(0..1)
            ) > 0

            Constants.CombatPotionType.SUPER_COMBAT -> getInventoryItemCount(*COMBAT_POTIONS) > count || getInventoryItemCount(
                *COMBAT_POTIONS.sliceArray(0..1)
            ) > 0

            Constants.CombatPotionType.DIVINE_SUPER_COMBAT -> getInventoryItemCount(*DIVINE_COMBAT_POTIONS) > count || getInventoryItemCount(
                *DIVINE_COMBAT_POTIONS.sliceArray(0..1)
            ) > 0

            else -> false
        }

        override fun getPotionToWithdraw(): Item = when (type) {
            Constants.CombatPotionType.SUPER_ATTACK -> getCorrectPotionFromBank(SUPER_ATTACK_BASE_NAME)
            Constants.CombatPotionType.SUPER_STRENGTH -> getCorrectPotionFromBank(SUPER_STRENGTH_BASE_NAME)
            Constants.CombatPotionType.SUPER_COMBAT -> getCorrectPotionFromBank(SUPER_COMBAT_BASE_NAME)
            Constants.CombatPotionType.DIVINE_SUPER_COMBAT -> getCorrectPotionFromBank(DIVINE_SUPER_COMBAT_BASE_NAME)
            else -> Item.Nil
        }

        override fun getPotionsToDeposit(): List<Item> = when (type) {
            Constants.CombatPotionType.SUPER_ATTACK -> getInventoryItems(*ATTACK_POTIONS)
            Constants.CombatPotionType.SUPER_STRENGTH -> getInventoryItems(*STRENGTH_POTIONS)
            Constants.CombatPotionType.SUPER_COMBAT -> getInventoryItems(*COMBAT_POTIONS)
            Constants.CombatPotionType.DIVINE_SUPER_COMBAT -> getInventoryItems(*DIVINE_COMBAT_POTIONS)
            else -> listOf()
        }

        override fun getPotionToDrink(): Item = when (type) {
            Constants.CombatPotionType.SUPER_ATTACK -> getInventoryItem(*ATTACK_POTIONS)
            Constants.CombatPotionType.SUPER_STRENGTH -> getInventoryItem(*STRENGTH_POTIONS)
            Constants.CombatPotionType.SUPER_COMBAT -> getInventoryItem(*COMBAT_POTIONS)
            Constants.CombatPotionType.DIVINE_SUPER_COMBAT -> getInventoryItem(*DIVINE_COMBAT_POTIONS)
            else -> Item.Nil
        }
    }

    data class RangingPotion(val type: Constants.RangePotionType, override val count: Int) : Potion(count) {
        override fun shouldDrink() = when (type) {
            Constants.RangePotionType.NONE -> false
            else -> Skills.level(Skill.Ranged) - Skills.realLevel(Skill.Ranged) < 2
        }

        override fun shouldWithdraw() = when (type) {
            Constants.RangePotionType.RANGING -> getInventoryItemCount(*RANGING_POTIONS.sliceArray(2..3)) < count
            Constants.RangePotionType.DIVINE_RANGING -> getInventoryItemCount(*DIVINE_RANGING_POTIONS.sliceArray(2..3)) < count
            else -> false
        }

        override fun shouldDeposit() = when (type) {
            Constants.RangePotionType.RANGING -> getInventoryItemCount(*RANGING_POTIONS) > count || getInventoryItemCount(
                *RANGING_POTIONS.sliceArray(0..1)
            ) > 0

            Constants.RangePotionType.DIVINE_RANGING -> getInventoryItemCount(*DIVINE_RANGING_POTIONS) > count || getInventoryItemCount(
                *RANGING_POTIONS.sliceArray(0..1)
            ) > 0

            else -> false
        }

        override fun getPotionToWithdraw(): Item = when (type) {
            Constants.RangePotionType.RANGING -> getCorrectPotionFromBank(RANGING_BASE_NAME)
            Constants.RangePotionType.DIVINE_RANGING -> getCorrectPotionFromBank(DIVINE_RANGING_BASE_NAME)
            else -> Item.Nil
        }

        override fun getPotionsToDeposit(): List<Item> {
            return getInventoryItems(*RANGING_POTIONS, *DIVINE_RANGING_POTIONS)
        }

        override fun getPotionToDrink(): Item = when (type) {
            Constants.RangePotionType.RANGING -> getInventoryItem(*RANGING_POTIONS)
            Constants.RangePotionType.DIVINE_RANGING -> getInventoryItem(*DIVINE_RANGING_POTIONS)
            else -> Item.Nil
        }
    }

    data class PrayerPotion(val type: Constants.PrayerPotionType, override val count: Int) : Potion(count) {
        override fun shouldDrink() = when (type) {
            else -> Skills.realLevel(Skill.Prayer) - Skills.level(Skill.Prayer) >= 26
        }

        override fun shouldWithdraw() = when (type) {
            Constants.PrayerPotionType.PRAYER -> getInventoryItemCount(*PRAYER_POTIONS.sliceArray(2..3)) < count
            Constants.PrayerPotionType.SUPER_RESTORE -> getInventoryItemCount(*SUPER_RESTORE_POTIONS.sliceArray(2..3)) < count
        }

        override fun shouldDeposit() = when (type) {
            Constants.PrayerPotionType.PRAYER -> getInventoryItemCount(*PRAYER_POTIONS) > count || getInventoryItemCount(
                *PRAYER_POTIONS.sliceArray(0..1)
            ) > 0

            Constants.PrayerPotionType.SUPER_RESTORE -> getInventoryItemCount(*PRAYER_POTIONS) > 0 || getInventoryItemCount(
                *SUPER_RESTORE_POTIONS
            ) > count || getInventoryItemCount(*SUPER_RESTORE_POTIONS.sliceArray(0..1)) > 0
        }

        override fun getPotionToWithdraw(): Item = when (type) {
            Constants.PrayerPotionType.PRAYER -> getCorrectPotionFromBank(PRAYER_BASE_NAME)
            Constants.PrayerPotionType.SUPER_RESTORE -> getCorrectPotionFromBank(SUPER_RESTORE_BASE_NAME)
        }

        override fun getPotionsToDeposit(): List<Item> {
            return getInventoryItems(*PRAYER_POTIONS, *SUPER_RESTORE_POTIONS)
        }

        override fun getPotionToDrink(): Item {
            return getInventoryItem(*SUPER_RESTORE_POTIONS, *PRAYER_POTIONS)
        }
    }
}