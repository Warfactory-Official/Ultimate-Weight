package com.warfactory.ultimateweight.forge;

import appeng.api.stacks.AEItemKey;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.StorageCell;
import com.warfactory.ultimateweight.api.CompatPlugin;
import com.warfactory.ultimateweight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.core.WeightResolutionContext;
import com.warfactory.ultimateweight.v1201.WeightViews1201;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;
import java.util.Map;
import java.util.OptionalDouble;

/**
 * Adds the weight of the items stored inside an Applied Energistics 2 storage cell to the cell item, so a
 * cell packed with weighted combat supplies weighs as much as its contents - you can't compress materiel
 * into a pocket cell to dodge the weight system. Fluid cells (or any cell holding no item keys) store no
 * item weight and abstain, resolving to the plain cell weight. Gated on {@code ae2}, so this plugin is
 * never loaded when AE2 is absent.
 *
 * <p>Deliberately references no fastutil type: AE2's {@code KeyCounter} entries are backed by the runtime
 * (Minecraft-provided) fastutil, whereas this mod shades + relocates its own fastutil. We iterate through
 * {@link Iterator} / {@link java.util.Map.Entry} (fastutil's {@code Object2LongMap.Entry} implements
 * {@code Map.Entry}), so the shadow relocation never rewrites a call that has to interop with AE2.
 */
@CompatPlugin(requiredMods = "ae2")
@SuppressWarnings("unused")
public final class Ae2StorageCellWeightPatch1201 implements IWeightCompatProvider {
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

        StorageCell inventory = StorageCells.getCellInventory(stack, null);
        if (inventory == null) {
            return OptionalDouble.empty(); // not an AE2 storage cell
        }

        double contents = 0.0D;
        Iterator<?> iterator = inventory.getAvailableStacks().iterator();
        while (iterator.hasNext()) {
            if (!(iterator.next() instanceof Map.Entry<?, ?> entry)) {
                continue;
            }
            if (entry.getKey() instanceof AEItemKey itemKey && entry.getValue() instanceof Number amount) {
                ItemStack unit = itemKey.toStack();
                if (!unit.isEmpty()) {
                    contents += WeightViews1201.stackWeight(unit, depth + 1) * amount.doubleValue();
                }
            }
        }

        return contents > EPSILON
            ? OptionalDouble.of(WeightViews1201.configuredWeightOf(stack, depth) + contents)
            : OptionalDouble.empty();
    }

    @Override
    public int getPriority() {
        // Item-specific, like the TACZ patches (300): an AE2 cell exposes no Forge IItemHandler, so the
        // generic nested-container provider (100) never matches it; run first so the contents-aware weight
        // is authoritative.
        return 300;
    }
}
