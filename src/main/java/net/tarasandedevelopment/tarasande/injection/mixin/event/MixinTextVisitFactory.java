package net.tarasandedevelopment.tarasande.injection.mixin.event;

import net.minecraft.client.font.TextVisitFactory;
import net.tarasandedevelopment.tarasande.event.EventTextVisit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import su.mandora.event.EventDispatcher;

@Mixin(TextVisitFactory.class)
public class MixinTextVisitFactory {

    @ModifyVariable(method = "visitFormatted(Ljava/lang/String;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z", at = @At("LOAD"), argsOnly = true, ordinal = 0)
    private static String hookEventTextVisit(String value) {
        EventTextVisit eventTextVisit = new EventTextVisit(value);
        EventDispatcher.INSTANCE.call(eventTextVisit);
        return eventTextVisit.getString();
    }


}
