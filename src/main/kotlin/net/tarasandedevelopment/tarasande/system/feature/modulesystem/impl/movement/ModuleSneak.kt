package net.tarasandedevelopment.tarasande.system.feature.modulesystem.impl.movement

import net.tarasandedevelopment.tarasande.event.EventInput
import net.tarasandedevelopment.tarasande.event.EventKeyBindingIsPressed
import net.tarasandedevelopment.tarasande.system.base.valuesystem.impl.ValueBoolean
import net.tarasandedevelopment.tarasande.system.feature.modulesystem.Module
import net.tarasandedevelopment.tarasande.system.feature.modulesystem.ModuleCategory
import net.tarasandedevelopment.tarasande.util.player.PlayerUtil

class ModuleSneak : Module("Sneak", "Automatically sneaks", ModuleCategory.MOVEMENT) {

    private val standStill = ValueBoolean(this, "Stand still", false)
    private val dontSlowdown = ValueBoolean(this, "Don't slowdown", false)

    init {
        registerEvent(EventKeyBindingIsPressed::class.java) { event ->
            if (event.keyBinding == mc.options.sneakKey)
                event.pressed = event.pressed || !standStill.value || !PlayerUtil.isPlayerMoving()
        }

        registerEvent(EventInput::class.java) { event ->
            if (event.input == mc.player?.input)
                if (dontSlowdown.value)
                    event.slowDown = false
        }
    }
}
