package net.tarasandedevelopment.tarasande.util.extension.minecraft.packet

import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
import net.tarasandedevelopment.tarasande.mc
import net.tarasandedevelopment.tarasande.util.math.rotation.Rotation

fun PlayerPositionLookS2CPacket.evaluateNewRotation(): Rotation {
    var j = yaw
    var k = pitch
    if (flags.contains(PlayerPositionLookS2CPacket.Flag.X_ROT)) {
        k += mc.player?.pitch!!
    }
    if (flags.contains(PlayerPositionLookS2CPacket.Flag.Y_ROT)) {
        j += mc.player?.yaw!!
    }
    return Rotation(j, k)
}