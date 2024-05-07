package org.ryno

import com.google.common.eventbus.Subscribe
import org.powbot.api.event.*
import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Game
import org.powbot.api.script.*
import org.powbot.api.script.tree.TreeComponent
import org.powbot.api.script.tree.TreeScript
import org.powbot.mobile.service.ScriptUploader
import org.ryno.models.Configuration
import org.ryno.models.ConfigurationFactory
import org.ryno.tree.branch.ShouldBreak
import org.ryno.utils.CombatUtils
import org.ryno.utils.PaintUtils
import org.ryno.utils.Utils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@ScriptManifest(
    name = "rGorillas",
    description = "Kills demonic gorillas for money",
    version = "2.5.1",
    author = "ryno",
    category = ScriptCategory.MoneyMaking
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "Melee Setup",
            description = "Melee Setup",
            optionType = OptionType.EQUIPMENT,
        ),
        ScriptConfiguration(
            name = "Ranged Setup",
            description = "Ranged Setup",
            optionType = OptionType.EQUIPMENT,
        ),
        ScriptConfiguration(
            name = "Inventory Setup",
            description = "Inventory Setup",
            optionType = OptionType.INVENTORY,
        ),
        ScriptConfiguration(
            name = "Special Attack Weapon",
            description = "Special Attack Weapon",
            optionType = OptionType.STRING,
            allowedValues = ["None", "Toxic blowpipe", "Saradomin godsword", "Dragon warhammer", "Arclight"],
            defaultValue = "None"
        ),
        ScriptConfiguration(
            name = "Melee Prayer",
            description = "Melee Prayer",
            optionType = OptionType.STRING,
            allowedValues = ["None", "Piety"],
            defaultValue = "None"
        ),
        ScriptConfiguration(
            name = "Ranged Prayer",
            description = "Ranged Prayer",
            optionType = OptionType.STRING,
            allowedValues = ["None", "Eagle Eye", "Rigour"],
            defaultValue = "None"
        ),
        ScriptConfiguration(
            name = "Alch Items",
            description = "Should Alch?",
            optionType = OptionType.BOOLEAN,
            defaultValue = "true"
        ),
        ScriptConfiguration(
            name = "Loot Ashes",
            description = "Loot Ashes?",
            optionType = OptionType.BOOLEAN,
            defaultValue = "false"
        ),
        ScriptConfiguration(
            name = "Use POH Pool",
            description = "Use POH Pool?",
            optionType = OptionType.BOOLEAN,
            defaultValue = "false"
        ),
    ]
)

class Script : TreeScript() {
    val localLogger: Logger = LoggerFactory.getLogger(this::class.java)
    lateinit var configuration: Configuration
    override val rootComponent: TreeComponent<*> = ShouldBreak(this)

    override fun onStart() {
        val meleeSetup = getOption<Map<Int, Equipment.Slot>>("Melee Setup")
        val rangedSetup = getOption<Map<Int, Equipment.Slot>>("Ranged Setup")
        val inventorySetup = getOption<Map<Int, Int>>("Inventory Setup") // <Item ID, Item Count>
        State.activeGearSetup = rangedSetup.keys.toIntArray()

        configuration = ConfigurationFactory.create(
            meleeSetup = meleeSetup.keys.toIntArray(),
            rangedSetup = rangedSetup.keys.toIntArray(),
            inventoryItems = inventorySetup,
            specialAttackWeaponName = getOption("Special Attack Weapon"),
            meleePrayer = getOption("Melee Prayer"),
            rangedPrayer = getOption("Ranged Prayer"),
            shouldAlch = getOption("Alch Items"),
            lootAshes = getOption("Loot Ashes"),
            useHouse = getOption("Use POH Pool")
        )
        localLogger.info("User Configuration: $configuration")

        addPaint(PaintUtils.createPaint(this))
    }

    @Subscribe
    fun onNpcAnimationChanged(event: NpcAnimationChangedEvent) {
        val npc = event.npc

        val interactingGorilla = CombatUtils.getInteractingGorilla()
        if (npc == State.targetGorilla || npc == interactingGorilla) {
            val animation = event.animation

            when (animation) {
                Constants.DEATH_ANIMATION_ID -> {
                    State.lootTile = npc.trueTile()
                    localLogger.info("Gorilla died; setting loot tile to ${State.lootTile}")

                    State.targetGorilla = null
                    State.shouldLoot = true
                    State.lootCounter = 50
                    State.attackCounter = 0
                    State.killCount += 1
                }

                Constants.MELEE_ATTACK_ANIMATION_ID -> {
                    State.gorillaAttackStyle = Constants.AttackStyles.MELEE
                }

                Constants.RANGED_ATTACK_ANIMATION_ID -> {
                    State.gorillaAttackStyle = Constants.AttackStyles.RANGED
                }

                Constants.MAGIC_ATTACK_ANIMATION_ID -> {
                    State.gorillaAttackStyle = Constants.AttackStyles.MAGIC
                }
            }
        }
    }

    @ValueChanged("Combat Potion Type")
    fun onCombatPotionTypeChange(newValue: String) {
        updateVisibility("Combat Potion Amount", newValue != "None")
    }

    @ValueChanged("Ranging Potion Type")
    fun onRangingPotionTypeChange(newValue: String) {
        updateVisibility("Ranging Potion Amount", newValue != "None")
    }

    @Subscribe
    fun onTick(event: TickEvent) {
        if (State.shouldLoot) {
            State.lootCounter -= 1

            if (State.lootCounter <= 0) {
                localLogger.info("Loot time finished; clearing tile")
                State.shouldLoot = false
                State.lootCounter = 0
            }
        }

        State.potionDelay = maxOf(State.potionDelay - 1, 0)
        State.foodDelay = maxOf(State.foodDelay - 1, 0)
        State.roarDelay = maxOf(State.roarDelay - 1, 0)
    }

    @Subscribe
    fun onProjectile(event: ProjectileDestinationChangedEvent) {
        if (event.id == Constants.BOULDER_PROJECTILE_ID) {
            State.projectiles.add(event.projectile)
        }

        State.projectiles.removeIf { projectile ->
            projectile.cycleEnd < Game.cycle()
        }
    }

    @Subscribe
    fun onBreakEvent(event: BreakEvent) {
        localLogger.info("Got BREAK event")
        State.shouldBreak = true
        if (Utils.atGrandTree() || Utils.playerAtSafespot()) {
            event.accept()
        } else {
            localLogger.info("Not in a safespot; delaying break by 5 seconds")
            event.delay(5000)
        }
    }

    @Subscribe
    fun onBreakEnd(event: BreakEndedEvent) {
        localLogger.info("Got BREAK END event")
        State.shouldBreak = false
    }
}

fun main() {
    ScriptUploader().uploadAndStart("rGorillas", "Rywhey", "localhost:5655", true, false)
}