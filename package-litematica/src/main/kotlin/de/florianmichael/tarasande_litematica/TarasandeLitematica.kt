package de.florianmichael.tarasande_litematica

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.tarasandedevelopment.tarasande.event.EventSuccessfulLoad
import de.florianmichael.tarasande_litematica.screenextension.ScreenExtensionButtonListGuiMainMenu
import net.tarasandedevelopment.tarasande.system.screen.screenextensionsystem.ManagerScreenExtension
import su.mandora.event.EventDispatcher
import java.util.logging.Logger

class TarasandeLitematica : ClientModInitializer {
    private val logger = Logger.getLogger("tarasande-litematica")!!

    override fun onInitializeClient() {
        if (!FabricLoader.getInstance().isModLoaded("litematica")) {
            logger.warning("tarasande Litematica is not designed to run without 'Litematica' installed")
            return
        }
        EventDispatcher.add(EventSuccessfulLoad::class.java) {
            ManagerScreenExtension.add(ScreenExtensionButtonListGuiMainMenu())
        }
    }
}