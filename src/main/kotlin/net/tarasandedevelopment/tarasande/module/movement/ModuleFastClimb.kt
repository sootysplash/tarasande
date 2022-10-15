package net.tarasandedevelopment.tarasande.module.movement

import net.tarasandedevelopment.tarasande.base.module.Module
import net.tarasandedevelopment.tarasande.base.module.ModuleCategory
import net.tarasandedevelopment.tarasande.value.ValueNumber

class ModuleFastClimb : Module("Fast climb", "Speeds up climbing movement", ModuleCategory.MOVEMENT) {
    val multiplier = ValueNumber(this, "Multiplier", 0.5, 1.0, 3.0, 0.1)
}