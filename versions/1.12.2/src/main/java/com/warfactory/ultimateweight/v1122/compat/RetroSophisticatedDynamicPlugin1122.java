package com.warfactory.ultimateweight.v1122.compat;

import com.warfactory.ultimateweight.api.CompatPlugin;
import com.warfactory.ultimateweight.api.WeightCompatContext;
import com.warfactory.ultimateweight.api.WeightCompatPlugin;
import com.warfactory.ultimateweight.v1122.RetroSophisticatedBackpackSupport1122;

/**
 * Marks Retro Sophisticated Backpacks as dynamic containers. Their contents live in a capability,
 * not the item tag, so the tag-hash weight cache would freeze and a single-slot delta cannot be
 * trusted - both are bypassed for these stacks. The contents themselves are weighed by
 * {@link RetroSophisticatedBackpackPatch1122}.
 */
@CompatPlugin(requiredMods = "retro_sophisticated_backpacks")
public final class RetroSophisticatedDynamicPlugin1122 implements WeightCompatPlugin {
    @Override
    public void register(WeightCompatContext context) {
        ((CompatContext1122) context).markDynamicContainer(RetroSophisticatedBackpackSupport1122::isBackpackStack);
    }
}
