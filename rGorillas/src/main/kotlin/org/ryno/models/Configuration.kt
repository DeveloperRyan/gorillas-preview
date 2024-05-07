package org.ryno.models

import org.powbot.api.Notifications
import org.powbot.api.rt4.Magic
import org.powbot.api.rt4.Prayer
import org.powbot.mobile.script.ScriptManager
import org.ryno.Constants
import org.ryno.Constants.RangePotionType
import org.ryno.Constants.CombatPotionType
import org.ryno.State
import org.ryno.utils.InventoryUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val configurationLogger: Logger = LoggerFactory.getLogger("Configuration")
class Configuration(
    var meleeSetup: IntArray = intArrayOf(),
    var rangedSetup: IntArray = intArrayOf(),
    var specialAttackWeapon: SpecialAttackWeapon,
    var meleePrayer: MeleePrayer,
    var rangedPrayer: RangedPrayer,
    var foodId: Int = Constants.SHARK_ID,
    var shouldAlch: Boolean = true,
    var combatPotionType: CombatPotionType = CombatPotionType.NONE,
    var combatPotionCount: Int = 0,
    var rangePotionType: RangePotionType = RangePotionType.NONE,
    var rangePotionCount: Int = 0,
    var prayerPotionType: Constants.PrayerPotionType = Constants.PrayerPotionType.PRAYER,
    var prayerPotionCount: Int = 0,
    var lootAshes: Boolean = false,
    var useHouse: Boolean = false,
) {
    val potions = mutableListOf<Potion>().apply {
        if (combatPotionType == CombatPotionType.SUPER_SET) {
            add(Potion.CombatPotion(CombatPotionType.SUPER_ATTACK, combatPotionCount))
            add(Potion.CombatPotion(CombatPotionType.SUPER_STRENGTH, combatPotionCount))
        } else {
            add(Potion.CombatPotion(combatPotionType, combatPotionCount))
        }
        add(Potion.RangingPotion(rangePotionType, rangePotionCount))
        add(Potion.PrayerPotion(prayerPotionType, prayerPotionCount))
    }

    override fun toString(): String {
        return "Configuration(meleeSetup=${meleeSetup.contentToString()}, " +
                "rangedSetup=${rangedSetup.contentToString()}, " +
                "specialAttackWeapon=${specialAttackWeapon}, " +
                "meleePrayer=$meleePrayer, " +
                "rangedPrayer=$rangedPrayer, " +
                "foodId=$foodId, " +
                "shouldAlch=$shouldAlch, " +
                "combatPotionType=$combatPotionType, " +
                "combatPotionCount=$combatPotionCount, " +
                "rangePotionType=$rangePotionType, " +
                "rangePotionCount=$rangePotionCount, " +
                "prayerPotionType=$prayerPotionType, " +
                "prayerPotionCount=$prayerPotionCount, " +
                "useHouse=$useHouse, " +
                "lootAshes=$lootAshes, " +
                "potions=$potions)"
    }
}

object ConfigurationFactory {
    fun create(
        meleeSetup: IntArray,
        rangedSetup: IntArray,
        inventoryItems: Map<Int, Int>, // <Id, Count>
        specialAttackWeaponName: String,
        meleePrayer: String,
        rangedPrayer: String,
        shouldAlch: Boolean,
        lootAshes: Boolean,
        useHouse: Boolean,
    ): Configuration {
        val alchingEnabled = shouldAlch && Magic.Spell.HIGH_ALCHEMY.canCast()
        if (shouldAlch && !Magic.Spell.HIGH_ALCHEMY.canCast()) {
            Notifications.showNotification("Alch Enabled, but can't cast - disabling.")
            configurationLogger.error("Alch Enabled, but can't cast, disabling.")
        }

        createLootWhitelist(lootAshes)
       val inventorySetup = InventorySetupFactory.createInventorySetup(inventoryItems)

        checkSetupsAndItems(meleeSetup, rangedSetup)

        val meleePrayerType = getMeleePrayerType(meleePrayer)
        val rangedPrayerType = getRangedPrayerType(rangedPrayer)

        return Configuration(
            meleeSetup = meleeSetup,
            rangedSetup = rangedSetup,
            specialAttackWeapon = getSpecialAttackWeapon(specialAttackWeaponName),
            meleePrayer = meleePrayerType,
            rangedPrayer = rangedPrayerType,
            foodId = inventorySetup.foodId,
            shouldAlch = alchingEnabled,
            combatPotionType = inventorySetup.combatPotionType,
            combatPotionCount = inventorySetup.combatPotionCount,
            rangePotionType = inventorySetup.rangePotionType,
            rangePotionCount = inventorySetup.rangePotionCount,
            prayerPotionType = inventorySetup.prayerPotionType,
            prayerPotionCount = inventorySetup.prayerPotionCount,
            lootAshes = lootAshes,
            useHouse = useHouse,
        )
    }

    private fun checkSetupsAndItems(meleeSetup: IntArray, rangedSetup: IntArray) {
        if (meleeSetup.isEmpty() || rangedSetup.isEmpty()) {
            configurationLogger.error("Melee or Ranged setup is empty. Stopping script.")
            Notifications.showNotification("Melee or Ranged setup is empty. Stopping script.")
            ScriptManager.stop()
        }

        if (meleeSetup.any { it in Constants.CHARGED_ITEMS } || rangedSetup.any { it in Constants.CHARGED_ITEMS }) {
            configurationLogger.error("Charged items in gear setup. Enabling hasChargedItems.")
            State.hasChargedItems = true
        }

        // Don't want to write a separate banking handler to fetch the seed pod - since we only bank at the Grand Tree.
        if (!InventoryUtils.hasSeedPod()) {
            configurationLogger.error("No Royal seed pod in inventory. Stopping script.")
            Notifications.showNotification("Please start the script with a Royal seed pod in your inventory.")
            ScriptManager.stop()
        }
    }

    private fun getSpecialAttackWeapon(weaponName: String) = when  (weaponName) {
        Constants.TOXIC_BLOWPIPE_NAME -> SpecialAttackWeapon.TOXIC_BLOWPIPE
        Constants.SARADOMIN_GODSWORD_NAME -> SpecialAttackWeapon.SARADOMIN_GODSWORD
        Constants.DRAGON_WARHAMMER_NAME -> SpecialAttackWeapon.DRAGON_WARHAMMER
        Constants.ARCLIGHT_NAME -> SpecialAttackWeapon.ARCLIGHT
        else -> SpecialAttackWeapon.NONE
    }

    private fun getMeleePrayerType(meleePrayer: String) = when (meleePrayer) {
        "Piety" -> MeleePrayer.PIETY
        else -> MeleePrayer.NONE
    }

    private fun getRangedPrayerType(rangedPrayer: String) = when (rangedPrayer) {
        "Eagle Eye" -> RangedPrayer.EAGLE_EYE
        "Rigour" -> RangedPrayer.RIGOUR
        else -> RangedPrayer.NONE
    }

    private fun createLootWhitelist(lootAshes: Boolean) {
        if (lootAshes) {
            State.lootWhitelist = intArrayOf(*Constants.LOOT_WHITELIST, Constants.MALICIOUS_ASHES_ID)
        } else {
            State.lootWhitelist = intArrayOf(*Constants.LOOT_WHITELIST)
        }
    }
}