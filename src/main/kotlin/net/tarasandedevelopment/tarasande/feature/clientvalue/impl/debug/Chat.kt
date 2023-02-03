package net.tarasandedevelopment.tarasande.feature.clientvalue.impl.debug

import net.tarasandedevelopment.tarasande.system.base.valuesystem.impl.ValueBoolean
import net.tarasandedevelopment.tarasande.system.base.valuesystem.impl.ValueMode

object Chat {

    val allowAllCharacters = ValueBoolean(this, "Allow all characters", true)
    val dontResetHistoryOnDisconnect = ValueBoolean(this, "Don't reset history on disconnect", true)
    val removeMaximum = ValueMode(this, "Remove maximum", true, "History", "Message field")
}