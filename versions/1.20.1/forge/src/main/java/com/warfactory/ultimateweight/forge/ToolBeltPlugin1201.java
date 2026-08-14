package com.warfactory.ultimateweight.forge;

import com.warfactory.ultimateweight.api.CompatPlugin;
import com.warfactory.ultimateweight.api.WeightCompatContext;
import com.warfactory.ultimateweight.api.WeightCompatPlugin;
import com.warfactory.ultimateweight.v1201.compat.CompatContext1201;


@CompatPlugin(requiredMods = "toolbelt")
public final class ToolBeltPlugin1201 implements WeightCompatPlugin {
    @Override
    public void register(WeightCompatContext context) {
        ((CompatContext1201) context).registerInventorySource(ToolBeltSupport1201::collectWorn);
    }
}
