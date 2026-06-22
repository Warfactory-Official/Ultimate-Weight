package com.warfactory.ultimateweight.fabric;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.compat.ModPresenceChecker;
import com.warfactory.ultimateweight.compat.WeightCompatBootstrap;
import com.warfactory.ultimateweight.runtime.UltimateWeightServices;
import com.warfactory.ultimateweight.v1211.UltimateWeight1211;
import com.warfactory.ultimateweight.v1211.UltimateWeightConfigFile1211;
import com.warfactory.ultimateweight.v1211.compat.CompatContext1211;
import com.warfactory.ultimateweight.v1211.compat.ItemNbtBridge1211;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class UltimateWeightFabric implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger(UltimateWeightCommon.MOD_ID);

    private static volatile MinecraftServer currentServer;

    @Override
    public void onInitialize() {
        UltimateWeightConfigFile1211.configure(FabricLoader.getInstance().getConfigDir());

        // Auto-discover @CompatPlugin classes (the shared common providers). The Forge-family loaders
        // add worn-slot/dynamic-backpack plugins that reach the Forge item-handler capability; on
        // Fabric those classes are simply absent, so only the loader-agnostic common providers
        // (generic nested container + storage drawers, both mod-gated) light up here.
        ModPresenceChecker mods = modId -> FabricLoader.getInstance().isModLoaded(modId);
        WeightCompatBootstrap.run(getClass().getClassLoader(), mods, new CompatContext1211(mods));

        UltimateWeight1211.setTransport(new UltimateWeightFabricTransport());
        UltimateWeight1211.setStateListener(new UltimateWeightFabricStateHooks());

        // 1.21.1 deserializes nested stacks from NBT via the registries; supply them from the running
        // server (covers dedicated + integrated). Without them, NBT-backed nested weight is skipped.
        ItemNbtBridge1211.setRegistryAccess(UltimateWeightFabric::activeRegistries);

        // Both sides must know the payload codecs before play; register them in the common entrypoint.
        UltimateWeightFabricNetworking.registerPayloadTypes();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> currentServer = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> currentServer = null);

        ServerTickEvents.END_SERVER_TICK.register(UltimateWeight1211::onServerTick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> UltimateWeight1211.onPlayerJoin(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> UltimateWeight1211.onPlayerLeave(handler.player));
        // Fires on both death respawn and end-return (alive == true), covering everything the NeoForge
        // PlayerEvent.Clone hook covers; the listener copies persisted stamina across the clone.
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> UltimateWeight1211.onPlayerClone(oldPlayer, newPlayer));

        UltimateWeightServices services = UltimateWeightCommon.bootstrap();
        LOGGER.info(
            "{} 1.21.1 Fabric integration initialized with {} exact rules, {} wildcard rules, {} dictionary rules.",
            UltimateWeightCommon.MOD_NAME,
            Integer.valueOf(services.config().resolverRules().exactCount()),
            Integer.valueOf(services.config().resolverRules().wildcardCount()),
            Integer.valueOf(services.config().resolverRules().matchCount())
        );
    }

    private static HolderLookup.Provider activeRegistries() {
        MinecraftServer server = currentServer;
        return server == null ? null : server.registryAccess();
    }
}
