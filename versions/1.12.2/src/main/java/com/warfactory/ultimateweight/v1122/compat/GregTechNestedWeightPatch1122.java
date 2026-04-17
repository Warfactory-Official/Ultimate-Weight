package com.warfactory.ultimateweight.v1122.compat;

import com.warfactory.ultimateweight.v1122.WeightViews1122;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings("unused")
public final class GregTechNestedWeightPatch1122 implements WeightViews1122.NestedWeightProvider {
    private static final String MOD_ID = "gregtech";
    private static final String INVENTORY = "Inventory";
    private static final String ITEMS = "Items";

    @Override
    public double additionalWeight(ItemStack stack, int depth) {
        if (stack.isEmpty()) {
            return 0.0D;
        }

        ResourceLocation itemId = stack.getItem().getRegistryName();
        if (itemId == null || !itemId.toString().startsWith(MOD_ID + ":")) {
            return 0.0D;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(INVENTORY, 10)) {
            return 0.0D;
        }

        NBTTagCompound inventory = tag.getCompoundTag(INVENTORY);
        if (!inventory.hasKey(ITEMS, 9)) {
            return 0.0D;
        }

        double total = 0.0D;
        NBTTagList items = inventory.getTagList(ITEMS, 10);
        for (int index = 0; index < items.tagCount(); index++) {
            ItemStack nested = new ItemStack(items.getCompoundTagAt(index));
            if (!nested.isEmpty()) {
                total += WeightViews1122.stackWeight(nested, depth + 1);
            }
        }
        return total;
    }
}
