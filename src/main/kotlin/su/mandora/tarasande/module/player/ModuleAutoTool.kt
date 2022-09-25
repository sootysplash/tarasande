package su.mandora.tarasande.module.player

import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import su.mandora.tarasande.base.event.Event
import su.mandora.tarasande.base.module.Module
import su.mandora.tarasande.base.module.ModuleCategory
import su.mandora.tarasande.event.EventUpdate
import su.mandora.tarasande.module.render.ModuleBedESP
import java.util.function.Consumer

class ModuleAutoTool : Module("Auto tool", "Selects the best tool for breaking a block", ModuleCategory.PLAYER) {

    fun getBreakSpeed(blockPos: BlockPos): Double {
        return if (enabled)
            ModuleBedESP.Breaker.getBreakSpeed(blockPos).first
        else
            ModuleBedESP.Breaker.getBreakSpeed(blockPos, mc.player?.inventory?.selectedSlot!!)
    }


    val eventConsumer = Consumer<Event> { event ->
        if (event is EventUpdate && event.state == EventUpdate.State.PRE) {
            if (!mc.interactionManager?.isBreakingBlock!!)
                return@Consumer
            if (mc.crosshairTarget !is BlockHitResult)
                return@Consumer
            val blockPos = (mc.crosshairTarget as BlockHitResult).blockPos
            val pair = ModuleBedESP.Breaker.getBreakSpeed(blockPos)

            if (pair.second == mc.player?.inventory?.selectedSlot)
                return@Consumer

            val currentSpeed = ModuleBedESP.Breaker.getBreakSpeed(blockPos, mc.player?.inventory?.selectedSlot ?: return@Consumer)
            if (currentSpeed == pair.first)
                return@Consumer

            val bestTool = pair.second
            if (bestTool == -1)
                return@Consumer
            mc.player?.inventory?.selectedSlot = bestTool
        }
    }
}