package net.tarasandedevelopment.tarasande.systems.feature.modulesystem.impl.movement

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShapes
import net.tarasandedevelopment.tarasande.event.EventCollisionShape
import net.tarasandedevelopment.tarasande.event.EventUpdate
import net.tarasandedevelopment.tarasande.systems.base.valuesystem.impl.ValueNumber
import net.tarasandedevelopment.tarasande.systems.feature.modulesystem.Module
import net.tarasandedevelopment.tarasande.systems.feature.modulesystem.ModuleCategory

class ModuleJesus : Module("Jesus", "Lets you walk on liquids", ModuleCategory.MOVEMENT) {

    private val height = ValueNumber(this, "Height", 0.0, 1.0, 1.0, 0.01)
    private val jumpMotion = ValueNumber(this, "Jump motion", 0.0, 0.0, 1.0, 0.01)

    init {
        registerEvent(EventCollisionShape::class.java) { event ->
            if (mc.player == null)
                return@registerEvent
            val inLiquid = mc.world?.getBlockState(BlockPos(mc.player?.pos?.add(0.0, 0.5, 0.0)))?.fluidState?.isEmpty == false
            if (inLiquid || mc.player?.input?.sneaking == true || mc.player?.fallDistance!! > 3.0)
                return@registerEvent
            if (event.pos.y < mc.player?.y!!)
                if (mc.world?.getBlockState(event.pos)?.fluidState?.isEmpty == false) {
                    event.collisionShape = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, height.value * (mc.world?.getBlockState(event.pos)?.fluidState?.height?.toDouble() ?: 1.0), 1.0)
                }
        }

        registerEvent(EventUpdate::class.java) { event ->
            if (event.state == EventUpdate.State.PRE) {
                if (mc.player?.input?.sneaking == true || mc.player?.fallDistance!! > 3.0)
                    return@registerEvent
                if (mc.player?.isTouchingWater == true || mc.player?.isInLava == true)
                    if (mc.world?.getBlockState(BlockPos(mc.player?.pos?.add(0.0, -0.01, 0.0)))?.fluidState?.isEmpty == false)
                        if (jumpMotion.value > 0.0) {
                            val prevVelocity = mc.player?.velocity?.add(0.0, 0.0, 0.0)!!
                            mc.player?.jump()
                            mc.player?.velocity = prevVelocity.withAxis(Direction.Axis.Y, mc.player?.velocity?.y?.times(jumpMotion.value)!!)
                        }
            }
        }
    }

}