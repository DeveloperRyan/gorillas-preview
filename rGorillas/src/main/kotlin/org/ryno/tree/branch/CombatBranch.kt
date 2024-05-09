package org.ryno.tree.branch

import org.powbot.api.Random
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.ryno.Constants
import org.ryno.Script
import org.ryno.State
import org.ryno.models.SpecialAttackWeapon
import org.ryno.tree.leaf.DrinkPotion
import org.ryno.tree.leaf.EatFood
import org.ryno.tree.leaf.combat.*
import org.ryno.utils.CombatUtils
import org.ryno.utils.InventoryUtils
import org.ryno.utils.Utils

class ShouldKillGorilla(script: Script) : Branch<Script>(script, "Should Fight Gorilla") {
    override val failedComponent: TreeComponent<Script> = ShouldUseHouse(script)
    override val successComponent: TreeComponent<Script> = ShouldWalkToGorillas(script)

    override fun validate(): Boolean {
        updateLogging()
        return shouldKeepFighting() ||
                (isWellProvisioned() && !needsRestock() && !needsHealing() && !shouldUsePoh() && !shouldBankLoot())
    }

    private fun updateLogging() {
        val logMessage = """
            Should Keep Fighting: ${shouldKeepFighting()} - 
            Has Food: ${InventoryUtils.hasFood()} - 
            Has Prayer Potion: ${InventoryUtils.hasPrayerPotion()} - 
            Should Restock: ${needsRestock()} - 
            Should Heal: ${needsHealing()} - 
            Should Use POH: ${shouldUsePoh()}
        """.trimIndent()
        script.localLogger.info(logMessage)
    }

    private fun shouldKeepFighting(): Boolean {
        val player = Players.local()
        return player.inCombat() && player.healthPercent() > Random.nextInt(30, 50)
    }

    private fun needsRestock(): Boolean {
        val player = Players.local()

        val atBank = Bank.nearest().distanceTo(player) < 5
        val atGrandTree = Utils.atGrandTree()
        return ((atBank || atGrandTree) && Inventory.emptySlotCount() > 0) ||
                (!Constants.GORILLAS_AREA.contains(player) && Inventory.emptySlotCount() > 5)
    }

    private fun needsHealing(): Boolean {
        val player = Players.local()
        return player.healthPercent() < 90 && Bank.nearest().distanceTo(player) < 5
    }

    private fun shouldUsePoh(): Boolean {
        val inCave = Constants.CRASH_SITE_CAVES_AREA.contains(Players.local())
        return script.configuration.useHouse && !inCave &&
                (!CombatUtils.hasFullHealth() || !CombatUtils.hasFullPrayer())
    }

    private fun shouldBankLoot(): Boolean {
        val atGorillas = Constants.GORILLAS_AREA.contains(Players.local())

        return InventoryUtils.hasLoot() && !atGorillas
    }

    private fun isWellProvisioned(): Boolean {
        val atGorillas = Constants.GORILLAS_AREA.contains(Players.local())
        // We specifically care about 3/4 dose potions if we're at the bank still
        val hasPrayerPotions =
            InventoryUtils.getInventoryItemCount(
                *Constants.PRAYER_POTIONS.sliceArray(2..3),
                *Constants.SUPER_RESTORE_POTIONS.sliceArray(2..3)
            ) == script.configuration.prayerPotionCount
        return InventoryUtils.hasFood() &&
                (hasPrayerPotions || (Prayer.prayerPoints() > 20 && atGorillas))
    }
}

class ShouldWalkToGorillas(script: Script) : Branch<Script>(script, "Should Walk to Gorillas") {
    override val failedComponent: TreeComponent<Script> = ShouldDodgeBoulder(script)
    override val successComponent: TreeComponent<Script> = WalkToGorillas(script)

    override fun validate(): Boolean {
        return !Constants.GORILLAS_AREA.contains(Players.local())
    }
}

class ShouldDodgeBoulder(script: Script) : Branch<Script>(script, "Should Dodge Boulder") {
    override val failedComponent: TreeComponent<Script> = ShouldChangeOverheadPrayer(script)
    override val successComponent: TreeComponent<Script> = DodgeBoulder(script)

    override fun validate(): Boolean {
        return Utils.getFallingBoulder(State.projectiles) != null
    }
}

class ShouldChangeOverheadPrayer(script: Script) : Branch<Script>(script, "Should Change Overhead Prayer") {
    override val failedComponent: TreeComponent<Script> = ShouldEatFood(script)
    override val successComponent: TreeComponent<Script> = ChangeOverheadPrayer(script)

