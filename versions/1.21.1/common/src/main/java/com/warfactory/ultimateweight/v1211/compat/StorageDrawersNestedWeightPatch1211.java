package com.warfactory.ultimateweight.v1211.compat;

import com.warfactory.ultimateweight.api.CompatPlugin;
import com.warfactory.ultimateweight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.core.WeightResolutionContext;
import com.warfactory.ultimateweight.v1211.WeightViews1211;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalDouble;

/**
 * Storage Drawers nested weight on 1.21.1. A drawer item keeps each slot's stored item plus a flat
 * count under a {@code tile/Drawers} structure. On 1.21.1 that root NBT lives in the
 * {@code minecraft:custom_data} component (block-entity contents fall back to
 * {@code minecraft:block_entity_data}); both are surfaced through {@link ItemNbtBridge1211}, and the
 * per-drawer {@code Item} compound is registry-deserialized the same way.
 */
@CompatPlugin(requiredMods = "storagedrawers")
@SuppressWarnings("unused")
public final class StorageDrawersNestedWeightPatch1211 implements IWeightCompatProvider {
    private static final String MOD_ID = "storagedrawers";
    private static final String TILE = "tile";
    private static final String DRAWERS = "Drawers";
    private static final String ITEM = "Item";
    private static final String COUNT = "Count";
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

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null || !itemId.toString().startsWith(MOD_ID + ":")) {
            return OptionalDouble.empty();
        }

        CompoundTag tile = tileTag(stack);
        if (tile == null || !tile.contains(DRAWERS, 9)) {
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

            ItemStack nested = ItemNbtBridge1211.loadStack(drawer.getCompound(ITEM));
            if (nested.isEmpty()) {
                continue;
            }

            int nestedCount = Math.max(1, nested.getCount());
            double perStackWeight = WeightViews1211.stackWeight(nested, depth + 1);
            double perItemWeight = perStackWeight / nestedCount;
            if (perItemWeight > EPSILON) {
                total += perItemWeight * storedCount;
            }
        }

        return total > EPSILON
            ? OptionalDouble.of(WeightViews1211.configuredWeightOf(stack, depth) + total)
            : OptionalDouble.empty();
    }

    private static CompoundTag tileTag(ItemStack stack) {
        CompoundTag custom = ItemNbtBridge1211.customData(stack);
        if (custom.contains(TILE, 10)) {
            return custom.getCompound(TILE);
        }
        CompoundTag blockEntity = ItemNbtBridge1211.blockEntityData(stack);
        if (blockEntity.contains(TILE, 10)) {
            return blockEntity.getCompound(TILE);
        }
        // Some block items store the drawer slots directly under block_entity_data.
        return blockEntity.contains(DRAWERS, 9) ? blockEntity : null;
    }

    @Override
    public int getPriority() {
        return 250;
    }
}
