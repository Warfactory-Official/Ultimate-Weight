package com.warfactory.ultimateweight.neoforge;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.compat.ModPresenceChecker;
import com.warfactory.ultimateweight.compat.WeightCompatBootstrap;
import com.warfactory.ultimateweight.neoforge.attachment.WeightAttachments;
import com.warfactory.ultimateweight.neoforge.client.UltimateWeightNeoForgeClientEvents;
import com.warfactory.ultimateweight.runtime.UltimateWeightServices;
import com.warfactory.ultimateweight.v1211.UltimateWeight1211;
import com.warfactory.ultimateweight.v1211.UltimateWeightConfigFile1211;
import com.warfactory.ultimateweight.v1211.compat.CompatContext1211;
import com.warfactory.ultimateweight.v1211.compat.ItemNbtBridge1211;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(UltimateWeightCommon.MOD_ID)
public final class UltimateWeightNeoForge {
    private static final Logger LOGGER = LogManager.getLogger(UltimateWeightCommon.MOD_ID);

    public UltimateWeightNeoForge(IEventBus modEventBus) {
        UltimateWeightConfigFile1211.configure(FMLPaths.CONFIGDIR.get());

        // Discover every @CompatPlugin on the classpath (common + neoforge), gated by mod presence -
        // the nested-weight provider, the backpack worn/dynamic hooks and the storage-drawers patch
        // are all auto-discovered plugins.
        ModPresenceChecker mods = modId -> ModList.get().isLoaded(modId);
        WeightCompatBootstrap.run(getClass().getClassLoader(), mods, new CompatContext1211(mods));

        UltimateWeight1211.setStateListener(new UltimateWeightNeoForgeStateHooks());
        // 1.21.1 deserializes nested stacks from NBT via the registries; supply them from the running
        // server (covers dedicated + integrated). Without them, NBT-backed nested weight is skipped.
        ItemNbtBridge1211.setRegistryAccess(UltimateWeightNeoForge::activeRegistries);

        WeightAttachments.ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(UltimateWeightNeoForgeNetworking::register);
        UltimateWeightNeoForgeNetworking.installTransport();

        UltimateWeightNeoForgeEvents.register(NeoForge.EVENT_BUS);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            UltimateWeightNeoForgeClientEvents.register();
        }

        UltimateWeightServices services = UltimateWeightCommon.bootstrap();
        LOGGER.info(
            "{} 1.21.1 NeoForge integration initialized with {} exact rules, {} wildcard rules, {} dictionary rules.",
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