    override fun validate(): Boolean {
        val gorilla = CombatUtils.getInteractingGorilla()
        val isRoaring =
            gorilla.valid() && gorilla.overheadMessage() == Constants.GORILLA_ROAR_TEXT && State.roarDelay == 0

        if (isRoaring) {
            script.localLogger.info("Gorilla is roaring; changing prayer")
            State.roarDelay = 4

            when (State.gorillaAttackStyle) {
                Constants.AttackStyles.MELEE -> State.gorillaAttackStyle = listOf(Constants.AttackStyles.MAGIC, Constants.AttackStyles.RANGED).random()
                Constants.AttackStyles.RANGED -> State.gorillaAttackStyle = Constants.AttackStyles.MAGIC
                Constants.AttackStyles.MAGIC -> State.gorillaAttackStyle = Constants.AttackStyles.RANGED
                else -> Constants.AttackStyles.NONE
            }
        }


        val correctPrayer = CombatUtils.getCorrectOverheadPrayer(State.gorillaAttackStyle) ?: return false

        return !Prayer.activePrayers().contains(correctPrayer) && Prayer.prayerPoints() > 0
    }
}

class ShouldEatFood(script: Script) : Branch<Script>(script, "Should Eat Food") {
    override val failedComponent: TreeComponent<Script> = ShouldChangeGear(script)
    override val successComponent: TreeComponent<Script> = EatFood(script)

    override fun validate(): Boolean {
        return Players.local().healthPercent() < State.eatAtPercent && State.foodDelay <= 0
    }
}

class ShouldChangeGear(script: Script) : Branch<Script>(script, "Should Change Gear") {
    override val failedComponent: TreeComponent<Script> = ShouldChangeCombatPrayer(script)
    override val successComponent: TreeComponent<Script> = EquipGear(script)

    override fun validate(): Boolean {
        val gorilla = CombatUtils.getInteractingGorilla()
        val gorillaPrayer = CombatUtils.getActivePrayer(gorilla.prayerHeadIconId())
        val correctAttackStyle = CombatUtils.getCorrectAttackStyleForPrayer(gorillaPrayer)

        val specWeapon = script.configuration.specialAttackWeapon
        val isUsingSpecialAttack = (specWeapon != SpecialAttackWeapon.NONE &&
                correctAttackStyle == specWeapon.style &&
                Combat.specialPercentage() > 50 &&
                CombatUtils.hasSpecialAttackWeaponEquipped(specWeapon.itemId))

        script.localLogger.info("Gorilla Prayer: ${gorillaPrayer?.name ?: "None"} - Correct Attack Style: ${correctAttackStyle.name}")

        State.gorillaPrayer = gorillaPrayer
        return !hasCorrectGear(correctAttackStyle) && !isUsingSpecialAttack
    }

    private fun hasCorrectGear(attackStyle: Constants.AttackStyles?): Boolean {
        val setup = when (attackStyle) {
            Constants.AttackStyles.RANGED -> script.configuration.rangedSetup
            Constants.AttackStyles.MELEE -> script.configuration.meleeSetup
            else -> State.activeGearSetup
        }
        return InventoryUtils.getInventoryItemCount(*setup) == 0
    }
}

class ShouldChangeCombatPrayer(script: Script) : Branch<Script>(script, "Should Change Combat Prayer") {
    override val failedComponent: TreeComponent<Script> = ShouldPickupLoot(script)
    override val successComponent: TreeComponent<Script> = ChangeCombatPrayer(script)

    override fun validate(): Boolean {
        val correctPrayer = CombatUtils.getCorrectCombatPrayer(
            State.playerAttackStyle,
            script.configuration.meleePrayer,
            script.configuration.rangedPrayer
        ) ?: return false // If we return null, just exit since we're not praying for this combat style

        return !Prayer.activePrayers().contains(correctPrayer) && Prayer.prayerPoints() > 0 && Players.local()
            .inCombat()
    }
}

class ShouldPickupLoot(script: Script) : Branch<Script>(script, "Should Pickup Loot") {
    override val failedComponent: TreeComponent<Script> = ShouldAttackGorilla(script)
    override val successComponent: TreeComponent<Script> = PickupItem(script)

