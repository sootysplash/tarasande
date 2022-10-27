package net.tarasandedevelopment.tarasande.mixin.mixins.features.module;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.tarasandedevelopment.tarasande.TarasandeMain;
import net.tarasandedevelopment.tarasande.module.qualityoflife.ModuleNoMessageSignatureIndicator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChatHud.class)
public class MixinChatHud {

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;indicator()Lnet/minecraft/client/gui/hud/MessageIndicator;"))
    public MessageIndicator noMessageSignatureIndicator_Cancel(ChatHudLine.Visible instance) {
        if (TarasandeMain.Companion.get().getManagerModule().get(ModuleNoMessageSignatureIndicator.class).getEnabled()) {
            return null;
        }
        return instance.indicator();
    }
}
