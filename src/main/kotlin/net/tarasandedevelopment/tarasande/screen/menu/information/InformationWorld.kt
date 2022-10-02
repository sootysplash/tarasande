package net.tarasandedevelopment.tarasande.screen.menu.information

import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket
import net.tarasandedevelopment.tarasande.TarasandeMain
import net.tarasandedevelopment.tarasande.base.event.Event
import net.tarasandedevelopment.tarasande.base.screen.menu.information.Information
import net.tarasandedevelopment.tarasande.event.EventPacket
import java.util.function.Consumer

class InformationWorldTime : Information("World", "World Time") {

    private var lastUpdate: Pair<Long, Long>? = null

    init {
        TarasandeMain.get().managerEvent.add(Pair(1, Consumer<Event> { event ->
            if (event is EventPacket) {
                if (event.type == EventPacket.Type.RECEIVE && event.packet is WorldTimeUpdateS2CPacket) {
                    lastUpdate = Pair(event.packet.timeOfDay, event.packet.time)
                }
            }
        }))
    }

    override fun getMessage(): String? {
        if (MinecraftClient.getInstance().world == null)
            return null
        if (lastUpdate == null)
            return null
        return lastUpdate?.first.toString() + "/" + lastUpdate?.second
    }
}