    override fun validate(): Boolean {
        if (State.lootTile == null) {
            return false
        }

        if (Inventory.isFull() && InventoryUtils.hasFood()) {
            val groundItems = GroundItems.stream().id(*State.lootWhitelist)
                .filtered { it.id() != Constants.SHARK_ID }
                .within(State.lootTile!!, 3)

            return groundItems.isNotEmpty()
        } else if (Inventory.isFull() && InventoryUtils.hasPrayerPotion()) {
            val groundItems = GroundItems.stream().id(*State.lootWhitelist)
                .filtered { it.id() != Constants.PRAYER_POTION_3_ID }
                .within(State.lootTile!!, 3)

            return groundItems.isNotEmpty()
        }

        val groundItems = GroundItems.stream()
            .id(*State.lootWhitelist)
            .within(State.lootTile!!, 3)

        return State.shouldLoot && groundItems.isNotEmpty()
    }
}

class ShouldAttackGorilla(script: Script) : Branch<Script>(script, "Should Attack Gorilla") {
    override val failedComponent: TreeComponent<Script> = ShouldEquipSpecialAttackWeapon(script)
    override val successComponent: TreeComponent<Script> = AttackGorilla(script)

    override fun validate(): Boolean {
        return Constants.GORILLAS_AREA.contains(Players.local()) && !Players.local().interacting().valid()
    }
}

class ShouldEquipSpecialAttackWeapon(script: Script) : Branch<Script>(script, "Should Equip Special Attack Weapon") {
    override val failedComponent: TreeComponent<Script> = ShouldUseSpecialAttack(script)
    override val successComponent: TreeComponent<Script> = EquipSpecialAttackWeapon(script)

    override fun validate(): Boolean {
        val specWeapon = script.configuration.specialAttackWeapon
        val isReadyForHealingSpecial = if (CombatUtils.isHealingSpecialAttack(specWeapon.itemId)) {
            Players.local().healthPercent() <= 90
        } else {
            true
        }

        return (specWeapon != SpecialAttackWeapon.NONE &&
                !CombatUtils.hasSpecialAttackWeaponEquipped(specWeapon.itemId) &&
                State.playerAttackStyle == specWeapon.style &&
                Combat.specialPercentage() >= 50 && isReadyForHealingSpecial
                )
    }
}

class ShouldUseSpecialAttack(script: Script) : Branch<Script>(script, "Should Use Special Attack") {
    override val failedComponent: TreeComponent<Script> = ShouldAlch(script)
    override val successComponent: TreeComponent<Script> = UseSpecialAttack(script)

    override fun validate(): Boolean {
        val specWeapon = script.configuration.specialAttackWeapon
        val isReadyForHealingSpecial = if (CombatUtils.isHealingSpecialAttack(specWeapon.itemId)) {
            Players.local().healthPercent() <= 90
        } else {
            true
        }

        return (specWeapon != SpecialAttackWeapon.NONE &&
                CombatUtils.hasSpecialAttackWeaponEquipped(specWeapon.itemId) &&
                State.playerAttackStyle == specWeapon.style &&
                Combat.specialPercentage() >= 50 &&
                isReadyForHealingSpecial)
    }
}

class ShouldAlch(script: Script) : Branch<Script>(script, "Should Alch") {
    override val failedComponent: TreeComponent<Script> = ShouldDrinkPotion(script)
    override val successComponent: TreeComponent<Script> = AlchItem(script)

    override fun validate(): Boolean {
        return InventoryUtils.getInventoryItem(*Constants.ALCH_WHITELIST).valid()
                && Magic.Spell.HIGH_ALCHEMY.canCast()
                && script.configuration.shouldAlch
    }
}

class ShouldDrinkPotion(script: Script) : Branch<Script>(script, "Should Drink Potion") {
    override val failedComponent: TreeComponent<Script> = ShouldDropVial(script)
    override val successComponent: TreeComponent<Script> = DrinkPotion(script)

    override fun validate(): Boolean {
        return script.configuration.potions.any { it.shouldDrink() } && State.potionDelay <= 0
    }
}

class ShouldDropVial(script: Script) : Branch<Script>(script, "Should Drop Vial") {
    override val failedComponent: TreeComponent<Script> = SimpleLeaf(script, "In Combat") {
        val player = Players.local()
        script.localLogger.info(
            "In Combat - Gorilla Health: ${
                player.interacting().healthPercent()
            }% - Player Health: ${player.healthPercent()}%"
        )
    }
    override val successComponent: TreeComponent<Script> = DropVial(script)

    override fun validate(): Boolean {
        return Inventory.stream().id(Constants.VIAL_ID).isNotEmpty()
    }
}