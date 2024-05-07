package org.ryno.tree.branch

import org.powbot.api.Notifications
import org.powbot.api.rt4.Magic
import org.powbot.api.rt4.Poh
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.ryno.Constants
import org.ryno.Script
import org.ryno.tree.leaf.poh.TeleportToHouse
import org.ryno.tree.leaf.poh.UseRestorePool
import org.ryno.utils.CombatUtils
import org.ryno.utils.InventoryUtils

class ShouldUseHouse(script: Script) : Branch<Script>(script, "Should Use POH") {
    override val failedComponent: TreeComponent<Script> = ShouldWalkToBank(script)
    override val successComponent: TreeComponent<Script> = ShouldTeleportToHouse(script)

    override fun validate(): Boolean {
        val teleportTablet = InventoryUtils.getInventoryItem(Constants.POH_TELEPORT_TABLET)
        if (!Magic.Spell.TELEPORT_TO_HOUSE.canCast() && !teleportTablet.valid()) {
            Notifications.showNotification("No teleport to house available, disabling POH usage")
            script.localLogger.info("No teleport to house available, disabling POH usage")
            script.configuration.useHouse = false
        }

        return script.configuration.useHouse &&
                (!CombatUtils.hasFullHealth() || !CombatUtils.hasFullPrayer())
    }
}

class ShouldTeleportToHouse(script: Script) : Branch<Script>(script, "Should Teleport to POH") {
    override val failedComponent: TreeComponent<Script> = ShouldUsePool(script)
    override val successComponent: TreeComponent<Script> = TeleportToHouse(script)

    override fun validate(): Boolean {
        return !Poh.inside(false)
    }
}

class ShouldUsePool(script: Script) : Branch<Script>(script, "Should Use Pool") {
    override val failedComponent: TreeComponent<Script> = ShouldWalkToBank(script)
    override val successComponent: TreeComponent<Script> = UseRestorePool(script)

    override fun validate(): Boolean {
        return !CombatUtils.hasFullHealth() || !CombatUtils.hasFullPrayer()
    }
}