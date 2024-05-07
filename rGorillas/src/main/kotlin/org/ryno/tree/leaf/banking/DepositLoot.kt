package org.ryno.tree.leaf.banking

import org.powbot.api.Condition
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.ryno.Constants
import org.ryno.Script
import org.ryno.State

class DepositLoot(script: Script) : Leaf<Script>(script, "Deposit Loot") {
    override fun execute() {
        script.localLogger.info("Depositing Loot")

        Inventory.stream().id(*State.lootWhitelist).filtered {
            it.id() != script.configuration.foodId && it.id() != Constants.PRAYER_POTION_3_ID
        }.forEach {
            if (Bank.deposit(it.id, Bank.Amount.ALL)) {
                Condition.wait({ !it.valid() }, 300, 4)
            }
        }
    }
}