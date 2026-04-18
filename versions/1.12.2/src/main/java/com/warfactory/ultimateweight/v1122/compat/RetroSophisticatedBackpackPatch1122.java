package com.warfactory.ultimateweight.v1122.compat;

import com.warfactory.ultimateweight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.core.WeightResolutionContext;
import com.warfactory.ultimateweight.v1122.WeightViews1122;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.OptionalDouble;

@SuppressWarnings("unused")
public class RetroSophisticatedBackpackPatch1122 implements IWeightCompatProvider {
    private static final double EPSILON = 0.000001D;

    @Override
    public OptionalDouble getUnitWeight(Object obj) {
        if (!(obj instanceof ItemStack)) return OptionalDouble.empty();


        ItemStack stack = (ItemStack) obj;
        if (stack.isEmpty() || !stack.hasTagCompound()) return OptionalDouble.empty();

        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) return OptionalDouble.empty();

        if (!nbt.hasKey("ForgeCaps", 10)) return OptionalDouble.empty();

        NBTTagCompound forgeCaps = nbt.getCompoundTag("ForgeCaps");
        NBTTagCompound parentCap = forgeCaps.getCompoundTag("Parent");

        if (!parentCap.hasKey("BackpackInventory", 10)) return OptionalDouble.empty();

        NBTTagCompound inventory = parentCap.getCompoundTag("BackpackInventory");
        NBTTagList items = inventory.getTagList("Items", 10);

        int depth = WeightResolutionContext.currentDepth();
        if (depth >= WeightViews1122.maxNestedDepth()) {
            return OptionalDouble.empty();
        }

        double total = 0.0;
        for (NBTBase key : items) {
            if (!(key instanceof NBTTagCompound)) {
                continue;
            }

            ItemStack nested = new ItemStack((NBTTagCompound) key);
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
        return 100;
    }
}
