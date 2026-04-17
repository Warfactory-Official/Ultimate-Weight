package com.warfactory.ultimateweight.v1122.compat;

import com.warfactory.ultimateweight.v1122.WeightViews1122;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public final class GenericNestedWeightPatch1122 implements WeightViews1122.NestedWeightProvider {
    private static final double EPSILON = 0.000001D;

    @Override
    public double additionalWeight(ItemStack stack, int depth) {
        if (stack.isEmpty()) {
            return 0.0D;
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
                return total;
            }
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey("BlockEntityTag", 10)) {
            return 0.0D;
        }

        NBTTagCompound blockEntityTag = tag.getCompoundTag("BlockEntityTag");
        if (!blockEntityTag.hasKey("Items", 9)) {
            return 0.0D;
        }

        double total = 0.0D;
        NBTTagList items = blockEntityTag.getTagList("Items", 10);
        for (int index = 0; index < items.tagCount(); index++) {
            ItemStack nested = new ItemStack(items.getCompoundTagAt(index));
            if (!nested.isEmpty()) {
                total += WeightViews1122.stackWeight(nested, depth + 1);
            }
        }
        return total;
    }
}
