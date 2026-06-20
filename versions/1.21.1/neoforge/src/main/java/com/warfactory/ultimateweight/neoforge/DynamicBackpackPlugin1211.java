package com.warfactory.ultimateweight.neoforge;

import com.warfactory.ultimateweight.api.CompatPlugin;
import com.warfactory.ultimateweight.api.WeightCompatContext;
import com.warfactory.ultimateweight.api.WeightCompatPlugin;
import com.warfactory.ultimateweight.v1211.compat.CompatContext1211;

/**
 * Marks Sophisticated/Traveler's backpacks as dynamic containers so their weight is not frozen by
 * the component cache and a single inventory-slot delta is not trusted for them - their contents are
 * capability/save-data backed and can change without the held stack's components changing.
 */
@CompatPlugin(anyOf = {"sophisticatedbackpacks", "travelersbackpack"})
public final class DynamicBackpackPlugin1211 implements WeightCompatPlugin {
    @Override
    public void register(WeightCompatContext context) {
        ((CompatContext1211) context).markDynamicContainer(BackpackSupport1211::isBackpack);
    }
}
