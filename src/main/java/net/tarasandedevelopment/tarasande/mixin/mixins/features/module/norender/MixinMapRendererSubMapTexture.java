package net.tarasandedevelopment.tarasande.mixin.mixins.features.module.norender;

import net.minecraft.client.render.MapRenderer;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import net.tarasandedevelopment.tarasande.TarasandeMain;
import net.tarasandedevelopment.tarasande.features.module.render.ModuleNoRender;
import net.tarasandedevelopment.tarasande.util.EmptyIterator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MapRenderer.MapTexture.class)
public class MixinMapRendererSubMapTexture {

    @Redirect(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/map/MapState;getIcons()Ljava/lang/Iterable;"))
    public Iterable<MapIcon> noRender_draw(MapState instance) {
        if (TarasandeMain.Companion.get().getManagerModule().get(ModuleNoRender.class).getWorld().getMapMarkers().should()) {
            return EmptyIterator::new;
        }
        return instance.getIcons();
    }
}
