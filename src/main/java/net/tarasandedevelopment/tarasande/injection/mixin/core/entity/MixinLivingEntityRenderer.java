package net.tarasandedevelopment.tarasande.injection.mixin.core.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.tarasandedevelopment.tarasande.util.math.rotation.RotationUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> {

    protected MixinLivingEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    public void modifyYaw(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (livingEntity == MinecraftClient.getInstance().player && RotationUtil.INSTANCE.getFakeRotation() != null)
            livingEntity.bodyYaw = livingEntity.prevBodyYaw = RotationUtil.INSTANCE.getFakeRotation().getYaw();
    }

    @Redirect(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;setAngles(Lnet/minecraft/entity/Entity;FFFFF)V"))
    public <E extends Entity> void modifyPitch(EntityModel<E> entityModel, E entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        boolean showFakeAngles = entity == MinecraftClient.getInstance().player && RotationUtil.INSTANCE.getFakeRotation() != null;
        float newHeadYaw = showFakeAngles ? 0 /* this is a delta */ : headYaw;
        float newHeadPitch = showFakeAngles ? RotationUtil.INSTANCE.getFakeRotation().getPitch() : headPitch;
        entityModel.setAngles(entity, limbAngle, limbDistance, animationProgress, newHeadYaw, newHeadPitch);
    }
}