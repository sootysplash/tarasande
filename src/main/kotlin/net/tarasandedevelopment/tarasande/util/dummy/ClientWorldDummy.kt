package net.tarasandedevelopment.tarasande.util.dummy

import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.BlockPos

class ClientWorldDummy : ClientWorld(null, null, null, /*DynamicRegistryManager.BUILTIN.get().get(Registry.DIMENSION_TYPE_KEY).entryOf(DimensionTypes.OVERWORLD) TODO Port*/ null, 1, 1, null, null, false, 0L) {

    override fun calculateAmbientDarkness() {
    }

    override fun initWeatherGradients() {
    }

    override fun setSpawnPos(pos: BlockPos?, angle: Float) {
    }

    override fun getRegistryManager() = null// TODO Port; DynamicRegistryManager.BUILTIN.get()

    override fun getSpawnPos() = BlockPos(0, 0, 0)

    override fun getSpawnAngle() = 0F

    override fun getTimeOfDay() = 0L

    override fun getTime() = 0L
}
