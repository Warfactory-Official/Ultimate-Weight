package com.warfactory.ultimateweight.v1122.compat;

import com.warfactory.ultimateweight.api.CompatPlugin;
import com.warfactory.ultimateweight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.core.WeightResolutionContext;
import com.warfactory.ultimateweight.v1122.RetroSophisticatedBackpackSupport1122;
import com.warfactory.ultimateweight.v1122.WeightViews1122;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.OptionalDouble;

/**
 * Weight provider for Retro Sophisticated Backpacks.
 *
 * <p>A backpack stores its contents in a forge capability ({@code BackpackWrapper}), not in the
 * item's regular NBT tag, so they are NOT present in {@code getTagCompound()} at runtime - only in
 * the serialized {@code ForgeCaps}. Instead of forcing a serialization, the contents are read live
 * through the standard {@link CapabilityItemHandler#ITEM_HANDLER_CAPABILITY}, which the wrapper
 * exposes. This yields the correct (extended) stack counts directly from the live stacks. The
 * installed upgrade items live in a separate handler that is not exposed through that capability,
 * so they are reached via {@link RetroSophisticatedBackpackSupport1122#upgradeHandler(IItemHandler)}
 * and weighed too.
 *
 * <p>These stacks bypass the resolver's complex cache (see
 * {@code WeightViews1122.StackView#complexCacheKey()}) because the regular tag hash does not change
 * when capability contents change, so the live read above must run on every resolve.
 */
@SuppressWarnings("unused")
@CompatPlugin(requiredMods = "retro_sophisticated_backpacks")
public class RetroSophisticatedBackpackPatch1122 implements IWeightCompatProvider {
    private static final double EPSILON = 0.000001D;

    @Override
    public OptionalDouble getUnitWeight(Object obj) {
        if (!(obj instanceof ItemStack)) {
            return OptionalDouble.empty();
        }

        ItemStack stack = (ItemStack) obj;
        if (!RetroSophisticatedBackpackSupport1122.isBackpackStack(stack)) {
            return OptionalDouble.empty();
        }

        int depth = WeightResolutionContext.currentDepth();
        if (depth >= WeightViews1122.maxNestedDepth()) {
            return OptionalDouble.empty();
        }

        IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (handler == null) {
            return OptionalDouble.empty();
        }

        // Stored items (main inventory) plus the installed upgrade items.
        double total = sumHandler(handler, stack, depth);
        total += sumHandler(RetroSophisticatedBackpackSupport1122.upgradeHandler(handler), stack, depth);

        return total > EPSILON
            ? OptionalDouble.of(WeightViews1122.configuredWeightOf(stack, depth) + total)
            : OptionalDouble.empty();
    }

    private static double sumHandler(IItemHandler handler, ItemStack self, int depth) {
        if (handler == null) {
            return 0.0D;
        }
        double total = 0.0D;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack nested = handler.getStackInSlot(slot);
            if (!nested.isEmpty() && nested != self) {
                total += WeightViews1122.stackWeight(nested, depth + 1);
            }
        }
        return total;
    }

    @Override
    public int getPriority() {
        return 200;
    }
}
