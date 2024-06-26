package org.ryno.tree.leaf.combat

import org.powbot.api.Condition
import org.powbot.api.Random
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Leaf
import org.ryno.Constants
import org.ryno.Script
import org.ryno.State
import org.ryno.utils.CombatUtils
import org.ryno.utils.InventoryUtils
import org.ryno.utils.Utils

class EquipGear(script: Script) : Leaf<Script>(script, "Equip Gear") {
    override fun execute() {
        val correctAttackStyle = CombatUtils.getCorrectAttackStyleForPrayer(State.gorillaPrayer)
        State.playerAttackStyle = correctAttackStyle

        val gearToEquip = when (correctAttackStyle) {
            Constants.AttackStyles.RANGED -> InventoryUtils.getInventoryItems(*script.configuration.rangedSetup)
            Constants.AttackStyles.MELEE -> InventoryUtils.getInventoryItems(*script.configuration.meleeSetup)
            else -> State.activeGearSetup.map { InventoryUtils.getInventoryItem(it) }
        }

        if (State.activeGearSetup.size > gearToEquip.size) {
            InventoryUtils.handleFullInventory()
        }

        script.localLogger.info("Correct Attack Style: $correctAttackStyle - Equipping gear: ${gearToEquip.map { it.name() }}")
        State.activeGearSetup = gearToEquip.map { it.id() }.toIntArray()

        val currentGorillaAttackStyle = State.gorillaAttackStyle
        gearToEquip.forEach {
            // If there's a boulder, we should return early
            if (Utils.getFallingBoulder(State.projectiles) != null) {
                script.localLogger.info("Boulder is falling, stopping equipping early")
                return
            }

            if (currentGorillaAttackStyle != State.gorillaAttackStyle) {
                script.localLogger.info("Gorilla attack style changed, stopping equipping early")
                return
            }

            if (Players.local().healthPercent() < State.eatAtPercent) {
                script.localLogger.info("Health is low, stopping equipping early")
                return
            }

            script.localLogger.info("Equipping ${it.name()}")

            if (it.actions().contains("Wield") && it.interact("Wield", false)) {
                Condition.sleep(Random.nextInt(120, 250))
            } else if (it.interact("Wear", false)) {
                Condition.sleep(Random.nextInt(120, 250))
            }
        }
    }
}