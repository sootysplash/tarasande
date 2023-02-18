/*
 * Copyright (c) FlorianMichael as EnZaXD 2022
 * Created on 6/24/22, 8:21 PM
 *
 * --FLORIAN MICHAEL PRIVATE LICENCE v1.0--
 *
 * This file / project is protected and is the intellectual property of Florian Michael (aka. EnZaXD),
 * any use (be it private or public, be it copying or using for own use, be it publishing or modifying) of this
 * file / project is prohibited. It requires in that use a written permission with official signature of the owner
 * "Florian Michael". "Florian Michael" receives the right to control and manage this file / project. This right is not
 * cancelled by copying or removing the license and in case of violation a criminal consequence is to be expected.
 * The owner "Florian Michael" is free to change this license.
 */
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

package de.florianmichael.clampclient.injection.mixin.protocolhack.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.florianmichael.tarasande_protocol_hack.tarasande.values.ProtocolHackValues;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.block.BlockState;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalDouble;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Shadow
    public abstract Item getItem();

    @Inject(method = "getMiningSpeedMultiplier", at = @At("RETURN"), cancellable = true)
    private void modifyMiningSpeedMultiplier(BlockState state, CallbackInfoReturnable<Float> ci) {
        final Item toolItem = ((ItemStack) (Object) this).getItem();

        if (ViaLoadingBase.getTargetVersion().isOlderThanOrEqualTo(ProtocolVersion.v1_15_2) && toolItem instanceof HoeItem) {
            ci.setReturnValue(1F);
        }
    }

    @Redirect(method = "getTooltip",
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/entity/attribute/EntityAttributes;GENERIC_ATTACK_DAMAGE:Lnet/minecraft/entity/attribute/EntityAttribute;", ordinal = 0)),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getAttributeBaseValue(Lnet/minecraft/entity/attribute/EntityAttribute;)D", ordinal = 0))
    private double redirectGetTooltip(PlayerEntity player, EntityAttribute attribute) {
        if (ViaLoadingBase.getTargetVersion().isOlderThanOrEqualTo(ProtocolVersion.v1_8)) {
            return 0;
        } else {
            return player.getAttributeBaseValue(attribute);
        }
    }

    @SuppressWarnings({"InvalidInjectorMethodSignature", "MixinAnnotationTarget"})
    @ModifyVariable(method = "getAttributeModifiers", ordinal = 0, at = @At(value = "STORE", ordinal = 1))
    private Multimap<EntityAttribute, EntityAttributeModifier> modifyVariableGetAttributeModifiers(Multimap<EntityAttribute, EntityAttributeModifier> modifiers) {
        if (!ProtocolHackValues.INSTANCE.getReplaceAttributeModifiers().getValue()) {
            return modifiers;
        }
        if (modifiers.isEmpty()) {
            return modifiers;
        }
        modifiers = HashMultimap.create(modifiers);
        modifiers.removeAll(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        OptionalDouble defaultAttackDamage = protocolhack_getDefaultAttackDamage(getItem());
        if (defaultAttackDamage.isPresent()) {
            modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(Item.ATTACK_DAMAGE_MODIFIER_ID, "Weapon Modifier", defaultAttackDamage.getAsDouble(), EntityAttributeModifier.Operation.ADDITION));
        }
        modifiers.removeAll(EntityAttributes.GENERIC_ATTACK_SPEED);
        modifiers.removeAll(EntityAttributes.GENERIC_ARMOR);
        modifiers.removeAll(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
        return modifiers;
    }

    @Unique
    private OptionalDouble protocolhack_getDefaultAttackDamage(Item item) {
        if (item instanceof ToolItem) {
            ToolMaterial material = ((ToolItem) item).getMaterial();
            int materialBonus;
            if (material == ToolMaterials.STONE) {
                materialBonus = 1;
            } else if (material == ToolMaterials.IRON) {
                materialBonus = 2;
            } else if (material == ToolMaterials.DIAMOND) {
                materialBonus = 3;
            } else {
                materialBonus = 0;
            }
            if (item instanceof SwordItem) {
                return OptionalDouble.of(4 + materialBonus);
            } else if (item instanceof PickaxeItem) {
                return OptionalDouble.of(2 + materialBonus);
            } else if (item instanceof ShovelItem) {
                return OptionalDouble.of(1 + materialBonus);
            } else if (item instanceof AxeItem) {
                return OptionalDouble.of(3 + materialBonus);
            }
        }

        return OptionalDouble.empty();
    }
}
