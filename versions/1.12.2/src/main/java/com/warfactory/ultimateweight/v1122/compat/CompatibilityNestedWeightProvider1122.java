package com.warfactory.ultimateweight.v1122.compat;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.api.IWeightCompatProvider;
import com.warfactory.ultimateweight.compat.CompatibilityPatchLoader;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CompatibilityNestedWeightProvider1122 {
    private static final Logger LOGGER = LogManager.getLogger(UltimateWeightCommon.MOD_ID);

    private static final List<CompatibilityPatchLoader.PatchSpec> PATCH_SPECS = Arrays.asList(
            new CompatibilityPatchLoader.PatchSpec(
                    "gregtech",
                    "com.warfactory.ultimateweight.v1122.compat.GregTechNestedWeightPatch1122"
            ),
            new CompatibilityPatchLoader.PatchSpec(
                    "hbm",
                    "com.warfactory.ultimateweight.v1122.compat.HbmStorageCrateWeightPatch1122"
            ),
            new CompatibilityPatchLoader.PatchSpec(
                    "retro_sophisticated_backpacks",
                    "com.warfactory.ultimateweight.v1122.compat.RetroSophisticatedBackpackPatch1122"
            ),
            new CompatibilityPatchLoader.PatchSpec(
                    "travelersbackpack",
                    "com.warfactory.ultimateweight.v1122.compat.TravelersBackpackWeightPatch1122"
            ),
            new CompatibilityPatchLoader.PatchSpec(
                    "storagedrawers",
                    "com.warfactory.ultimateweight.v1122.compat.StorageDrawersNestedWeightPatch1122"
            )


    );

    private CompatibilityNestedWeightProvider1122() {
    }

    public static List<IWeightCompatProvider> create() {
        ArrayList<IWeightCompatProvider> providers = new ArrayList<IWeightCompatProvider>();
        providers.add(new GenericNestedWeightPatch1122());
        providers.addAll(
                CompatibilityPatchLoader.load(
                        Loader::isModLoaded,
                        PATCH_SPECS,
                        IWeightCompatProvider.class
                )
        );

        if (UltimateWeightCommon.isDebugEnabled()) {
            LOGGER.info(
                    "1.12.2 compatibility weight providers active: {}",
                    providers.size()
            );
        }
        return providers;
    }
}
