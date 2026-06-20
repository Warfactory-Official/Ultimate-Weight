package com.warfactory.ultimateweight.v1201.compat;

import com.warfactory.ultimateweight.compat.AbstractWeightCompatContext;
import com.warfactory.ultimateweight.compat.ModPresenceChecker;
import com.warfactory.ultimateweight.v1201.WeightViews1201;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * The 1.20.1 registration surface handed to {@link com.warfactory.ultimateweight.api.WeightCompatPlugin}s.
 * Adds the version-specific hooks on top of the shared weight-provider registration: extra worn
 * inventory sources and dynamic-container marking, both backed by {@link WeightViews1201}.
 *
 * <p>Dynamic-container predicates are OR-composed here so multiple plugins can each contribute a
 * predicate without clobbering one another (the underlying {@code WeightViews1201} hook stores a
 * single predicate).
 */
public final class CompatContext1201 extends AbstractWeightCompatContext {
    private Predicate<ItemStack> dynamicContainers = stack -> false;

    public CompatContext1201(ModPresenceChecker modPresence) {
        super(modPresence);
    }

    /** Register a worn-item source (Curios slots, a natively-worn Traveler's Backpack, ...). */
    public void registerInventorySource(WeightViews1201.InventorySource source) {
        WeightViews1201.registerInventorySource(source);
    }

    /** Mark stacks matching {@code isDynamic} as dynamic containers (cache-bypassed). OR-composed. */
    public void markDynamicContainer(Predicate<ItemStack> isDynamic) {
        if (isDynamic == null) {
            return;
        }
        Predicate<ItemStack> previous = dynamicContainers;
        dynamicContainers = stack -> previous.test(stack) || isDynamic.test(stack);
        WeightViews1201.setDynamicContainerPredicate(dynamicContainers);
    }
}
