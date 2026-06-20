package com.warfactory.ultimateweight.lexforge;

import com.warfactory.ultimateweight.api.CompatPlugin;
import com.warfactory.ultimateweight.api.WeightCompatContext;
import com.warfactory.ultimateweight.api.WeightCompatPlugin;
import com.warfactory.ultimateweight.v1211.compat.CompatContext1211;

/**
 * Adds worn backpacks/curios to the weighed inventory. Active when either Curios or Traveler's
 * Backpack is present. Contents are weighed by {@link ForgeNestedWeightProvider1211} through the
 * Forge item-handler capability.
 */
@CompatPlugin(anyOf = {"curios", "travelersbackpack"})
public final class WornSlotsPlugin1211 implements WeightCompatPlugin {
    @Override
    public void register(WeightCompatContext context) {
        ((CompatContext1211) context).registerInventorySource(BackpackSupport1211::collectWorn);
    }
}
