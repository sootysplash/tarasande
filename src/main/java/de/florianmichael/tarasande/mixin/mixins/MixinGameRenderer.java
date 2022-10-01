package de.florianmichael.tarasande.mixin.mixins;

import de.florianmichael.tarasande.event.EventEntityRaycast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import su.mandora.tarasande.TarasandeMain;

import java.util.function.Predicate;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Redirect(method = "updateTargetedEntity", at =
    @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"))
    public @Nullable EntityHitResult hookedRaycast(Entity entity, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate, double d) {
        EventEntityRaycast eventEntityRaycast = new EventEntityRaycast(MinecraftClient.getInstance().crosshairTarget);
        TarasandeMain.Companion.get().getManagerEvent().call(eventEntityRaycast);
        if (eventEntityRaycast.getCancelled()) {
            return null;
        }
        return ProjectileUtil.raycast(entity, min, max, box, predicate, d);
    }
}
