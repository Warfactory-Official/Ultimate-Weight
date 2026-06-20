package com.warfactory.ultimateweight.forge;

import com.warfactory.ultimateweight.api.CompatPlugin;
import com.warfactory.ultimateweight.api.WeightCompatContext;
import com.warfactory.ultimateweight.api.WeightCompatPlugin;
import com.warfactory.ultimateweight.v1201.compat.CompatContext1201;

/**
 * Adds worn backpacks/curios to the weighed inventory. Active when either Curios (extra equip slots)
 * or Traveler's Backpack (worn in its own capability) is present, since both put items on the player
 * that the vanilla inventory enumeration misses. The actual contents are weighed by
 * {@link ForgeNestedWeightProvider1201} through the standard item-handler capability.
 */
@CompatPlugin(anyOf = {"curios", "travelersbackpack"})
public final class WornSlotsPlugin1201 implements WeightCompatPlugin {
    @Override
    public void register(WeightCompatContext context) {
        ((CompatContext1201) context).registerInventorySource(BackpackSupport1201::collectWorn);
    }
}
