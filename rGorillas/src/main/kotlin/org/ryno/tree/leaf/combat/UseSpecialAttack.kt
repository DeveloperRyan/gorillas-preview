package org.ryno.tree.leaf.combat

import org.powbot.api.Condition
import org.powbot.api.rt4.Combat
import org.powbot.api.script.tree.Leaf
import org.ryno.Script
import org.ryno.utils.CombatUtils

class UseSpecialAttack(script: Script) : Leaf<Script>(script, "Use Special Attack") {
    override fun execute() {
        val energy = Combat.specialPercentage()
        val gorilla = CombatUtils.getInteractingGorilla()

        if (gorilla.valid() && Combat.specialAttack(true) && gorilla.interact("Attack")) {
            Condition.wait({ Combat.specialPercentage() < energy }, 300, 3)
        }
    }
}