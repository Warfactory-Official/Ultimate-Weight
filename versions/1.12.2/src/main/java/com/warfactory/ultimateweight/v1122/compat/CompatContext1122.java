package com.warfactory.ultimateweight.v1122.compat;

import com.warfactory.ultimateweight.compat.AbstractWeightCompatContext;
import com.warfactory.ultimateweight.compat.ModPresenceChecker;
import com.warfactory.ultimateweight.v1122.WeightViews1122;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

/**
 * The 1.12.2 registration surface handed to {@link com.warfactory.ultimateweight.api.WeightCompatPlugin}s.
 * Adds the version-specific hooks on top of the shared weight-provider registration: extra worn
 * inventory sources (Baubles slots, a worn Traveler's Backpack and its cargo) and dynamic-container
 * marking, both backed by {@link WeightViews1122}.
 *
 * <p>Dynamic-container predicates are OR-composed so several plugins can each contribute one without
 * clobbering the single predicate {@code WeightViews1122} stores.
 */
public final class CompatContext1122 extends AbstractWeightCompatContext {
    private Predicate<ItemStack> dynamicContainers = stack -> false;

    public CompatContext1122(ModPresenceChecker modPresence) {
        super(modPresence);
    }

    public void registerInventorySource(WeightViews1122.InventorySource source) {
        WeightViews1122.registerInventorySource(source);
    }

    public void markDynamicContainer(Predicate<ItemStack> isDynamic) {
        if (isDynamic == null) {
            return;
        }
        Predicate<ItemStack> previous = dynamicContainers;
        dynamicContainers = stack -> previous.test(stack) || isDynamic.test(stack);
        WeightViews1122.setDynamicContainerPredicate(dynamicContainers);
    }
}
