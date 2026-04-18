package com.warfactory.ultimateweight.v1122.compat;

import com.warfactory.ultimateweight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.core.WeightResolutionContext;
import com.warfactory.ultimateweight.v1122.WeightViews1122;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import java.util.OptionalDouble;

@SuppressWarnings("unused")
public final class GregTechNestedWeightPatch1122 implements IWeightCompatProvider {
    private static final String MOD_ID = "gregtech";
    private static final String INVENTORY = "Inventory";
    private static final String ITEMS = "Items";

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

        ResourceLocation itemId = stack.getItem().getRegistryName();
        if (itemId == null || !itemId.toString().startsWith(MOD_ID + ":")) {
            return OptionalDouble.empty();
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(INVENTORY, 10)) {
            return OptionalDouble.empty();
        }

        NBTTagCompound inventory = tag.getCompoundTag(INVENTORY);
        if (!inventory.hasKey(ITEMS, 9)) {
            return OptionalDouble.empty();
        }

        double total = 0.0D;
        NBTTagList items = inventory.getTagList(ITEMS, 10);
        for (int index = 0; index < items.tagCount(); index++) {
            ItemStack nested = new ItemStack(items.getCompoundTagAt(index));
            if (!nested.isEmpty()) {
                total += WeightViews1122.stackWeight(nested, depth + 1);
            }
        }
        return total > 0.000001D
            ? OptionalDouble.of(WeightViews1122.configuredWeightOf(stack, depth) + total)
            : OptionalDouble.empty();
    }

    @Override
    public int getPriority() {
        return 300;
    }
}
