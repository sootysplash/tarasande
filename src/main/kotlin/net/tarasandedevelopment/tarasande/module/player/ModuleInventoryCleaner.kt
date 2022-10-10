package net.tarasandedevelopment.tarasande.module.player

import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen
import net.minecraft.item.AirBlockItem
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.SlotActionType
import net.tarasandedevelopment.tarasande.base.event.Event
import net.tarasandedevelopment.tarasande.base.module.Module
import net.tarasandedevelopment.tarasande.base.module.ModuleCategory
import net.tarasandedevelopment.tarasande.event.EventPollEvents
import net.tarasandedevelopment.tarasande.value.ValueBoolean
import java.util.function.Consumer

class ModuleInventoryCleaner : Module("Inventory cleaner", "Drops items in your inventory", ModuleCategory.PLAYER) {

    private val openInventory = ValueBoolean(this, "Open inventory", true)

    val eventConsumer = Consumer<Event> { event ->
        if (event is EventPollEvents) {
            if (event.fake)
                return@Consumer

            if (openInventory.value && mc.currentScreen !is AbstractInventoryScreen<*>)
                return@Consumer

            val inventory = mc.player?.inventory?.main!!
            val screenHandler = mc.player?.playerScreenHandler

            for (slot in screenHandler?.slots!!) {
                if (slot != null && slot.isEnabled && slot.hasStack())
                    if (shouldDrop(slot.stack, inventory)) {
                        println(mc.player?.inventory?.getSlotWithStack(slot.stack)!!)
                        mc.interactionManager?.clickSlot(mc.player?.playerScreenHandler?.syncId!!, slot.id, 1 /* 1 = all; 0 = single */, SlotActionType.THROW, mc.player)
                        return@Consumer
                    }
            }
        }
    }

    private fun shouldDrop(stack: ItemStack, list: List<ItemStack>): Boolean {
        println(stack)
        return stack.item !is AirBlockItem
    }

}