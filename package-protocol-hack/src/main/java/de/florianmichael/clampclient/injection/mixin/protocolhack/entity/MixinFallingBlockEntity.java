package de.florianmichael.clampclient.injection.mixin.protocolhack.entity;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.util.VersionList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FallingBlockEntity.class)
public abstract class MixinFallingBlockEntity extends Entity {

    public MixinFallingBlockEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(method = { "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/block/BlockState;)V", "onSpawnPacket" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/FallingBlockEntity;setPosition(DDD)V"))
    public void revertMissingOffset(FallingBlockEntity instance, double x, double y, double z) {
        if (VersionList.isOlderOrEqualTo(ProtocolVersion.v1_18_2)) {
            instance.setPosition(x, y + (double)((1.0F - this.getHeight()) / 2.0F), z);
        }
        instance.setPosition(x, y, z);
    }
}
