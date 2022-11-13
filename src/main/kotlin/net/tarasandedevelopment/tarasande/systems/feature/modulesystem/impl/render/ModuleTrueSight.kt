package net.tarasandedevelopment.tarasande.systems.feature.modulesystem.impl.render

import net.minecraft.entity.Entity
import net.tarasandedevelopment.tarasande.event.EventEntityFlag
import net.tarasandedevelopment.tarasande.systems.base.valuesystem.impl.ValueNumber
import net.tarasandedevelopment.tarasande.systems.feature.modulesystem.Module
import net.tarasandedevelopment.tarasande.systems.feature.modulesystem.ModuleCategory

class ModuleTrueSight : Module("True sight", "Makes invisible players visible", ModuleCategory.RENDER) {

    val alpha = ValueNumber(this, "Alpha", 0.0, 0.15, 1.0, 0.01)

    init {
        registerEvent(EventEntityFlag::class.java) { event ->
            if (event.flag == Entity.INVISIBLE_FLAG_INDEX)
                event.enabled = false
        }
    }
}
