package com.warfactory.ultimateweight.v1122.compat;

import com.hbm.weight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.core.WeightResolutionContext;
import com.warfactory.ultimateweight.v1122.WeightViews1122;
import java.util.Arrays;
import java.util.HashSet;
import java.util.OptionalDouble;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

@SuppressWarnings("unused")
public final class HbmStorageCrateWeightPatch1122 implements IWeightCompatProvider {
    private static final String PERSISTENT = "persistent";
    private static final double EPSILON = 0.000001D;
    private static final Set<String> CRATE_IDS = new HashSet<String>(Arrays.asList(
        "hbm:crete_iron",
        "hbm:crate_steel",
        "hbm:crate_desh",
        "hbm:crate_tungsten"
    ));

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

        if (stack.getItem().getRegistryName() == null || !CRATE_IDS.contains(stack.getItem().getRegistryName().toString())) {
            return OptionalDouble.empty();
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(PERSISTENT, 10)) {
            return OptionalDouble.empty();
        }

        NBTTagCompound persistent = tag.getCompoundTag(PERSISTENT);
        double total = 0.0D;
        for (String key : persistent.getKeySet()) {
            NBTBase rawEntry = persistent.getTag(key);
            if (!(rawEntry instanceof NBTTagCompound)) {
                continue;
            }

            ItemStack nested = new ItemStack((NBTTagCompound) rawEntry);
            if (!nested.isEmpty()) {
                total += WeightViews1122.stackWeight(nested, depth + 1);
            }
        }

        return total > EPSILON
            ? OptionalDouble.of(WeightViews1122.configuredWeightOf(stack, depth) + total)
            : OptionalDouble.empty();
    }

    @Override
    public int getPriority() {
        return 275;
    }
}
