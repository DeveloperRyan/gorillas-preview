package org.ryno.tree.leaf.banking

import org.powbot.api.Condition
import org.powbot.api.rt4.Bank
import org.powbot.api.script.tree.Leaf
import org.ryno.Script
import org.ryno.utils.InventoryUtils

class DepositPotion(script: Script) : Leaf<Script>(script, "Deposit Potion") {
    override fun execute() {
        val potion = script.configuration.potions.find { it.shouldDeposit() } ?: return
        val itemsToDeposit = potion.getPotionsToDeposit()

        itemsToDeposit.forEach {
            val currentCount = InventoryUtils.getInventoryItemCount(it.id)
            if (it.valid() && Bank.deposit(it.id, Bank.Amount.ALL)) {
                script.localLogger.info("Depositing $currentCount ${it.name()}")
                Condition.wait({ InventoryUtils.getInventoryItemCount(it.id) < currentCount }, 300, 4)
            } else {
                script.localLogger.info("Failed to deposit $currentCount ${it.name()}")
            }
        }
    }
}