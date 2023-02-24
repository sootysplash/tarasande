package de.florianmichael.tarasande_protocol_spoofer.injection.mixin.haproxyhack;

import de.florianmichael.tarasande_protocol_spoofer.tarasandevalues.HAProxyProtocol;
import io.netty.channel.Channel;
import io.netty.handler.codec.haproxy.HAProxyMessageEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.network.ClientConnection$1")
public class MixinClientConnection_1 {

    @Inject(method = "initChannel", at = @At("TAIL"))
    public void addChannelHandlers(Channel channel, CallbackInfo ci) {
        if (HAProxyProtocol.INSTANCE.getEnabled().getValue()) {
            channel.pipeline().addFirst("haproxy-encoder", HAProxyMessageEncoder.INSTANCE);
            channel.pipeline().addLast(HAProxyProtocol.INSTANCE.createHandler());
        }
    }
}
