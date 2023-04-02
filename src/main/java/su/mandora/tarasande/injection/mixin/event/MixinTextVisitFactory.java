package su.mandora.tarasande.injection.mixin.event;

import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import su.mandora.tarasande.event.EventDispatcher;
import su.mandora.tarasande.event.impl.EventTextVisit;

@Mixin(TextVisitFactory.class)
public class MixinTextVisitFactory {

    @ModifyVariable(method = "visitFormatted(Ljava/lang/String;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z", at = @At("LOAD"), argsOnly = true, ordinal = 0)
    private static String hookEventTextVisit(String value) {
        EventTextVisit eventTextVisit = new EventTextVisit(value);
        EventDispatcher.INSTANCE.call(eventTextVisit);
        return eventTextVisit.getString();
    }


}