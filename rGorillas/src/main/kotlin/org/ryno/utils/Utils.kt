package org.ryno.utils

import org.powbot.api.Condition
import org.powbot.api.Notifications
import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.mobile.script.ScriptManager
import org.ryno.Constants
import org.ryno.State
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.abs

object Utils {
    val utilsLogger: Logger = LoggerFactory.getLogger(Utils::class.java)

    fun atGrandTree(): Boolean {
        val player = Players.local()

        return Constants.GRAND_TREE_AREA_LOWER.contains(player) ||
                Constants.GRAND_TREE_AREA_UPPER.contains(player)
    }

    fun getFallingBoulder(projectiles: ArrayList<Projectile>): Projectile? {
        val localTile = Players.local().trueTile()

        return projectiles.firstOrNull { projectile ->
            val projectileTile = projectile.destination().tile()
            val distance = abs(projectileTile.x - localTile.x) + abs(projectileTile.y - localTile.y)
            distance <= 1 && projectile.cycleEnd > Game.cycle()
        }
    }

    fun handleGrandTreeTeleport(): Boolean {
        if (atGrandTree()) {
            return true
        }

        val teleportSeed = InventoryUtils.getInventoryItem(Constants.ROYAL_SEED_POD_NAME)
        if (teleportSeed.valid() && teleportSeed.interact("Commune")) {
            utilsLogger.info("Teleporting to Grand Tree")

            // TODO: Need to find a better way to handle this, a long wait can kill the player, but a short wait doesn't wait for the TP to finish
            // Should probably check if animation === seed pod animation, else break early
            Condition.wait({ atGrandTree() && Players.local().animation() == -1 }, 300, 13)
        }

        return atGrandTree()
    }

    fun playerAtSafespot(): Boolean {
        return Players.local().tile() == Constants.GORILLAS_SAFESPOT_TILE
    }

    fun walkToSafespot() {
        val safespot = Constants.GORILLAS_SAFESPOT_TILE

        utilsLogger.info("Walking to safespot at $safespot")
        Movement.builder(safespot).setRunMin(1).setRunMax(3).setAutoRun(true).move()
    }

    fun handleUnchargedItems() {
        if (!State.hasChargedItems) {
            return
        }

        val unchargedItemCount = Inventory.stream().filtered {
            it.id() in Constants.UNCHARGED_ITEMS
        }.count() + Equipment.stream().filtered {
            it.id() in Constants.UNCHARGED_ITEMS
        }.count()

        // TODO: Add charge handling in the future; this might be weird for items like Arclight and Bowfa

        if (unchargedItemCount > 0) {
            utilsLogger.info("Uncharged items found; teleporting and stopping")
            if (!handleGrandTreeTeleport()) {
                utilsLogger.error("Failed to handle Grand Tree teleport for uncharged items")
                walkToSafespot()
            }

            Notifications.showNotification("Out of charges; stopping script.")
            ScriptManager.stop()
            return
        }
    }

    fun Tile.rangeTo(other: Tile): List<Tile> {
        val xRange = this.x()..other.x()
        val yRange = this.y()..other.y()

        return xRange.flatMap { x ->
            yRange.map { y ->
                Tile(x, y)
            }
        }
    }

    fun getRingTiles(center: Tile, radius: Int): List<Tile> {
        val allTiles = center.derive(-radius, -radius).rangeTo(center.derive(radius, radius))
        val innerTiles = center.derive(-(radius - 1), -(radius - 1)).rangeTo(center.derive(radius - 1, radius - 1))
        return allTiles - innerTiles.toSet()
    }
}