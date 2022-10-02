package net.tarasandedevelopment.tarasande.module.movement

import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
import net.tarasandedevelopment.tarasande.base.event.Event
import net.tarasandedevelopment.tarasande.base.event.Priority
import net.tarasandedevelopment.tarasande.base.module.Module
import net.tarasandedevelopment.tarasande.base.module.ModuleCategory
import net.tarasandedevelopment.tarasande.event.EventPacket
import net.tarasandedevelopment.tarasande.event.EventPollEvents
import net.tarasandedevelopment.tarasande.event.EventRotationSet
import net.tarasandedevelopment.tarasande.util.math.rotation.Rotation
import net.tarasandedevelopment.tarasande.util.math.rotation.RotationUtil
import net.tarasandedevelopment.tarasande.value.ValueNumberRange
import java.util.function.Consumer

class ModuleNoRotate : Module("No rotate", "Prevents the server from rotating you", ModuleCategory.MOVEMENT) {

    private val recoverSpeed = ValueNumberRange(this, "Recover speed", 0.1, 1.0, 1.0, 1.0, 0.1)

    private var prevRotation: Rotation? = null
    private var rotation: Rotation? = null

    @Priority(1) // the rotation should always be overridden
    val eventConsumer = Consumer<Event> { event ->
        when (event) {
            is EventPacket -> {
                if (event.type == EventPacket.Type.RECEIVE && event.packet is PlayerPositionLookS2CPacket) {
                    if(mc.player != null) {
                        prevRotation = Rotation(mc.player!!)
                        if (RotationUtil.fakeRotation == null) // if this isn't the case the rotation is being handled by the RotationUtil
                            rotation = RotationUtil.evaluateNewRotation(event.packet)
                    }
                }
            }

            is EventPollEvents -> {
                if (rotation != null) {
                    event.rotation = rotation!!
                    event.minRotateToOriginSpeed = recoverSpeed.minValue
                    event.maxRotateToOriginSpeed = recoverSpeed.maxValue

                    rotation = null
                }
            }

            is EventRotationSet -> {
                if(prevRotation != null) {
                    mc.player?.yaw = prevRotation?.yaw!!
                    mc.player?.pitch = prevRotation?.pitch!!
                }
            }
        }
    }

}