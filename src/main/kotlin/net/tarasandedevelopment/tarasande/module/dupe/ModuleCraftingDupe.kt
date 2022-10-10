package net.tarasandedevelopment.tarasande.module.dupe

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.screen.slot.SlotActionType
import net.tarasandedevelopment.tarasande.base.module.Module
import net.tarasandedevelopment.tarasande.base.module.ModuleCategory
import net.tarasandedevelopment.tarasande.mixin.accessor.IInventoryScreen
import net.tarasandedevelopment.tarasande.util.exploit.ExploitInjector

class ModuleCraftingDupe : Module("Crafting dupe", "dupe via crafting grid on 1.17.0", ModuleCategory.DUPE) {

    init {
        ExploitInjector.hook(InventoryScreen::class.java, "Crafting Dupe 1.17.0", object : ExploitInjector.Action {
            override fun on() {
                val inventoryScreen = MinecraftClient.getInstance().currentScreen!! as InventoryScreen
                val outputSlot = inventoryScreen.screenHandler.getSlot(0)

                (inventoryScreen as IInventoryScreen).tarasande_onMouseClick(outputSlot, outputSlot.id, 0, SlotActionType.THROW)
            }
        }, this)
    }
}