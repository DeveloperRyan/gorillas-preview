package org.ryno.tree.leaf.poh

import org.powbot.api.Condition
import org.powbot.api.rt4.Magic
import org.powbot.api.rt4.Poh
import org.powbot.api.script.tree.Leaf
import org.ryno.Constants
import org.ryno.Script
import org.ryno.utils.InventoryUtils

class TeleportToHouse(script: Script) : Leaf<Script>(script, "Teleport to House") {
    override fun execute() {
        if (Magic.Spell.TELEPORT_TO_HOUSE.canCast() && Magic.Spell.TELEPORT_TO_HOUSE.cast()) {
            Condition.wait({ Poh.inside(true) }, 300, 8)
            return
        }

        val teleportTablet = InventoryUtils.getInventoryItem(Constants.POH_TELEPORT_TABLET)
        if (teleportTablet.valid() && teleportTablet.interact("Inside")) {
            Condition.wait({ Poh.inside(true) }, 300, 8)
        }
    }
}