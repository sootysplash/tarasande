package net.tarasandedevelopment.tarasande.system.feature.modulesystem.impl.render

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.registry.Registries
import net.minecraft.util.math.BlockPos
import net.tarasandedevelopment.tarasande.event.EventRender3D
import net.tarasandedevelopment.tarasande.event.EventRenderBlockModel
import net.tarasandedevelopment.tarasande.system.base.valuesystem.impl.ValueBoolean
import net.tarasandedevelopment.tarasande.system.base.valuesystem.impl.ValueColor
import net.tarasandedevelopment.tarasande.system.base.valuesystem.impl.ValueRegistry
import net.tarasandedevelopment.tarasande.system.feature.modulesystem.Module
import net.tarasandedevelopment.tarasande.system.feature.modulesystem.ModuleCategory
import net.tarasandedevelopment.tarasande.util.extension.mc
import net.tarasandedevelopment.tarasande.util.extension.minecraft.boundingBox
import net.tarasandedevelopment.tarasande.util.render.RenderUtil
import java.util.concurrent.CopyOnWriteArrayList

class ModuleBlockESP : Module("Block ESP", "Highlights blocks through walls", ModuleCategory.RENDER) {

    private val hideBlocks = object : ValueBoolean(this, "Hide blocks", false) {
        override fun onChange() = onDisable()
    }
    private val color = object : ValueColor(this, "Color", 0.0, 1.0, 1.0, 1.0) {
        override fun isEnabled() = !hideBlocks.value
    }
    private val blocks = object : ValueRegistry<Block>(this, "Blocks", Registries.BLOCK) {
        override fun onChange() = onDisable()
        override fun filter(key: Block) = key != Blocks.AIR
        override fun getTranslationKey(key: Any?) = (key as Block).translationKey
    }

    private val list = CopyOnWriteArrayList<Pair<BlockPos, BlockState>>()

    override fun onEnable() {
        mc.worldRenderer.reload()
    }

    override fun onDisable() {
        if (!enabled)
            return
        list.clear()
        mc.worldRenderer.reload()
    }

    init {
        registerEvent(EventRenderBlockModel::class.java) { event ->
            if (!blocks.list.contains(event.state.block)) {
                if (hideBlocks.value) {
                    event.cancelled = true
                }
            } else if (!hideBlocks.value) {
                if (list.none { it.first == event.pos })
                    list.add(Pair(BlockPos(event.pos), event.state))
            }
        }

        registerEvent(EventRender3D::class.java) { event ->
            list.removeIf { mc.world?.getBlockState(it.first)?.block != it.second.block }
            for (pair in list) {
                val pos = pair.first
                val shape = pair.second.getOutlineShape(mc.world, pos)?.offset(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
                if (shape != null) {
                    RenderUtil.blockOutline(event.matrices, shape.boundingBox(), color.getColor().rgb)
                } else {
                    list.remove(pair)
                }
            }
        }
    }
}