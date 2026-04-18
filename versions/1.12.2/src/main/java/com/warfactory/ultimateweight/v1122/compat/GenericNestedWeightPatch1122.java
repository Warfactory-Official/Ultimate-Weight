package com.warfactory.ultimateweight.v1122.compat;

import com.warfactory.ultimateweight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.core.WeightResolutionContext;
import com.warfactory.ultimateweight.v1122.WeightViews1122;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.OptionalDouble;

public final class GenericNestedWeightPatch1122 implements IWeightCompatProvider {
    private static final double EPSILON = 0.000001D;

    @Override
    public OptionalDouble getUnitWeight(Object rawStack) {
        if (!(rawStack instanceof ItemStack)) {
            return OptionalDouble.empty();
        }

        ItemStack stack = (ItemStack) rawStack;
        if (stack.isEmpty()) {
            return OptionalDouble.empty();
        }

        int depth = WeightResolutionContext.currentDepth();
        if (depth >= WeightViews1122.maxNestedDepth()) {
            return OptionalDouble.empty();
        }

        IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (handler != null) {
            double total = 0.0D;
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack nested = handler.getStackInSlot(slot);
                if (!nested.isEmpty() && nested != stack) {
                    total += WeightViews1122.stackWeight(nested, depth + 1);
                }
            }
            if (total > EPSILON) {
                return OptionalDouble.of(WeightViews1122.configuredWeightOf(stack, depth) + total);
            }
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey("BlockEntityTag", 10)) {
            return OptionalDouble.empty();
        }

        NBTTagCompound blockEntityTag = tag.getCompoundTag("BlockEntityTag");
        if (!blockEntityTag.hasKey("Items", 9)) {
            return OptionalDouble.empty();
        }

        double total = 0.0D;
        NBTTagList items = blockEntityTag.getTagList("Items", 10);
        for (int index = 0; index < items.tagCount(); index++) {
            ItemStack nested = new ItemStack(items.getCompoundTagAt(index));
            if (!nested.isEmpty()) {
                total += WeightViews1122.stackWeight(nested, depth + 1);
            }
        }
        return total > EPSILON
            ? OptionalDouble.of(WeightViews1122.configuredWeightOf(stack, depth) + total)
            : OptionalDouble.empty();
    }

    @Override
    public int getPriority() {
        return 100;
    }
}
