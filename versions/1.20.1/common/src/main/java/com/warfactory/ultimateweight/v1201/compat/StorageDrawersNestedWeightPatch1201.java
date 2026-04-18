package com.warfactory.ultimateweight.v1201.compat;

import com.hbm.weight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.core.WeightResolutionContext;
import com.warfactory.ultimateweight.v1201.WeightViews1201;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalDouble;

public class StorageDrawersNestedWeightPatch1201 implements IWeightCompatProvider {
        private static final String MOD_ID = "storagedrawers";
        private static final String TILE = "tile";
        private static final String DRAWERS = "Drawers";
        private static final String ITEM = "Item";
        private static final String COUNT = "Count";
        private static final double EPSILON = 0.000001D;

        @Override
        public OptionalDouble getUnitWeight(Object rawStack) {
                if (!(rawStack instanceof ItemStack stack)) {
                        return OptionalDouble.empty();
                }

                if (stack.isEmpty()) {
                        return OptionalDouble.empty();
                }

                int depth = WeightResolutionContext.currentDepth();
                if (depth >= WeightViews1201.maxNestedDepth()) {
                        return OptionalDouble.empty();
                }

                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (!itemId.toString().startsWith(MOD_ID + ":")) {
                        return OptionalDouble.empty();
                }

                CompoundTag stackTag = stack.getTag();
                if (stackTag == null || !stackTag.contains(TILE, 10)) {
                        return OptionalDouble.empty();
                }

                CompoundTag tile = stackTag.getCompound(TILE);
                if (!tile.contains(DRAWERS, 9)) {
                        return OptionalDouble.empty();
                }

                double total = 0.0D;
                ListTag drawers = tile.getList(DRAWERS, 10);

                for (int index = 0; index < drawers.size(); index++) {
                        CompoundTag drawer = drawers.getCompound(index);
                        if (!drawer.contains(ITEM, 10)) {
                                continue;
                        }

                        int storedCount = drawer.contains(COUNT, 99) ? drawer.getInt(COUNT) : 0;
                        if (storedCount <= 0) {
                                continue;
                        }

                        ItemStack nested = ItemStack.of(drawer.getCompound(ITEM));
                        if (nested.isEmpty()) {
                                continue;
                        }

                        int nestedCount = Math.max(1, nested.getCount());
                        double perStackWeight = WeightViews1201.stackWeight(nested, depth + 1);
                        double perItemWeight = perStackWeight / nestedCount;
                        if (perItemWeight > EPSILON) {
                                total += perItemWeight * storedCount;
                        }
                }

                return total > EPSILON
                        ? OptionalDouble.of(WeightViews1201.configuredWeightOf(stack, depth) + total)
                        : OptionalDouble.empty();

        }
        @Override
        public int getPriority() {
            return 250;
        }
}
