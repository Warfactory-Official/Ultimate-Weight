package com.warfactory.ultimateweight.v1201.compat;

import com.warfactory.ultimateweight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.core.WeightResolutionContext;
import com.warfactory.ultimateweight.v1201.WeightViews1201;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalDouble;

public final class GenericNestedWeightPatch1201 implements IWeightCompatProvider {
    private static final double EPSILON = 0.000001D;

    @Override
    public OptionalDouble getUnitWeight(Object rawStack) {
        if (!(rawStack instanceof ItemStack stack) || stack.isEmpty()) {
            return OptionalDouble.empty();
        }

        int depth = WeightResolutionContext.currentDepth();
        if (depth >= WeightViews1201.maxNestedDepth()) {
            return OptionalDouble.empty();
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("BlockEntityTag", 10)) {
            return OptionalDouble.empty();
        }

        CompoundTag blockEntityTag = tag.getCompound("BlockEntityTag");
        if (!blockEntityTag.contains("Items", 9)) {
            return OptionalDouble.empty();
        }

        double total = 0.0D;
        ListTag items = blockEntityTag.getList("Items", 10);
        for (int index = 0; index < items.size(); index++) {
            ItemStack nested = ItemStack.of(items.getCompound(index));
            if (!nested.isEmpty()) {
                total += WeightViews1201.stackWeight(nested, depth + 1);
            }
        }
        return total > EPSILON
            ? OptionalDouble.of(WeightViews1201.configuredWeightOf(stack, depth) + total)
            : OptionalDouble.empty();
    }

    @Override
    public int getPriority() {
        return 50;
    }
}
