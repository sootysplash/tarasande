package net.tarasandedevelopment.tarasande.system.base.valuesystem.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.tarasandedevelopment.tarasande.system.base.valuesystem.Value
import net.tarasandedevelopment.tarasande.system.base.valuesystem.valuecomponent.impl.ElementWidthValueComponentNumber

open class ValueNumber(owner: Any, name: String, val min: Double, var value: Double, val max: Double, val increment: Double, val exceed: Boolean = true, manage: Boolean = true) : Value(owner, name, ElementWidthValueComponentNumber::class.java, manage) {
    override fun save(): JsonElement {
        return JsonPrimitive(value)
    }

    override fun load(jsonElement: JsonElement) {
        value = jsonElement.asDouble
    }
}
