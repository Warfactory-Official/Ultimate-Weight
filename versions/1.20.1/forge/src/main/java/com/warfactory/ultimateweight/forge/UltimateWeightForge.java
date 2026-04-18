package com.warfactory.ultimateweight.forge;

import com.warfactory.ultimateweight.api.WeightCompatRegistry;
import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.v1201.UltimateWeightConfigFile1201;
import com.warfactory.ultimateweight.v1201.UltimateWeight1201;
import com.warfactory.ultimateweight.v1201.compat.CompatibilityNestedWeightProvider1201;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.ModList;
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
        UltimateWeight1201.setStateListener(new UltimateWeightForgeStateHooks());
        UltimateWeightForgeNetworking.bootstrap();
        LOGGER.info("{} 1.20.1 Forge integration initialized.", UltimateWeightCommon.MOD_NAME);
    }
}
