package com.warfactory.ultimateweight.forge;

import com.warfactory.ultimateweight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.core.WeightResolutionContext;
import com.warfactory.ultimateweight.v1201.WeightViews1201;
import java.util.OptionalDouble;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.items.IItemHandler;

public final class ForgeNestedWeightProvider1201 implements IWeightCompatProvider {
    private static final double EPSILON = 0.000001D;
    private static final Capability<IItemHandler> ITEM_HANDLER_CAPABILITY = CapabilityManager.get(
        new CapabilityToken<IItemHandler>() {
        }
    );

    @Override
    public OptionalDouble getUnitWeight(Object rawStack) {
        if (!(rawStack instanceof ItemStack)) {
            return OptionalDouble.empty();
        }

        ItemStack stack = (ItemStack) rawStack;
        int depth = WeightResolutionContext.currentDepth();
        if (depth >= WeightViews1201.maxNestedDepth()) {
            return OptionalDouble.empty();
        }

        double total = 0.0D;
        IItemHandler handler = stack.getCapability(ITEM_HANDLER_CAPABILITY).resolve().orElse(null);
        if (handler != null) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack nested = handler.getStackInSlot(slot);
                if (!nested.isEmpty() && nested != stack) {
                    total += WeightViews1201.stackWeight(nested, depth + 1);
                }
            }
            if (total > EPSILON) {
                return OptionalDouble.of(WeightViews1201.configuredWeightOf(stack, depth) + total);
            }
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("BlockEntityTag", 10)) {
            return OptionalDouble.empty();
        }

        CompoundTag blockEntityTag = tag.getCompound("BlockEntityTag");
        if (!blockEntityTag.contains("Items", 9)) {
            return OptionalDouble.empty();
        }

        ListTag items = blockEntityTag.getList("Items", 10);
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
        return 100;
    }
}
