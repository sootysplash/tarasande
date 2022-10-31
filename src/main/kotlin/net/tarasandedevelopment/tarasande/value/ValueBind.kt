package net.tarasandedevelopment.tarasande.value

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import net.minecraft.client.MinecraftClient
import net.tarasandedevelopment.tarasande.TarasandeMain
import net.tarasandedevelopment.tarasande.base.features.module.Module
import net.tarasandedevelopment.tarasande.base.value.Value
import net.tarasandedevelopment.tarasande.event.EventKey
import net.tarasandedevelopment.tarasande.event.EventMouse
import org.lwjgl.glfw.GLFW

open class ValueBind(owner: Any, name: String, var type: Type, var button: Int, var mouse: Boolean = true, manage: Boolean = true) : Value(owner, name, manage) {

    private var presses = 0

    init {
        TarasandeMain.get().managerEvent.also {
            it.add(EventMouse::class.java) {
                if (type == Type.MOUSE)
                    if (MinecraftClient.getInstance().currentScreen == null)
                        if (button == it.button)
                            if (it.action == GLFW.GLFW_PRESS) {
                                if (owner !is Module || this == owner.bind || owner.enabled)
                                    presses++
                            }
            }
            it.add(EventKey::class.java) {
                if (type == Type.KEY)
                    if (MinecraftClient.getInstance().currentScreen == null)
                        if (it.key == button)
                            if (it.action == GLFW.GLFW_PRESS) {
                                if (owner !is Module || this == owner.bind || owner.enabled)
                                    presses++
                            }
            }
        }
    }

    override fun save(): JsonElement {
        val jsonArray = JsonArray()
        jsonArray.add(type.ordinal)
        jsonArray.add(button)
        return jsonArray
    }

    override fun load(jsonElement: JsonElement) {
        val jsonArray = jsonElement.asJsonArray
        type = Type.values()[jsonArray.get(0).asInt]
        button = jsonArray.get(1).asInt
    }

    open fun filter(type: Type, bind: Int): Boolean {
        return true
    }

    fun wasPressed(): Int {
        val prev = presses
        presses = 0
        return prev
    }

    fun isPressed(ignoreScreen: Boolean = false): Boolean {
        if (button == GLFW.GLFW_KEY_UNKNOWN) return false
        if (MinecraftClient.getInstance().currentScreen != null && !ignoreScreen) return false

        return when (type) {
            Type.KEY -> GLFW.glfwGetKey(MinecraftClient.getInstance().window.handle, button) == GLFW.GLFW_PRESS
            Type.MOUSE -> GLFW.glfwGetMouseButton(MinecraftClient.getInstance().window.handle, button) == GLFW.GLFW_PRESS
        }
    }

    enum class Type {
        KEY, MOUSE
    }
}