package org.ryno.tree.leaf.combat

import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Leaf
import org.powbot.mobile.script.ScriptManager
import org.ryno.Constants
import org.ryno.Script
import org.ryno.utils.Utils

class WalkToGorillas(script: Script) : Leaf<Script>(script, "Walk to Gorillas") {
    override fun execute() {
        script.localLogger.info("Walking to Gorillas")

        // There's cases where we aren't leaving from the bank - Web Walker doesn't use the seed pod for some reason
        if (!Utils.atGrandTree() &&
            !Constants.CRASH_SITE_AREA.contains(Players.local()) &&
            !Constants.CRASH_SITE_CAVES_AREA.contains(Players.local())) {
            Utils.handleGrandTreeTeleport()
        }

        Movement.builder(Constants.GORILLAS_DESTINATION_TILE).setRunMin(2).setRunMax(7).setAutoRun(true)
            .setWalkUntil { ScriptManager.isStopping() }.move()
    }
}