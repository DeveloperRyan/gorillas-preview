package org.ryno.tree.leaf.combat

import org.powbot.api.Condition
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.ryno.Constants
import org.ryno.Script
import org.ryno.State
import org.ryno.utils.InventoryUtils
import org.ryno.utils.Utils

class PickupItem(script: Script) : Leaf<Script>(script, "Pickup Item") {
    override fun execute() {
        val items = getItemsToPickup()

        items.forEach {
            InventoryUtils.handleFullInventory()

            script.localLogger.info("Picking up item: ${it.name()}")

            val fallingBoulder = Utils.getFallingBoulder(State.projectiles)

            if (fallingBoulder != null && fallingBoulder.tile() == it.tile) {
                script.localLogger.info("Boulder falling, stopping looting early")
                return
            }

            if (it.valid()) {
                if (it.distance() > 8) {
                    Movement.step(it.tile)
                    Condition.wait({ Players.local().trueTile() == it.tile }, 200, 5)
                }

                if (!it.inViewport()) {
                    Camera.turnTo(it)
                }

                if (it.interact("Take")) {
                    Condition.wait({ !it.valid() || Utils.getFallingBoulder(State.projectiles) != null }, 200, 4)
                    script.localLogger.info("Picked up item: ${it.name()}")
                } else {
                    script.localLogger.info("Failed to pick up item: ${it.name()}")
                }
            }
        }

        State.lootCounter = 0 // After we loot all the items, clear the counter so we don't try to pickup other players' items (ironman support)
    }

    private fun getItemsToPickup(): List<GroundItem> {
        if (State.lootTile == null || !State.shouldLoot) {
            return listOf()
        }

        return if (Inventory.isFull()) {
            val lootItems = State.lootWhitelist.filter { it != Constants.SHARK_ID && it != Constants.PRAYER_POTION_3_ID }

            GroundItems.stream()
                .id(*lootItems.toIntArray())
                .within(State.lootTile!!, 2).toList()
        } else {
            GroundItems.stream().id(*State.lootWhitelist).within(State.lootTile!!, 2).toList()
        }
    }
}