package net.tarasandedevelopment.tarasande.injection.mixin.event.blockesp;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.tarasandedevelopment.tarasande.event.EventRenderBlockModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.mandora.event.EventDispatcher;

@Mixin(targets = "net.minecraft.client.render.chunk.RenderedChunk")
public class MixinRenderedChunk {

    @Inject(method = "getBlockState", at = @At("RETURN"), cancellable = true)
    public void hookEventRenderBlockModel(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = cir.getReturnValue();
        EventRenderBlockModel eventRenderBlockModel = new EventRenderBlockModel(state, pos);
        EventDispatcher.INSTANCE.call(eventRenderBlockModel);
        if (eventRenderBlockModel.getCancelled())
            cir.setReturnValue(Blocks.AIR.getDefaultState());
    }

}