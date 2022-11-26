package net.tarasandedevelopment.tarasande.injection.mixin.core.forgefaker;

import net.minecraft.server.ServerMetadata;
import net.tarasandedevelopment.tarasande.injection.accessor.forgefaker.IServerMetadata;
import net.tarasandedevelopment.tarasande.system.feature.multiplayerfeaturesystem.impl.forgefaker.payload.IForgePayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerMetadata.class)
public class MixinServerMetadata implements IServerMetadata {

    @Unique
    private IForgePayload tarasande_forgePayload;

    @Override
    public IForgePayload tarasande_getForgePayload() {
        return this.tarasande_forgePayload;
    }

    @Override
    public void tarasande_setForgePayload(IForgePayload forgePayload) {
        this.tarasande_forgePayload = forgePayload;
    }
}