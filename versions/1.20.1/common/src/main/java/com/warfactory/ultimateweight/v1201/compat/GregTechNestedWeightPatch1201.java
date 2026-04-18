package com.warfactory.ultimateweight.v1201.compat;

import com.hbm.weight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.core.WeightResolutionContext;
import com.warfactory.ultimateweight.v1201.WeightViews1201;
import java.util.OptionalDouble;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("unused")
public final class GregTechNestedWeightPatch1201 implements IWeightCompatProvider {
    private static final String MOD_ID = "gtceu";
    private static final String INVENTORY = "inventory";
    private static final String ITEMS = "Items";
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

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null || !itemId.toString().startsWith(MOD_ID + ":")) {
            return OptionalDouble.empty();
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(INVENTORY, 10)) {
            return OptionalDouble.empty();
        }

        CompoundTag inventory = tag.getCompound(INVENTORY);
        if (!inventory.contains(ITEMS, 9)) {
            return OptionalDouble.empty();
        }

        double total = 0.0D;
        ListTag items = inventory.getList(ITEMS, 10);
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
        return 300;
    }
}
