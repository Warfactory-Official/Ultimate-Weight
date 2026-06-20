package com.warfactory.ultimateweight.neoforge;

import com.warfactory.ultimateweight.api.CompatPlugin;
import com.warfactory.ultimateweight.api.WeightCompatContext;
import com.warfactory.ultimateweight.api.WeightCompatPlugin;
import com.warfactory.ultimateweight.v1211.compat.CompatContext1211;

/**
 * Adds worn backpacks/curios to the weighed inventory. Active when either Curios (extra equip slots)
 * or Traveler's Backpack (worn in its own handler) is present, since both put items on the player
 * that the vanilla inventory enumeration misses. The actual contents are weighed by
 * {@link NeoForgeNestedWeightProvider1211} through the standard item-handler capability.
 */
@CompatPlugin(anyOf = {"curios", "travelersbackpack"})
public final class WornSlotsPlugin1211 implements WeightCompatPlugin {
    @Override
    public void register(WeightCompatContext context) {
        ((CompatContext1211) context).registerInventorySource(BackpackSupport1211::collectWorn);
    }
}
