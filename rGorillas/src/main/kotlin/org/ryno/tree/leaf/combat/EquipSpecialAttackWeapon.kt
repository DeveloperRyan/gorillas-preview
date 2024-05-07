package org.ryno.tree.leaf.combat

import org.powbot.api.Condition
import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.ryno.Script
import org.ryno.models.SpecialAttackWeapon
import org.ryno.utils.CombatUtils
import org.ryno.utils.InventoryUtils

class EquipSpecialAttackWeapon(script: Script) : Leaf<Script>(script, "Equip Special Attack Weapon") {
    override fun execute() {
        // Two-handed weapons need to handle full inventories
        if (script.configuration.specialAttackWeapon == SpecialAttackWeapon.SARADOMIN_GODSWORD ||
            script.configuration.specialAttackWeapon == SpecialAttackWeapon.TOXIC_BLOWPIPE) {
            InventoryUtils.handleFullInventory()
        }

        val weapon = InventoryUtils.getInventoryItem(script.configuration.specialAttackWeapon.itemId)

        if (weapon.valid() && weapon.interact("Wield")) {
            script.localLogger.info("Equipping special attack weapon")
            Condition.wait({ Equipment.itemAt(Equipment.Slot.MAIN_HAND) == weapon }, 250, 3)
        }
    }
}