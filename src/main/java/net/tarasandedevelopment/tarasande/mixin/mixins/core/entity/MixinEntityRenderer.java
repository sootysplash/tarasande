package net.tarasandedevelopment.tarasande.mixin.mixins.core.entity;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.tarasandedevelopment.tarasande.TarasandeMain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getDisplayName()Lnet/minecraft/text/Text;"))
    public Text overwriteNameTag(Entity instance) {
        Text text = TarasandeMain.Companion.get().getTagName().getTagName(instance);
        if (text != null)
            return text;
        else
            return instance.getDisplayName();
    }

}
