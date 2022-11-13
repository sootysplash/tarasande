package net.tarasandedevelopment.tarasande.systems.feature.modulesystem.impl.misc

import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket
import net.tarasandedevelopment.tarasande.event.EventPacket
import net.tarasandedevelopment.tarasande.systems.base.valuesystem.impl.ValueBoolean
import net.tarasandedevelopment.tarasande.systems.base.valuesystem.impl.ValueMode
import net.tarasandedevelopment.tarasande.systems.feature.modulesystem.Module
import net.tarasandedevelopment.tarasande.systems.feature.modulesystem.ModuleCategory

class ModuleResourcePackSpoofer : Module("Resource pack spoofer", "Changes the response to resource pack packets", ModuleCategory.MISC) {

    private val ignoreInvalidProtocol = ValueBoolean(this, "Ignore invalid protocol", true)
    private val mode = ValueMode(this, "Mode", false, "Accept", "Decline")
    private val acceptMode = object : ValueMode(this, "Accept mode", false, "Successful loaded", "Fail download") {
        override fun isEnabled() = mode.isSelected(0)
    }

    init {
        registerEvent(EventPacket::class.java) { event ->
            if (event.type == EventPacket.Type.RECEIVE && event.packet is ResourcePackSendS2CPacket) {
                if (ignoreInvalidProtocol.value) {
                    if (ClientPlayNetworkHandler.resolveUrl(event.packet.url) == null)
                        return@registerEvent
                }

                when {
                    mode.isSelected(0) -> {
                        mc.networkHandler?.sendPacket(ResourcePackStatusC2SPacket(ResourcePackStatusC2SPacket.Status.ACCEPTED))
                        when {
                            acceptMode.isSelected(0) -> mc.networkHandler?.sendPacket(ResourcePackStatusC2SPacket(ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED))
                            acceptMode.isSelected(1) -> mc.networkHandler?.sendPacket(ResourcePackStatusC2SPacket(ResourcePackStatusC2SPacket.Status.FAILED_DOWNLOAD))
                        }
                    }

                    mode.isSelected(1) -> mc.networkHandler?.sendPacket(ResourcePackStatusC2SPacket(ResourcePackStatusC2SPacket.Status.DECLINED))
                }
                event.cancelled = true
            }
        }
    }
}
