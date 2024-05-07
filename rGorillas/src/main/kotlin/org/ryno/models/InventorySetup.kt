package org.ryno.models
import org.ryno.Constants

data class InventorySetup(
    val foodId: Int = Constants.SHARK_ID,
    val combatPotionType: Constants.CombatPotionType = Constants.CombatPotionType.NONE,
    val combatPotionCount: Int = 0,
    val rangePotionType: Constants.RangePotionType = Constants.RangePotionType.NONE,
    val rangePotionCount: Int = 0,
    val prayerPotionType: Constants.PrayerPotionType = Constants.PrayerPotionType.PRAYER,
    val prayerPotionCount: Int = 0
)

object InventorySetupFactory {
    fun createInventorySetup(inventorySetupMap: Map<Int, Int>): InventorySetup {
        var foodId: Int = Constants.SHARK_ID
        var combatPotionType: Constants.CombatPotionType = Constants.CombatPotionType.NONE
        var combatPotionCount: Int = 0
        var rangePotionType: Constants.RangePotionType = Constants.RangePotionType.NONE
        var rangePotionCount: Int = 0
        var prayerPotionType: Constants.PrayerPotionType = Constants.PrayerPotionType.PRAYER
        var prayerPotionCount: Int = 0

        inventorySetupMap.forEach { (item, count) ->
            when {
                isFood(item) -> foodId = item
                isPrayerPotion(item) -> {
                    prayerPotionType = Constants.PrayerPotionType.PRAYER
                    prayerPotionCount += count
                }
                isSuperRestorePotion(item) -> {
                    prayerPotionType = Constants.PrayerPotionType.SUPER_RESTORE
                    prayerPotionCount += count
                }
                isRangingPotion(item) -> {
                    rangePotionType = Constants.RangePotionType.RANGING
                    rangePotionCount += count
                }
                isDivineRangingPotion(item) -> {
                    rangePotionType = Constants.RangePotionType.DIVINE_RANGING
                    rangePotionCount += count
                }
                isSuperCombatSet(item) -> {
                    combatPotionType = Constants.CombatPotionType.SUPER_SET
                    combatPotionCount += count
                }
                isCombatPotion(item) -> {
                    combatPotionType = Constants.CombatPotionType.SUPER_COMBAT
                    combatPotionCount += count
                }
                isDivineCombatPotion(item) -> {
                    combatPotionType = Constants.CombatPotionType.SUPER_COMBAT
                    combatPotionCount += count
                }
            }
        }

        return InventorySetup(foodId, combatPotionType, combatPotionCount, rangePotionType, rangePotionCount, prayerPotionType, prayerPotionCount)
    }

    private fun isFood(item: Int) = item in Constants.FOOD_IDS
    private fun isPrayerPotion(item: Int) = item in Constants.PRAYER_POTIONS
    private fun isSuperRestorePotion(item: Int) = item in Constants.SUPER_RESTORE_POTIONS
    private fun isRangingPotion(item: Int) = item in Constants.RANGING_POTIONS
    private fun isDivineRangingPotion(item: Int) = item in Constants.DIVINE_RANGING_POTIONS
    private fun isSuperCombatSet(item: Int) = item in Constants.SUPER_COMBAT_POTION_SET
    private fun isCombatPotion(item: Int) = item in Constants.COMBAT_POTIONS
    private fun isDivineCombatPotion(item: Int) = item in Constants.DIVINE_COMBAT_POTIONS
}