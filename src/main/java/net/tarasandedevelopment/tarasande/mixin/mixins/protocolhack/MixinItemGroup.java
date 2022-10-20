package net.tarasandedevelopment.tarasande.mixin.mixins.protocolhack;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.viaprotocolhack.util.VersionList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.tarasandedevelopment.tarasande.TarasandeMain;
import net.tarasandedevelopment.tarasande.protocol.util.ItemSplitter;
import net.tarasandedevelopment.tarasande.screen.clientmenu.ElementMenuScreenProtocolHack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemGroup.class)
public class MixinItemGroup {

    @Redirect(method = "appendStacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;appendStacks(Lnet/minecraft/item/ItemGroup;Lnet/minecraft/util/collection/DefaultedList;)V"))
    public void removeUseless(Item instance, ItemGroup group, DefaultedList<ItemStack> stacks) {
        if (ItemSplitter.INSTANCE.getSplitter().containsKey(instance) && TarasandeMain.Companion.get().getManagerClientMenu().get(ElementMenuScreenProtocolHack.class).getFilterItemGroups().getValue()) {
            final ProtocolVersion version = ItemSplitter.INSTANCE.getSplitter().get(instance);

            if (VersionList.isNewerTo(version)) {
                instance.appendStacks(group, stacks);
            }
            return;
        }
        instance.appendStacks(group, stacks); // in case an item has mo napping
    }
}