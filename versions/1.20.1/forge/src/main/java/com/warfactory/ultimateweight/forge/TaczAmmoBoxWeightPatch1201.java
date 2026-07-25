package com.warfactory.ultimateweight.forge;

import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.warfactory.ultimateweight.api.CompatPlugin;
import com.warfactory.ultimateweight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.core.WeightResolutionContext;
import com.warfactory.ultimateweight.v1201.WeightViews1201;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalDouble;


@CompatPlugin(requiredMods = "tacz")
@SuppressWarnings("unused")
public final class TaczAmmoBoxWeightPatch1201 implements IWeightCompatProvider {
    private static final double EPSILON = 0.000001D;

    @Override
    public OptionalDouble getUnitWeight(Object rawStack) {
        if (!(rawStack instanceof ItemStack stack) || stack.isEmpty()) {
            return OptionalDouble.empty();
        }

        if (!(stack.getItem() instanceof IAmmoBox box)) {
            return OptionalDouble.empty();
        }

        // Creative / all-type-creative boxes report Integer.MAX_VALUE rounds and hold no real payload;
        // abstain so they resolve to the plain tacz:ammo_box item weight (and never overflow).
        if (box.isCreative(stack) || box.isAllTypeCreative(stack)) {
            return OptionalDouble.empty();
        }

        int count = box.getAmmoCount(stack);
        ResourceLocation ammoId = box.getAmmoId(stack);
        if (count <= 0 || ammoId == null) {
            return OptionalDouble.empty();
        }

        int depth = WeightResolutionContext.currentDepth();
        if (depth >= WeightViews1201.maxNestedDepth()) {
            return OptionalDouble.empty();
        }

        // Weigh the stored rounds as if loose: build one real round of this type and resolve it, so the
        // per-round weight matches TaczAmmoWeightPatch (AmmoId override, else tacz:ammo, else default).
        ItemStack round = AmmoItemBuilder.create().setId(ammoId).setCount(1).build();
        if (round.isEmpty()) {
            return OptionalDouble.empty();
        }

        double contents = WeightViews1201.stackWeight(round, depth + 1) * (double) count;
        return contents > EPSILON
            ? OptionalDouble.of(WeightViews1201.configuredWeightOf(stack, depth) + contents)
            : OptionalDouble.empty();
    }

    @Override
    public int getPriority() {
        // Consulted early alongside the other item-specific TACZ patches (guns and ammo = 300). The box
        // stores no ItemStacks, so the generic nested-container provider (100) never matches it; running
        // first here keeps the contents-aware result authoritative.
        return 300;
    }
}
