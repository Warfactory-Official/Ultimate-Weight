package com.warfactory.ultimateweight.v1201.compat;

import com.warfactory.ultimateweight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.core.WeightResolutionContext;
import com.warfactory.ultimateweight.v1201.WeightViews1201;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalDouble;

/**
 * Weight provider for the Superb Warfare ammo box ({@code superbwarfare:ammo_box}).
 *
 * <p>Unlike a normal stack-handler container, the ammo box does not store ItemStacks - it keeps an
 * integer count per ammo type directly on the stack's root tag (keys {@code HandgunAmmo},
 * {@code RifleAmmo}, {@code ShotgunAmmo}, {@code SniperAmmo}, {@code HeavyAmmo}), each corresponding
 * to a real ammo item. The stored weight is therefore {@code count * weight(ammo item)} summed over
 * the types, so the box's weight scales with how much ammo it holds.
 *
 * <p>Pure NBT + registry lookups, so it is safe to load whether or not Superb Warfare is present;
 * the counts live in the root tag, so the normal item-tag cache invalidates correctly when ammo
 * changes (no cache bypass needed).
 */
@SuppressWarnings("unused")
public final class SuperbWarfareAmmoBoxPatch1201 implements IWeightCompatProvider {
    private static final String AMMO_BOX_ID = "superbwarfare:ammo_box";
    private static final double EPSILON = 0.000001D;
    private static final String[][] AMMO_TYPES = {
        {"HandgunAmmo", "superbwarfare:handgun_ammo"},
        {"RifleAmmo", "superbwarfare:rifle_ammo"},
        {"ShotgunAmmo", "superbwarfare:shotgun_ammo"},
        {"SniperAmmo", "superbwarfare:sniper_ammo"},
        {"HeavyAmmo", "superbwarfare:heavy_ammo"}
    };

    @Override
    public OptionalDouble getUnitWeight(Object rawStack) {
        if (!(rawStack instanceof ItemStack stack) || stack.isEmpty()) {
            return OptionalDouble.empty();
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null || !AMMO_BOX_ID.equals(itemId.toString())) {
            return OptionalDouble.empty();
        }

        int depth = WeightResolutionContext.currentDepth();
        if (depth >= WeightViews1201.maxNestedDepth()) {
            return OptionalDouble.empty();
        }

        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return OptionalDouble.empty();
        }

        double total = 0.0D;
        for (String[] ammo : AMMO_TYPES) {
            int count = tag.getInt(ammo[0]);
            if (count <= 0) {
                continue;
            }
            ItemStack ammoStack = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(ammo[1])));
            if (ammoStack.isEmpty()) {
                continue;
            }
            total += WeightViews1201.stackWeight(ammoStack, depth + 1) * (double) count;
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
