package com.warfactory.ultimateweight.forge;

import com.warfactory.ultimateweight.api.CompatPlugin;
import com.warfactory.ultimateweight.api.WeightCompatContext;
import com.warfactory.ultimateweight.api.WeightCompatPlugin;
import com.warfactory.ultimateweight.v1201.compat.CompatContext1201;

/**
 * Marks Sophisticated/Traveler's backpacks as dynamic containers so their weight is not frozen by
 * the item-tag cache and a single inventory-slot delta is not trusted for them - their contents are
 * capability/save-data backed and can change without the held stack's tag changing.
 */
@CompatPlugin(anyOf = {"sophisticatedbackpacks", "travelersbackpack"})
public final class DynamicBackpackPlugin1201 implements WeightCompatPlugin {
    @Override
    public void register(WeightCompatContext context) {
        ((CompatContext1201) context).markDynamicContainer(BackpackSupport1201::isBackpack);
    }
}
