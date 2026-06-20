package com.warfactory.ultimateweight.forge;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.compat.ModPresenceChecker;
import com.warfactory.ultimateweight.compat.WeightCompatBootstrap;
import com.warfactory.ultimateweight.v1201.UltimateWeight1201;
import com.warfactory.ultimateweight.v1201.UltimateWeightConfigFile1201;
import com.warfactory.ultimateweight.v1201.compat.CompatContext1201;
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
        // Discover every @CompatPlugin on the classpath (common + forge), gated by mod presence.
        // This replaces the old hand-maintained provider list and the imperative backpack wiring -
        // ForgeNestedWeightProvider1201, the nested-weight patches, and the worn/dynamic backpack
        // hooks are now all auto-discovered plugins.
        ModPresenceChecker mods = modId -> ModList.get().isLoaded(modId);
        WeightCompatBootstrap.run(getClass().getClassLoader(), mods, new CompatContext1201(mods));
        UltimateWeight1201.setStateListener(new UltimateWeightForgeStateHooks());
        UltimateWeightForgeNetworking.bootstrap();
        LOGGER.info("{} 1.20.1 Forge integration initialized.", UltimateWeightCommon.MOD_NAME);
    }
}
