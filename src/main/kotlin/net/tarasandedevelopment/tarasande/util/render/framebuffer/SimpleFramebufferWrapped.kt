package net.tarasandedevelopment.tarasande.util.render.framebuffer

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.SimpleFramebuffer
import net.tarasandedevelopment.tarasande.event.EventResolutionUpdate
import su.mandora.event.EventDispatcher

class SimpleFramebufferWrapped : SimpleFramebuffer(MinecraftClient.getInstance().window.framebufferWidth, MinecraftClient.getInstance().window.framebufferHeight, true, MinecraftClient.IS_SYSTEM_MAC) {

    init {
        setClearColor(0.0F, 0.0F, 0.0F, 0.0F)

        EventDispatcher.add(EventResolutionUpdate::class.java) {
            resize(MinecraftClient.getInstance().window.framebufferWidth, MinecraftClient.getInstance().window.framebufferHeight, MinecraftClient.IS_SYSTEM_MAC)
        }
    }

}