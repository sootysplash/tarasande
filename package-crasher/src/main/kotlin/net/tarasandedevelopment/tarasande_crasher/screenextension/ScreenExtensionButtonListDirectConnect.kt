package net.tarasandedevelopment.tarasande_crasher.screenextension

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.DirectConnectScreen
import net.minecraft.client.network.ServerAddress
import net.tarasandedevelopment.tarasande.system.screen.panelsystem.screen.impl.ScreenBetterOwnerValues
import net.tarasandedevelopment.tarasande.system.screen.screenextensionsystem.ScreenExtensionButtonList
import net.tarasandedevelopment.tarasande_crasher.crasher.ManagerCrasher
import org.lwjgl.glfw.GLFW

class ScreenExtensionButtonListDirectConnect(crashSystem: ManagerCrasher) : ScreenExtensionButtonList<DirectConnectScreen>(DirectConnectScreen::class.java) {

    init {
        for (crasher in crashSystem.list) {
            val name = crasher.name + " Crasher"
            add(name, direction = Direction.RIGHT) {
                if (it == GLFW.GLFW_MOUSE_BUTTON_LEFT || it == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                    try {
                        ServerAddress.parse((MinecraftClient.getInstance().currentScreen as DirectConnectScreen).addressField.text).apply {
                            crasher.crash(address, port)
                        }
                    } catch (_: Exception) {
                    }
                } else if (it == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    MinecraftClient.getInstance().setScreen(ScreenBetterOwnerValues(MinecraftClient.getInstance().currentScreen!!, name, crasher))
                }
            }
        }
    }
}