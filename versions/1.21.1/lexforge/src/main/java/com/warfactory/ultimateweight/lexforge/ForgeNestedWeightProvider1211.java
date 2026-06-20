package com.warfactory.ultimateweight.lexforge;

import com.warfactory.ultimateweight.api.CompatPlugin;
import com.warfactory.ultimateweight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.core.WeightResolutionContext;
import com.warfactory.ultimateweight.v1211.WeightViews1211;
import com.warfactory.ultimateweight.v1211.compat.ItemNbtBridge1211;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.util.OptionalDouble;

/**
 * LexForge nested-container weight. Reads, in order: the Forge item-handler capability (live stacks -
 * covers backpacks and most mod containers), the modern {@code minecraft:container} component
 * (vanilla shulker boxes), then the legacy {@code BlockEntityTag.Items} NBT list. The latter two are
 * surfaced through {@link ItemNbtBridge1211}; only the legacy NBT path needs registry access.
 */
@CompatPlugin
@SuppressWarnings("unused")
public final class ForgeNestedWeightProvider1211 implements IWeightCompatProvider {
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

        double total = 0.0D;

        IItemHandler handler = stack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().orElse(null);
        if (handler != null) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack nested = handler.getStackInSlot(slot);
                if (!nested.isEmpty() && nested != stack) {
                    total += WeightViews1211.stackWeight(nested, depth + 1);
                }
            }
            if (total > EPSILON) {
                return OptionalDouble.of(WeightViews1211.configuredWeightOf(stack, depth) + total);
            }
        }

        for (ItemStack nested : ItemNbtBridge1211.containerContents(stack)) {
            if (!nested.isEmpty() && nested != stack) {
                total += WeightViews1211.stackWeight(nested, depth + 1);
            }
        }

        CompoundTag blockEntityTag = ItemNbtBridge1211.blockEntityData(stack);
        if (blockEntityTag.contains("Items", 9)) {
            ListTag items = blockEntityTag.getList("Items", 10);
            for (int index = 0; index < items.size(); index++) {
                ItemStack nested = ItemNbtBridge1211.loadStack(items.getCompound(index));
                if (!nested.isEmpty()) {
                    total += WeightViews1211.stackWeight(nested, depth + 1);
                }
            }
        }

        return total > EPSILON
            ? OptionalDouble.of(WeightViews1211.configuredWeightOf(stack, depth) + total)
            : OptionalDouble.empty();
    }

    @Override
    public int getPriority() {
        return 100;
    }
}
