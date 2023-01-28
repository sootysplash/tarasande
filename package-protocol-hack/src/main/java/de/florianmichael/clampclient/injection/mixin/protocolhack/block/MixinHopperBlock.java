/**
 * --FLORIAN MICHAEL PRIVATE LICENCE v1.2--
 *
 * This file / project is protected and is the intellectual property of Florian Michael (aka. EnZaXD),
 * any use (be it private or public, be it copying or using for own use, be it publishing or modifying) of this
 * file / project is prohibited. It requires in that use a written permission with official signature of the owner
 * "Florian Michael". "Florian Michael" receives the right to control and manage this file / project. This right is not
 * cancelled by copying or removing the license and in case of violation a criminal consequence is to be expected.
 * The owner "Florian Michael" is free to change this license. The creator assumes no responsibility for any infringements
 * that have arisen, are arising or will arise from this project / file. If this licence is used anywhere,
 * the latest version published by the author Florian Michael (aka EnZaXD) always applies automatically.
 *
 * Changelog:
 *     v1.0:
 *         Added License
 *     v1.1:
 *         Ownership withdrawn
 *     v1.2:
 *         Version-independent validity and automatic renewal
 */

package de.florianmichael.clampclient.injection.mixin.protocolhack.block;

import de.florianmichael.vialoadingbase.ViaLoadingBase;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlock.class)
public class MixinHopperBlock {

    @Unique
    private final static VoxelShape protocolhack_INSIDE_SHAPE_1_12_2 = Block.createCuboidShape(2, 10, 2, 14, 16, 14);

    @Unique
    private final static VoxelShape protocolhack_HOPPER_SHAPE_1_12_2 = VoxelShapes.combineAndSimplify(
            VoxelShapes.fullCube(),
            protocolhack_INSIDE_SHAPE_1_12_2,
            BooleanBiFunction.ONLY_FIRST);

    @Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
    public void injectGetOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (ViaLoadingBase.getTargetVersion().isOlderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            cir.setReturnValue(protocolhack_HOPPER_SHAPE_1_12_2);
        }
    }

    @Inject(method = "getRaycastShape", at = @At("HEAD"), cancellable = true)
    public void injectGetRaycastShape(BlockState state, BlockView world, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
        if (ViaLoadingBase.getTargetVersion().isOlderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            cir.setReturnValue(protocolhack_INSIDE_SHAPE_1_12_2);
        }
    }
}
