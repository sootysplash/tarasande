package net.tarasandedevelopment.tarasande.system.feature.modulesystem.impl.player

import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.AxeItem
import net.minecraft.item.SwordItem
import net.minecraft.util.hit.BlockHitResult
import net.tarasandedevelopment.tarasande.TarasandeMain
import net.tarasandedevelopment.tarasande.event.EventAttackEntity
import net.tarasandedevelopment.tarasande.event.EventUpdate
import net.tarasandedevelopment.tarasande.system.base.valuesystem.impl.ValueBoolean
import net.tarasandedevelopment.tarasande.system.base.valuesystem.impl.ValueMode
import net.tarasandedevelopment.tarasande.system.feature.modulesystem.Module
import net.tarasandedevelopment.tarasande.system.feature.modulesystem.ModuleCategory
import net.tarasandedevelopment.tarasande.system.feature.modulesystem.impl.combat.ModuleHealingBot
import net.tarasandedevelopment.tarasande.util.extension.mc
import net.tarasandedevelopment.tarasande.util.player.PlayerUtil
import net.tarasandedevelopment.tarasande.util.player.container.ContainerUtil

class ModuleAutoTool : Module("Auto tool", "Selects the best tool for breaking a block", ModuleCategory.PLAYER) {

    val mode = ValueMode(this, "Mode", true, "Blocks", "Entities")
    val useAxeToCounterBlocking = object : ValueBoolean(this, "Use axe to counter blocking", false) {
        override fun isEnabled() = mode.isSelected(1)
    }

    init {
        registerEvent(EventUpdate::class.java) { event ->
            if (event.state == EventUpdate.State.PRE) {
                if (!mode.isSelected(0))
                    return@registerEvent
                if (!mc.interactionManager?.isBreakingBlock!!)
                    return@registerEvent
                if (mc.crosshairTarget !is BlockHitResult)
                    return@registerEvent
                val blockPos = (mc.crosshairTarget as BlockHitResult).blockPos
                val pair = PlayerUtil.getBreakSpeed(blockPos)

                if (pair.second == mc.player?.inventory?.selectedSlot)
                    return@registerEvent

                val currentSpeed = PlayerUtil.getBreakSpeed(blockPos, mc.player?.inventory?.selectedSlot ?: return@registerEvent)
                if (currentSpeed == pair.first)
                    return@registerEvent

                val bestTool = pair.second
                if (bestTool == -1)
                    return@registerEvent
                mc.player?.inventory?.selectedSlot = bestTool
            }
        }

        registerEvent(EventAttackEntity::class.java) { event ->
            if (event.state == EventAttackEntity.State.PRE) {
                if (!mode.isSelected(1))
                    return@registerEvent
                if (TarasandeMain.managerModule().get(ModuleHealingBot::class.java).let { it.enabled && it.state != ModuleHealingBot.State.IDLE })
                    return@registerEvent

                var best: Int? = null
                var score = 0.0F
                for (i in 0..8) {
                    val stack = mc.player?.inventory?.main?.get(i)
                    if (stack != null && !stack.isEmpty) {
                        val newScore = ContainerUtil.wrapMaterialDamage(stack) + EnchantmentHelper.get(stack).values.sum()

                        if (useAxeToCounterBlocking.value && event.entity is PlayerEntity && event.entity.isBlocking && mc.player?.inventory?.main?.subList(0, 8)?.any { it.item is AxeItem } == true) {
                            if (stack.item !is AxeItem)
                                continue
                        } else if (stack.item !is SwordItem)
                            continue

                        if (best == null || newScore > score) {
                            best = i
                            score = newScore
                        }
                    }
                }

                if (best != null)
                    mc.player?.inventory?.selectedSlot = best
            }
        }
    }
}