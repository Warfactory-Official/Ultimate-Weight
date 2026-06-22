package com.warfactory.ultimateweight.v1211.compat;

import com.warfactory.ultimateweight.api.CompatPlugin;
import com.warfactory.ultimateweight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.core.WeightResolutionContext;
import com.warfactory.ultimateweight.v1211.WeightViews1211;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.OptionalDouble;

/**
 * Generic nested-container weight on 1.21.1. Reads two storage shapes, neither mod-specific:
 * <ul>
 *   <li>the modern {@code minecraft:container} component (vanilla shulker boxes and anything else
 *       using {@code ItemContainerContents}) - live {@link ItemStack}s, no NBT round-trip;</li>
 *   <li>the legacy {@code BlockEntityTag.Items} NBT list, for mods that still serialize that way
 *       (deserialized through {@link ItemNbtBridge1211}, which needs registry access installed).</li>
 * </ul>
 */
@CompatPlugin
@SuppressWarnings("unused")
public final class GenericNestedWeightPatch1211 implements IWeightCompatProvider {
    private static final double EPSILON = 0.000001D;

    @Override
    public OptionalDouble getUnitWeight(Object rawStack) {
        if (!(rawStack instanceof ItemStack stack) || stack.isEmpty()) {
            return OptionalDouble.empty();
        }

        int depth = WeightResolutionContext.currentDepth();
        if (depth >= WeightViews1211.maxNestedDepth()) {
            return OptionalDouble.empty();
        }

        double total = 0.0D;

        List<ItemStack> contents = ItemNbtBridge1211.containerContents(stack);
        for (ItemStack nested : contents) {
            if (!nested.isEmpty() && nested != stack) {
                total += WeightViews1211.stackWeight(nested, depth + 1);
            }
        }

        CompoundTag blockEntityTag = ItemNbtBridge1211.blockEntityData(stack);
        if (blockEntityTag.contains("Items", 9)) {
            ListTag items = blockEntityTag.getList("Items", 10);
            for (int index = 0; index < items.size(); index++) {
                ItemStack nested = ItemNbtBridge1211.loadStack(items.getCompound(index));
                if (!nested.isEmpty()) {
                    total += WeightViews1211.stackWeight(nested, depth + 1);
                }
            }
        }

        return total > EPSILON
            ? OptionalDouble.of(WeightViews1211.configuredWeightOf(stack, depth) + total)
            : OptionalDouble.empty();
    }

    @Override
    public int getPriority() {
        return 50;
    }
}
