package org.ryno.tree.leaf.poh

import org.powbot.api.Condition
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.tree.Leaf
import org.ryno.Script
import org.ryno.utils.CombatUtils

class UseRestorePool(script: Script) : Leaf<Script>(script, "Use Restore Pool") {
    override fun execute() {
        val pool = Poh.getHealthRestore()

        // TODO: Make this a shared handler
        if (Prayer.prayersActive()) {
            Prayer.activePrayers().forEach {
                script.localLogger.info("Disabling prayer: ${it.name}")
                Prayer.prayer(it, false)
                Condition.wait({ !Prayer.prayerActive(it) }, 300, 4)
            }
        }

        if (pool.valid() && pool.interact("Drink")) {
            Condition.wait({
                CombatUtils.hasFullHealth() && CombatUtils.hasFullPrayer()
            }, 300, 7)
        }
    }
}