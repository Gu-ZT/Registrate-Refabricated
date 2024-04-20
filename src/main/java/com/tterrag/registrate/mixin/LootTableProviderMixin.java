package com.tterrag.registrate.mixin;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.tterrag.registrate.fabric.CustomValidationLootProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;

@Mixin(LootTableProvider.class)
public class LootTableProviderMixin {
    @ModifyExpressionValue(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;iterator()Ljava/util/Iterator;" // Sets.difference
            )
    )
    private Iterator<ResourceLocation> runCustomValidation(Iterator<ResourceLocation> original,
                                                        @Local(ordinal = 0) Map<ResourceLocation, LootTable> tables,
                                                        @Local(ordinal = 0)ValidationContext context) {
        if (this instanceof CustomValidationLootProvider custom) {
            custom.validate(tables, context);
            return Collections.emptyIterator();
        }
        return original;
    }

    @WrapWithCondition(
            method = "run",
                at = @At(
                        value = "INVOKE",
                        target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"
                )
    )
    private boolean preventOtherValidation(Map<ResourceLocation, LootTable> tables, BiConsumer<ResourceLocation, LootTable> consumer) {
        return !(this instanceof CustomValidationLootProvider);
    }
}
