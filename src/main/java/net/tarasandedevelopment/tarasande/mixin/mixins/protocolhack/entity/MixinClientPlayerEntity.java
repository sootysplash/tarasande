package net.tarasandedevelopment.tarasande.mixin.mixins.protocolhack.entity;

import com.mojang.authlib.GameProfile;
import de.florianmichael.viaprotocolhack.util.VersionList;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.tarasandedevelopment.tarasande.TarasandeMain;
import net.tarasandedevelopment.tarasande.mixin.accessor.protocolhack.IClientPlayerEntity_Protocol;
import net.tarasandedevelopment.tarasande.module.movement.ModuleNoSlowdown;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientPlayerEntity.class, priority = 2000)
public class MixinClientPlayerEntity extends AbstractClientPlayerEntity implements IClientPlayerEntity_Protocol {

    @Shadow private boolean lastOnGround;

    @Shadow public Input input;

    @Unique
    private boolean areSwingCanceledThisTick = false;

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile, @Nullable PlayerPublicKey publicKey) {
        super(world, profile, publicKey);
    }

    @Redirect(method = "tickMovement()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tickMovement()V"))
    public void doNothing(AbstractClientPlayerEntity instance) {
        if ((instance.isUsingItem() || instance.isSneaking() || instance.isSubmergedInWater()) && VersionList.isOlderOrEqualTo(VersionList.R1_8)) {
            ModuleNoSlowdown moduleNoSlowdown = TarasandeMain.Companion.get().getManagerModule().get(ModuleNoSlowdown.class);
            if (!moduleNoSlowdown.getEnabled() || !moduleNoSlowdown.getPreventUnsprint().getValue())
                instance.setSprinting(false);
        }

        super.tickMovement();
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;lastOnGround:Z", opcode = Opcodes.GETFIELD))
    public boolean redirectSendMovementPackets(ClientPlayerEntity player) {
        if (VersionList.isOlderOrEqualTo(VersionList.R1_8))
            return !this.lastOnGround; // make sure player packets are sent every tick to tick the server-side player entity
        else
            return this.lastOnGround;
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;square(D)D"))
    public double redirectSendMovementPackets_2(double n) {
        if (VersionList.isOlderOrEqualTo(VersionList.R1_8))
            n = 9.0E-4D;

        return MathHelper.square(n);
    }

    @Inject(method = "swingHand", at = @At("HEAD"), cancellable = true)
    public void injectSwingHand(Hand hand, CallbackInfo ci) {
        if (VersionList.isOlderOrEqualTo(VersionList.R1_8) && areSwingCanceledThisTick)
            ci.cancel();
        areSwingCanceledThisTick = false;
    }

    @Inject(
            method = "tickMovement()V",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isCamera()Z")),
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/input/Input;sneaking:Z", ordinal = 0)
    )
    private void injectTickMovement(CallbackInfo ci) {
        if (VersionList.isOlderOrEqualTo(VersionList.R1_14_4))
            if (this.input.sneaking) {
                this.input.movementSideways = (float)((double)this.input.movementSideways / 0.3D);
                this.input.movementForward = (float)((double)this.input.movementForward / 0.3D);
            }
    }

    @Redirect(method = "tickMovement",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isWalking()Z")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSwimming()Z", ordinal = 0))
    public boolean redirectIsSneakingWhileSwimming(ClientPlayerEntity _this) {
        if (VersionList.isOlderOrEqualTo(VersionList.R1_14_1))
            return false;
        else
            return _this.isSwimming();
    }

    @Inject(method = "isWalking", at = @At("HEAD"), cancellable = true)
    public void easierUnderwaterSprinting(CallbackInfoReturnable<Boolean> ci) {
        if (VersionList.isOlderOrEqualTo(VersionList.R1_14_1))
            ci.setReturnValue(input.movementForward >= 0.8);
    }

    @Redirect(method = "tickMovement()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;hasForwardMovement()Z", ordinal = 0))
    private boolean disableSprintSneak(Input input) {
        if (VersionList.isOlderOrEqualTo(VersionList.R1_14_1))
            return input.movementForward >= 0.8F;
        return input.hasForwardMovement();
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isTouchingWater()Z"))
    private boolean redirectTickMovement(ClientPlayerEntity self) {
        if (VersionList.isOlderOrEqualTo(VersionList.R1_12_2))
            return false; // disable all water related movement
        return self.isTouchingWater();
    }

    @Override
    public void tarasande_cancelSwingOnce() {
        areSwingCanceledThisTick = true;
    }
}
