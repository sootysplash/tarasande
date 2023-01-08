package net.tarasandedevelopment.tarasande.system.feature.modulesystem.impl.player

import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorItem
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.math.Vec2f
import net.tarasandedevelopment.tarasande.event.EventScreenInput
import net.tarasandedevelopment.tarasande.system.base.valuesystem.impl.ValueBoolean
import net.tarasandedevelopment.tarasande.system.base.valuesystem.impl.ValueNumber
import net.tarasandedevelopment.tarasande.system.base.valuesystem.impl.ValueNumberRange
import net.tarasandedevelopment.tarasande.system.feature.modulesystem.Module
import net.tarasandedevelopment.tarasande.system.feature.modulesystem.ModuleCategory
import net.tarasandedevelopment.tarasande.util.extension.mc
import net.tarasandedevelopment.tarasande.util.math.TimeUtil
import net.tarasandedevelopment.tarasande.util.player.container.ContainerUtil
import org.lwjgl.glfw.GLFW
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.sqrt

class ModuleAutoArmor : Module("Auto armor", "Equips armor if none is equipped", ModuleCategory.PLAYER) {
    private val openInventory = ValueBoolean(this, "Open inventory", true)
    private val delay = ValueNumberRange(this, "Delay", 0.0, 100.0, 200.0, 500.0, 1.0)
    private val openDelay = object : ValueNumber(this, "Open delay", 0.0, 100.0, 500.0, 1.0) {
        override fun isEnabled() = openInventory.value
    }
    private val randomize = ValueNumber(this, "Randomize", 0.0, 0.0, 30.0, 1.0)

    private val timeUtil = TimeUtil()

    private var wasClosed = true
    private var mousePos: Vec2f? = null
    private var nextDelay: Long = 0

    init {
        registerEvent(EventScreenInput::class.java, 999) { event ->
            if (event.doneInput)
                return@registerEvent

            if (openInventory.value && mc.currentScreen !is AbstractInventoryScreen<*>) {
                timeUtil.reset()
                wasClosed = true
                mousePos = null
                return@registerEvent
            }

            val accessor = mc.currentScreen as HandledScreen<*>

            val screenHandler = mc.player?.playerScreenHandler!!

            if(screenHandler.cursorStack?.isEmpty == false)
                return@registerEvent

            if (mousePos == null) {
                mousePos = Vec2f(mc.window.scaledWidth / 2f, mc.window.scaledHeight / 2f)
            }

            val bestArmors = EquipmentSlot.values()
                .filter { it.isArmorSlot }
                .filter { ContainerUtil.getEquipmentSlot(screenHandler, it)?.hasStack() != true }
                .mapNotNull { equipmentSlot ->
                    ContainerUtil.getValidSlots(screenHandler)
                        .filter { it.stack.item is ArmorItem && (it.stack.item as ArmorItem).slotType == equipmentSlot }
                        .maxByOrNull { (it.stack.item as ArmorItem).protection + EnchantmentHelper.get(it.stack).values.sum() }
                }

            if(bestArmors.isNotEmpty())
                event.doneInput = true

            val nextSlot = ContainerUtil.getClosestSlot(screenHandler, accessor, mousePos!!) { slot, _ -> bestArmors.contains(slot) }

            if (!timeUtil.hasReached(
                    if (wasClosed && !openInventory.value)
                        openDelay.value.toLong()
                    else nextDelay
                ))
                return@registerEvent

            wasClosed = false
            timeUtil.reset()

            if (nextSlot != null) {
                val displayPos = ContainerUtil.getDisplayPosition(accessor, nextSlot).add(Vec2f(
                    if (randomize.value == 0.0) 0.0F else ThreadLocalRandom.current().nextDouble(-randomize.value, randomize.value).toFloat(),
                    if (randomize.value == 0.0) 0.0F else ThreadLocalRandom.current().nextDouble(-randomize.value, randomize.value).toFloat()
                ))
                val distance = mousePos?.distanceSquared(displayPos)!!
                mousePos = displayPos
                val mapped = sqrt(distance).div(Vec2f(accessor.backgroundWidth.toFloat(), accessor.backgroundHeight.toFloat()).length())
                nextDelay = (delay.minValue + (delay.maxValue - delay.minValue) * mapped).toLong()
                mc.interactionManager?.clickSlot(screenHandler.syncId, nextSlot.id, GLFW.GLFW_MOUSE_BUTTON_LEFT, SlotActionType.QUICK_MOVE, mc.player)
            }
        }
    }
}