package net.tarasandedevelopment.tarasande.system.feature.modulesystem.impl.render

import net.minecraft.util.math.MathHelper
import net.tarasandedevelopment.tarasande.event.EventMovementFovMultiplier
import net.tarasandedevelopment.tarasande.system.base.valuesystem.impl.ValueBoolean
import net.tarasandedevelopment.tarasande.system.base.valuesystem.impl.ValueNumberRange
import net.tarasandedevelopment.tarasande.system.feature.modulesystem.Module
import net.tarasandedevelopment.tarasande.system.feature.modulesystem.ModuleCategory
import net.tarasandedevelopment.tarasande.util.player.PlayerUtil

class ModuleNoFOV : Module("No FOV", "Limits the dynamic FOV", ModuleCategory.RENDER) {

    private val limit = ValueNumberRange(this, "Limit", 0.1, 1.0, 1.15, 1.5, 0.01)
    private val resetWhenIdling = ValueBoolean(this, "Reset when idling", true)

    init {
        registerEvent(EventMovementFovMultiplier::class.java) { event ->
            if (resetWhenIdling.value && !(PlayerUtil.isPlayerMoving() && mc.player?.isSprinting == true))
                event.movementFovMultiplier = 1.0F
            else
                event.movementFovMultiplier = MathHelper.clamp(event.movementFovMultiplier, limit.minValue.toFloat(), limit.maxValue.toFloat())
        }
    }

}