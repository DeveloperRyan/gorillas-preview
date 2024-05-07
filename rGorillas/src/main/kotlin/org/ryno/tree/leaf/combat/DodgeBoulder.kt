package org.ryno.tree.leaf.combat

import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.ryno.Script
import org.ryno.State
import org.ryno.utils.CombatUtils
import org.ryno.utils.Utils

class DodgeBoulder(script: Script) : Leaf<Script>(script, "Dodge Boulder") {
    override fun execute() {
        if (!Movement.running() && Movement.energyLevel() > 5) {
            Movement.running(true)
        }

        val projectile = Utils.getFallingBoulder(State.projectiles)

        if (projectile != null) {
            val ringTiles = Utils.getRingTiles(projectile.destination().tile(), 2)
            val gorilla = CombatUtils.getInteractingGorilla()

            // Get the closest tile to the player that is valid and can be walked to
            val destinationTile =
                    ringTiles.sortedBy { it.distanceTo(gorilla) }.firstOrNull { it.valid() }

            if (destinationTile == null) {
                script.localLogger.info("No valid tile to dodge to. Choosing random tile...")
                Movement.step(ringTiles.random())
                return
            }

            script.localLogger.info(
                    "Dodging Boulder - From: ${projectile.destination().tile()} To: $destinationTile"
            )

            Movement.step(destinationTile)
        }
    }
}
