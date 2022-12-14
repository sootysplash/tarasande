/**
 * --FLORIAN MICHAEL PRIVATE LICENCE v1.2--
 *
 * This file / project is protected and is the intellectual property of Florian Michael (aka. EnZaXD),
 * any use (be it private or public, be it copying or using for own use, be it publishing or modifying) of this
 * file / project is prohibited. It requires in that use a written permission with official signature of the owner
 * "Florian Michael". "Florian Michael" receives the right to control and manage this file / project. This right is not
 * cancelled by copying or removing the license and in case of violation a criminal consequence is to be expected.
 * The owner "Florian Michael" is free to change this license. The creator assumes no responsibility for any infringements
 * that have arisen, are arising or will arise from this project / file. If this licence is used anywhere,
 * the latest version published by the author Florian Michael (aka EnZaXD) always applies automatically.
 *
 * Changelog:
 *     v1.0:
 *         Added License
 *     v1.1:
 *         Ownership withdrawn
 *     v1.2:
 *         Version-independent validity and automatic renewal
 */

package de.florianmichael.clampclient.injection.mixin.protocolhack.screen;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ProfileKey;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.clampclient.injection.mixininterface.IClientConnection_Protocol;
import de.florianmichael.clampclient.injection.mixininterface.IPublicKeyData_Protocol;
import de.florianmichael.viaprotocolhack.ViaProtocolHack;
import de.florianmichael.viaprotocolhack.util.VersionList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.encryption.PlayerKeyPair;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.tarasandedevelopment.tarasande_protocol_hack.TarasandeProtocolHack;
import net.tarasandedevelopment.tarasande_protocol_hack.fix.chatsession.v1_19_0.ChatSession1_19_0;
import net.tarasandedevelopment.tarasande_protocol_hack.fix.chatsession.v1_19_2.ChatSession1_19_2;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

@Mixin(targets = "net.minecraft.client.gui.screen.ConnectScreen$1")
public class MixinConnectScreen_1 {

    @Final
    @Shadow
    ServerAddress field_33737;

    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Ljava/net/InetSocketAddress;getHostName()Ljava/lang/String;"))
    public String replaceAddress(InetSocketAddress instance) {
        if (VersionList.isOlderOrEqualTo(ProtocolVersion.v1_17)) {
            return field_33737.getAddress();
        }

        return instance.getHostString();
    }

    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Ljava/net/InetSocketAddress;getPort()I"))
    public int replacePort(InetSocketAddress instance) {
        if (VersionList.isOlderOrEqualTo(ProtocolVersion.v1_17)) {
            return field_33737.getPort();
        }

        return instance.getPort();
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/Packet;)V", ordinal = 1, shift = At.Shift.BEFORE))
    public void setupChatSessions(CallbackInfo ci) {
        if (VersionList.isOlderTo(ProtocolVersion.v1_19)) {
            return; // This disables the chat session emulation for all versions <= 1.18.2
        }

        if (VersionList.isOlderOrEqualTo(ProtocolVersion.v1_19_1)) {
            try {
                final PlayerKeyPair playerKeyPair = MinecraftClient.getInstance().getProfileKeys().fetchKeyPair().get().orElse(null);
                if (playerKeyPair != null) {
                    final UserConnection userConnection = TarasandeProtocolHack.Companion.getViaConnection();

                    if (userConnection != null) {
                        final PlayerPublicKey.PublicKeyData publicKeyData = playerKeyPair.publicKey().data();

                        userConnection.put(new ChatSession1_19_2(
                                userConnection,
                                new ProfileKey(
                                        publicKeyData.expiresAt().toEpochMilli(),
                                        publicKeyData.key().getEncoded(),
                                        publicKeyData.keySignature()
                                ),
                                playerKeyPair.privateKey()
                        ));
                        if (VersionList.isEqualTo(ProtocolVersion.v1_19)) {
                            final byte[] legacyKey = ((IPublicKeyData_Protocol) (Object) publicKeyData).protocolhack_get1_19_0Key().array();
                            if (legacyKey != null) {
                                userConnection.put(new ChatSession1_19_0(
                                        userConnection,
                                        legacyKey
                                ));
                            } else {
                                ViaProtocolHack.instance().logger().log(Level.WARNING, "Mojang removed the legacy key");
                            }
                        }
                    } else {
                        ViaProtocolHack.instance().logger().log(Level.WARNING, "Via is not connected");
                    }
                } else {
                    ViaProtocolHack.instance().logger().log(Level.WARNING, "Failed to fetch the key pair");
                }
            } catch (InterruptedException | ExecutionException e) {
                ViaProtocolHack.instance().logger().log(Level.WARNING, "Failed to fetch the key pair");
            }
        }
    }
}
