package net.tarasandedevelopment.tarasande.injection.mixin.feature.clientvalue;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityStatuses;
import net.tarasandedevelopment.tarasande.feature.clientvalue.impl.DebugValues;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

    @Inject(method = "getPermissionLevel",at = @At("HEAD"), cancellable = true)
    public void forcePermissions(CallbackInfoReturnable<Integer> cir) {
        if(DebugValues.INSTANCE.getForcePermissionLevel().getValue())
            cir.setReturnValue((int) DebugValues.INSTANCE.getPermissionLevel().getValue() - EntityStatuses.SET_OP_LEVEL_0);
    }

}