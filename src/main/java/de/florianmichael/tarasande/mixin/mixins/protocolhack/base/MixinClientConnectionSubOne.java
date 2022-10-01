package de.florianmichael.tarasande.mixin.mixins.protocolhack.base;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import de.florianmichael.tarasande.mixin.accessor.protocolhack.IClientConnection_Protocol;
import de.florianmichael.tarasande.util.network.EventDecoder;
import de.florianmichael.tarasande.util.network.EventEncoder;
import de.florianmichael.viaprotocolhack.netty.CustomViaDecodeHandler;
import de.florianmichael.viaprotocolhack.netty.CustomViaEncodeHandler;
import de.florianmichael.viaprotocolhack.netty.NettyConstants;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/network/ClientConnection$1")
public class MixinClientConnectionSubOne {

    // synthetic field
    @Final
    @Shadow
    ClientConnection field_11663;

    @Inject(method = "initChannel", at = @At("TAIL"))
    public void injectPostInitChannel(Channel channel, CallbackInfo ci) {
        if (channel instanceof SocketChannel) {
            UserConnection user = new UserConnectionImpl(channel, true);
            ((IClientConnection_Protocol) field_11663).florianMichael_setViaConnection(user);
            new ProtocolPipelineImpl(user);

            channel.pipeline()
                    .addBefore("encoder", NettyConstants.HANDLER_ENCODER_NAME, new CustomViaEncodeHandler(user))
                    .addBefore("decoder", NettyConstants.HANDLER_DECODER_NAME, new CustomViaDecodeHandler(user));

            channel.pipeline()
                    .addBefore(NettyConstants.HANDLER_ENCODER_NAME, "tarasande-encoder", new EventEncoder())
                    .addBefore(NettyConstants.HANDLER_DECODER_NAME, "tarasande-decoder", new EventDecoder());
        }
    }
}
