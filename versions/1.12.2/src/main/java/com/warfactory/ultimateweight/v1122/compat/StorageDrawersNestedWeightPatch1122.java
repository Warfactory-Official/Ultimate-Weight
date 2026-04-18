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
public final class StorageDrawersNestedWeightPatch1122 implements IWeightCompatProvider {
    private static final String MOD_ID = "storagedrawers";
    private static final String TILE = "tile";
    private static final String DRAWERS = "Drawers";
    private static final String ITEM = "Item";
    private static final String COUNT = "Count";
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

        ResourceLocation itemId = stack.getItem().getRegistryName();
        if (itemId == null || !itemId.toString().startsWith(MOD_ID + ":")) {
            return OptionalDouble.empty();
        }

        NBTTagCompound stackTag = stack.getTagCompound();
        if (stackTag == null || !stackTag.hasKey(TILE, 10)) {
            return OptionalDouble.empty();
        }

        NBTTagCompound tile = stackTag.getCompoundTag(TILE);
        if (!tile.hasKey(DRAWERS, 9)) {
            return OptionalDouble.empty();
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
        return total > EPSILON
            ? OptionalDouble.of(WeightViews1122.configuredWeightOf(stack, depth) + total)
            : OptionalDouble.empty();
    }

    @Override
    public int getPriority() {
        return 250;
    }
}
