package net.tarasandedevelopment.tarasande.mixin.mixins.features.clientvalues;

import net.minecraft.client.util.telemetry.TelemetrySender;
import net.tarasandedevelopment.tarasande.TarasandeMain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TelemetrySender.class)
public class MixinTelemetrySender {

    @SuppressWarnings("InvalidInjectorMethodSignature") // Coerce is not supported cuz massive brain
    @Inject(method = "send(Lnet/minecraft/client/util/telemetry/TelemetrySender$PlayerGameMode;)V", at = @At("HEAD"), cancellable = true)
    public void disableTelemetry(@Coerce Object gameMode, CallbackInfo ci) {
        // This bypasses the TarasandeMain#disabled, because ms spying on us is a major problem
        if (TarasandeMain.Companion.clientValues().getDisableTelemetry().getValue()) {
            TarasandeMain.Companion.get().getLogger().info("Blocked telemetry services");
            ci.cancel();
        }
    }

}
