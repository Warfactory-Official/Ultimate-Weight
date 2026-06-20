package com.warfactory.ultimateweight.v1122.compat;

import com.warfactory.ultimateweight.api.CompatPlugin;
import com.warfactory.ultimateweight.api.WeightCompatContext;
import com.warfactory.ultimateweight.api.WeightCompatPlugin;
import com.warfactory.ultimateweight.v1122.BaublesSupport1122;
import com.warfactory.ultimateweight.v1122.WeightViews1122;
import net.minecraft.item.ItemStack;

/**
 * Counts every equipped Baubles item toward the player's weight (and equipment bonuses). Baubles
 * live in their own slots outside the vanilla inventory, so without this they would be invisible to
 * the weight system.
 */
@CompatPlugin(requiredMods = "baubles")
public final class BaublesWornPlugin1122 implements WeightCompatPlugin {
    @Override
    public void register(WeightCompatContext context) {
        ((CompatContext1122) context).registerInventorySource((player, sink) -> {
            for (ItemStack stack : BaublesSupport1122.equipped(player)) {
                sink.addWorn(stack);
            }
        });
    }
}
