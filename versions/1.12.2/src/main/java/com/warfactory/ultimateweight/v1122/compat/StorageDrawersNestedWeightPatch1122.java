package com.warfactory.ultimateweight.v1122.compat;

import com.warfactory.ultimateweight.v1122.WeightViews1122;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings("unused")
public final class StorageDrawersNestedWeightPatch1122 implements WeightViews1122.NestedWeightProvider {
    private static final String MOD_ID = "storagedrawers";
    private static final String TILE = "tile";
    private static final String DRAWERS = "Drawers";
    private static final String ITEM = "Item";
    private static final String COUNT = "Count";
    private static final double EPSILON = 0.000001D;

    @Override
    public double additionalWeight(ItemStack stack, int depth) {
        if (stack.isEmpty()) {
            return 0.0D;
        }

        ResourceLocation itemId = stack.getItem().getRegistryName();
        if (itemId == null || !itemId.toString().startsWith(MOD_ID + ":")) {
            return 0.0D;
        }

        NBTTagCompound stackTag = stack.getTagCompound();
        if (stackTag == null || !stackTag.hasKey(TILE, 10)) {
            return 0.0D;
        }

        NBTTagCompound tile = stackTag.getCompoundTag(TILE);
        if (!tile.hasKey(DRAWERS, 9)) {
            return 0.0D;
        }

        double total = 0.0D;
        NBTTagList drawers = tile.getTagList(DRAWERS, 10);
        for (int index = 0; index < drawers.tagCount(); index++) {
            NBTTagCompound drawer = drawers.getCompoundTagAt(index);
            if (!drawer.hasKey(ITEM, 10)) {
                continue;
            }

            int storedCount = drawer.hasKey(COUNT, 99) ? drawer.getInteger(COUNT) : 0;
            if (storedCount <= 0) {
                continue;
            }

            ItemStack nested = new ItemStack(drawer.getCompoundTag(ITEM));
            if (nested.isEmpty()) {
                continue;
            }

            int nestedCount = Math.max(1, nested.getCount());
            double perStackWeight = WeightViews1122.stackWeight(nested, depth + 1);
            double perItemWeight = perStackWeight / nestedCount;
            if (perItemWeight > EPSILON) {
                total += perItemWeight * storedCount;
            }
        }
        return total;
    }
}
