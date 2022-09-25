package de.enzaxd.viaforge.injection.mixin.entity;

import de.enzaxd.viaforge.equals.ProtocolEquals;
import de.enzaxd.viaforge.equals.VersionList;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @ModifyConstant(method = "getActiveEyeHeight", constant = @Constant(floatValue = 1.27f))
    private float modifySneakEyeHeight(float prevEyeHeight) {
        if (ProtocolEquals.isNewerTo(VersionList.R1_13_2))
            return prevEyeHeight;
        else
            return 1.54f;
    }

    @Inject(method = "getAttackCooldownProgress", at = @At("HEAD"), cancellable = true)
    private void injectGetAttackCooldownProgress(CallbackInfoReturnable<Float> ci) {
        if (ProtocolEquals.isOlderOrEqualTo(VersionList.R1_8))
            ci.setReturnValue(1f);
    }
}