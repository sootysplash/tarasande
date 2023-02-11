package de.florianmichael.tarasande_protocol_hack.provider

import com.viaversion.viaversion.api.connection.UserConnection
import com.viaversion.viaversion.api.minecraft.item.Item
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.HandItemProvider
import net.minecraft.item.ItemStack
import de.florianmichael.tarasande_protocol_hack.util.inventory.MinecraftViaItemRewriter

class FabricHandItemProvider : HandItemProvider() {

    companion object {
        var lastUsedItem: ItemStack? = null
    }

    override fun getHandItem(info: UserConnection): Item? {
        if (lastUsedItem == null) {
            return null
        }
        return MinecraftViaItemRewriter.minecraftToViaItem(info, lastUsedItem!!, ProtocolVersion.v1_8.originalVersion)
    }
}