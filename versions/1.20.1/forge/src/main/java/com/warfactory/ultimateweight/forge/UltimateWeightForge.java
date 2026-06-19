package com.warfactory.ultimateweight.forge;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.api.WeightCompatRegistry;
import com.warfactory.ultimateweight.v1201.UltimateWeight1201;
import com.warfactory.ultimateweight.v1201.UltimateWeightConfigFile1201;
import com.warfactory.ultimateweight.v1201.WeightViews1201;
import com.warfactory.ultimateweight.v1201.compat.CompatibilityNestedWeightProvider1201;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(UltimateWeightCommon.MOD_ID)
public final class UltimateWeightForge {
    private static final Logger LOGGER = LogManager.getLogger(UltimateWeightCommon.MOD_ID);

    public UltimateWeightForge() {
        UltimateWeightConfigFile1201.configure(FMLPaths.CONFIGDIR.get());
        WeightCompatRegistry.registerAll(
            CompatibilityNestedWeightProvider1201.create(
                (modId) -> ModList.get().isLoaded(modId)
            )
        );
        WeightCompatRegistry.register(new ForgeNestedWeightProvider1201());
        registerBackpackSupport();
        UltimateWeight1201.setStateListener(new UltimateWeightForgeStateHooks());
        UltimateWeightForgeNetworking.bootstrap();
        LOGGER.info("{} 1.20.1 Forge integration initialized.", UltimateWeightCommon.MOD_NAME);
    }

    private static void registerBackpackSupport() {
        ModList mods = ModList.get();
        boolean curios = mods.isLoaded("curios");
        boolean travelers = mods.isLoaded("travelersbackpack");
        boolean sophisticated = mods.isLoaded("sophisticatedbackpacks");
        if (curios || travelers) {
            WeightViews1201.registerInventorySource(BackpackSupport1201::collectWorn);
        }
        // Treat backpacks as dynamic containers: bypass the weight cache and force a full rescan on
        // their inventory deltas instead of trusting a numeric delta.
        if (sophisticated || travelers) {
            WeightViews1201.setDynamicContainerPredicate(BackpackSupport1201::isBackpack);
        }
    }
}
