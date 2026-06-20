package com.warfactory.ultimateweight.v1122.compat;

import com.warfactory.ultimateweight.api.CompatPlugin;
import com.warfactory.ultimateweight.api.WeightCompatContext;
import com.warfactory.ultimateweight.api.WeightCompatPlugin;
import com.warfactory.ultimateweight.v1122.TravelersBackpackSupport1122;
import net.minecraft.item.ItemStack;

/**
 * Handles a Traveler's Backpack worn in its own capability slot - invisible to the vanilla
 * inventory. The worn backpack item is counted at its base weight only; its cargo lives in the
 * player capability (not the item tag), so it is enumerated separately as cargo. A loose backpack in
 * the main inventory is handled instead by {@link TravelersBackpackWeightPatch1122}, which reads its
 * NBT contents.
 *
 * <p>Also marks Traveler's Backpacks as dynamic containers so equipping/unequipping one forces a
 * full rescan rather than trusting a single-slot delta (the worn one moves through an invisible
 * capability slot).
 */
@CompatPlugin(requiredMods = "travelersbackpack")
public final class TravelersWornPlugin1122 implements WeightCompatPlugin {
    @Override
    public void register(WeightCompatContext context) {
        CompatContext1122 ctx = (CompatContext1122) context;
        ctx.registerInventorySource((player, sink) -> {
            ItemStack worn = TravelersBackpackSupport1122.equippedBackpack(player);
            if (!worn.isEmpty()) {
                sink.addWornBase(worn);
            }
            for (ItemStack stack : TravelersBackpackSupport1122.contents(player)) {
                sink.addCargo(stack);
            }
        });
        ctx.markDynamicContainer(TravelersBackpackSupport1122::isBackpackStack);
    }
}
