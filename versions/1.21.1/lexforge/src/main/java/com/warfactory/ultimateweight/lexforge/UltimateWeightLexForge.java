package com.warfactory.ultimateweight.lexforge;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.compat.ModPresenceChecker;
import com.warfactory.ultimateweight.compat.WeightCompatBootstrap;
import com.warfactory.ultimateweight.lexforge.client.UltimateWeightLexForgeClientEvents;
import com.warfactory.ultimateweight.runtime.UltimateWeightServices;
import com.warfactory.ultimateweight.v1211.UltimateWeight1211;
import com.warfactory.ultimateweight.v1211.UltimateWeightConfigFile1211;
import com.warfactory.ultimateweight.v1211.compat.CompatContext1211;
import com.warfactory.ultimateweight.v1211.compat.ItemNbtBridge1211;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(UltimateWeightCommon.MOD_ID)
public final class UltimateWeightLexForge {
    private static final Logger LOGGER = LogManager.getLogger(UltimateWeightCommon.MOD_ID);

    public UltimateWeightLexForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        UltimateWeightConfigFile1211.configure(FMLPaths.CONFIGDIR.get());

        // Discover every @CompatPlugin on the classpath (common + lexforge), gated by mod presence.
        ModPresenceChecker mods = modId -> ModList.get().isLoaded(modId);
        WeightCompatBootstrap.run(getClass().getClassLoader(), mods, new CompatContext1211(mods));

        UltimateWeight1211.setStateListener(new UltimateWeightLexForgeStateHooks());
        // 1.21.1 deserializes nested stacks from NBT via the registries; supply them from the running
        // server. Without them, NBT-backed nested weight is skipped (graceful).
        ItemNbtBridge1211.setRegistryAccess(UltimateWeightLexForge::activeRegistries);

        UltimateWeightLexForgeNetworking.bootstrap();
        UltimateWeightLexForgeEvents.register(MinecraftForge.EVENT_BUS);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            UltimateWeightLexForgeClientEvents.register(modEventBus);
        }

        UltimateWeightServices services = UltimateWeightCommon.bootstrap();
        LOGGER.info(
            "{} 1.21.1 LexForge integration initialized with {} exact rules, {} wildcard rules, {} dictionary rules.",
            UltimateWeightCommon.MOD_NAME,
            Integer.valueOf(services.config().resolverRules().exactCount()),
            Integer.valueOf(services.config().resolverRules().wildcardCount()),
            Integer.valueOf(services.config().resolverRules().matchCount())
        );
    }

    private static HolderLookup.Provider activeRegistries() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : server.registryAccess();
    }
}
