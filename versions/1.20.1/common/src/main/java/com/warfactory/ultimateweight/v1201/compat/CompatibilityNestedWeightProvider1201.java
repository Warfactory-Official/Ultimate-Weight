package com.warfactory.ultimateweight.v1201.compat;

import com.hbm.weight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.compat.CompatibilityPatchLoader;
import com.warfactory.ultimateweight.compat.ModPresenceChecker;
import com.warfactory.ultimateweight.logging.WeightLoggers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CompatibilityNestedWeightProvider1201 {
    private static final WeightLoggers.WeightLogger LOGGER = WeightLoggers.component("compat");

    private static final List<CompatibilityPatchLoader.PatchSpec> PATCH_SPECS = Arrays.asList(
        new CompatibilityPatchLoader.PatchSpec(
            "gtceu",
            "com.warfactory.ultimateweight.v1201.compat.GregTechNestedWeightPatch1201"
        ),
        new CompatibilityPatchLoader.PatchSpec(
            "storagedrawers",
            "com.warfactory.ultimateweight.v1201.compat.StorageDrawersNestedWeightPatch1201"
        )
    );

    private CompatibilityNestedWeightProvider1201() {
    }

    public static List<IWeightCompatProvider> create(ModPresenceChecker modPresence) {
        ArrayList<IWeightCompatProvider> providers = new ArrayList<IWeightCompatProvider>();
        providers.add(new GenericNestedWeightPatch1201());
        providers.addAll(CompatibilityPatchLoader.load(modPresence, PATCH_SPECS, IWeightCompatProvider.class));

        if (UltimateWeightCommon.isDebugEnabled()) {
            LOGGER.info("1.20.1 compatibility weight providers active: {}", Integer.valueOf(providers.size()));
        }
        return providers;
    }
}
