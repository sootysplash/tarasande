package net.tarasandedevelopment.tarasande.mixin.mixins.core.command;

import net.minecraft.SharedConstants;
import net.tarasandedevelopment.tarasande.TarasandeMain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SharedConstants.class)
public class MixinSharedConstants {

    @Inject(method = "isValidChar", at = @At("HEAD"), cancellable = true)
    private static void hookClientValue(char chr, CallbackInfoReturnable<Boolean> cir) {
        if (TarasandeMain.Companion.get().getClientValues().getAllowEveryCharacter().getValue()) {
            cir.setReturnValue(true);
        }
    }
}