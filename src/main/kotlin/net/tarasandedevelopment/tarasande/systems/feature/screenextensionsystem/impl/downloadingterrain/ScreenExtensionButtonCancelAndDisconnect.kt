package net.tarasandedevelopment.tarasande.systems.feature.screenextensionsystem.impl.downloadingterrain

import net.minecraft.client.gui.screen.DownloadingTerrainScreen
import net.tarasandedevelopment.tarasande.systems.feature.screenextensionsystem.ScreenExtensionButton
import net.tarasandedevelopment.tarasande.util.player.PlayerUtil
import net.tarasandedevelopment.tarasande.util.render.helper.Alignment

class ScreenExtensionButtonCancelAndDisconnect : ScreenExtensionButton<DownloadingTerrainScreen>("Cancel and disconnect", DownloadingTerrainScreen::class.java, alignment = Alignment.MIDDLE) {

    override fun onClick(current: DownloadingTerrainScreen) {
        PlayerUtil.disconnect()
    }
}
