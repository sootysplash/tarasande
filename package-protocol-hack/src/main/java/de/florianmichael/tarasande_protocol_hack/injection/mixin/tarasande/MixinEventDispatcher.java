package de.florianmichael.tarasande_protocol_hack.injection.mixin.tarasande;

import de.florianmichael.tarasande_protocol_hack.injection.accessor.IEventScreenInput;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.vialoadingbase.util.VersionListEnum;
import de.florianmichael.tarasande_protocol_hack.injection.accessor.IEventScreenInput;
import net.tarasandedevelopment.tarasande.event.EventScreenInput;
import de.florianmichael.tarasande_protocol_hack.injection.accessor.IEventScreenInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.mandora.event.Event;
import su.mandora.event.EventDispatcher;

@Mixin(value = EventDispatcher.class, remap = false)
public class MixinEventDispatcher {

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = "call", at = @At("HEAD"), cancellable = true)
    public void cancelOriginalScreenInputEvent(Event event, CallbackInfo ci) {
        if (event instanceof EventScreenInput && ViaLoadingBase.getTargetVersion().isOlderThanOrEqualTo(VersionListEnum.r1_12_2)) {
            if (((IEventScreenInput) event).getOriginal()) {
                ci.cancel();
            }
        }
    }
}