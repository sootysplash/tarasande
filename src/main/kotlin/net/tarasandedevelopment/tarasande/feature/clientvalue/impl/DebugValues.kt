package net.tarasandedevelopment.tarasande.feature.clientvalue.impl

import net.minecraft.entity.EntityStatuses
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket
import net.tarasandedevelopment.tarasande.event.EventPacket
import net.tarasandedevelopment.tarasande.feature.clientvalue.impl.debug.BlockChangeTracker
import net.tarasandedevelopment.tarasande.feature.clientvalue.impl.debug.Chat
import net.tarasandedevelopment.tarasande.feature.clientvalue.impl.debug.MinecraftDebugger
import net.tarasandedevelopment.tarasande.feature.clientvalue.impl.debug.PlayerMovementPrediction
import net.tarasandedevelopment.tarasande.feature.clientvalue.impl.debug.camera.Camera
import net.tarasandedevelopment.tarasande.system.base.valuesystem.impl.ValueBoolean
import net.tarasandedevelopment.tarasande.system.base.valuesystem.impl.ValueNumber
import net.tarasandedevelopment.tarasande.system.base.valuesystem.impl.meta.abstracted.ValueButtonOwnerValues
import su.mandora.event.EventDispatcher

object DebugValues {

    val blockChangeTracker = ValueButtonOwnerValues(this, "Block change tracker", BlockChangeTracker)

    init {
        ValueButtonOwnerValues(this, "Minecraft debugger", MinecraftDebugger)
        ValueButtonOwnerValues(this, "Player movement prediction", PlayerMovementPrediction)
    }

    val openGLErrorDebugger = ValueBoolean(this, "OpenGL error debugger", true)
    val disableInterpolation = ValueBoolean(this, "Disable interpolation", false)
    private val ignoreResourcePackHash = ValueBoolean(this, "Ignore resource pack hash", false)
    val camera = ValueButtonOwnerValues(this, "Camera", Camera)
    val eliminateHitDelay = ValueBoolean(this, "Eliminate hit delay", false)
    val chat = ValueButtonOwnerValues(this, "Chat", Chat)
    val forcePermissionLevel = ValueBoolean(this, "Force permission level", false)
    // TODO Auto Update
    val permissionLevel = object : ValueNumber(this, "Permission level", EntityStatuses.SET_OP_LEVEL_0.toDouble(), EntityStatuses.SET_OP_LEVEL_4.toDouble(), EntityStatuses.SET_OP_LEVEL_4.toDouble(), 1.0) {
        override fun isEnabled() = forcePermissionLevel.value
    }

    init {
        EventDispatcher.add(EventPacket::class.java) { event ->
            if (!ignoreResourcePackHash.value) return@add

            if (event.type == EventPacket.Type.RECEIVE && event.packet is ResourcePackSendS2CPacket) {
                event.packet.hash = "" // The client ignores the hash if it is not 40 characters long
            }
        }
    }
}
