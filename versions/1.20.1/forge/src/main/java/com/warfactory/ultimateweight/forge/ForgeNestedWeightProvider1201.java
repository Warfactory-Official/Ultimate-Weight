package com.warfactory.ultimateweight.forge;

import com.warfactory.ultimateweight.v1201.WeightViews1201;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.items.IItemHandler;

public final class ForgeNestedWeightProvider1201 implements WeightViews1201.NestedWeightProvider {
    private static final double EPSILON = 0.000001D;
    private static final Capability<IItemHandler> ITEM_HANDLER_CAPABILITY = CapabilityManager.get(
        new CapabilityToken<IItemHandler>() {
        }
    );

    @Override
    public double additionalWeight(ItemStack stack, int depth) {
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
                return total;
            }
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("BlockEntityTag", 10)) {
            return 0.0D;
        }

        CompoundTag blockEntityTag = tag.getCompound("BlockEntityTag");
        if (!blockEntityTag.contains("Items", 9)) {
            return 0.0D;
        }

        ListTag items = blockEntityTag.getList("Items", 10);
        for (int index = 0; index < items.size(); index++) {
            ItemStack nested = ItemStack.of(items.getCompound(index));
            if (!nested.isEmpty()) {
                total += WeightViews1201.stackWeight(nested, depth + 1);
            }
        }
        return total;
    }
}
