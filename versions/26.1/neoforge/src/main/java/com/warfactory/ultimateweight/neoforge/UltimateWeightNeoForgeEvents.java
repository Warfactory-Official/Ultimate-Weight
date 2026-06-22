package com.warfactory.ultimateweight.neoforge;

import com.warfactory.ultimateweight.v1211.UltimateWeight1211;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Game-bus event handlers for the NeoForge loader, registered manually from the entrypoint. */
public final class UltimateWeightNeoForgeEvents {
    private UltimateWeightNeoForgeEvents() {
    }

    public static void register(IEventBus gameBus) {
        gameBus.addListener(UltimateWeightNeoForgeEvents::onServerTick);
        gameBus.addListener(UltimateWeightNeoForgeEvents::onPlayerLogin);
        gameBus.addListener(UltimateWeightNeoForgeEvents::onPlayerLogout);
        gameBus.addListener(UltimateWeightNeoForgeEvents::onPlayerRespawn);
        gameBus.addListener(UltimateWeightNeoForgeEvents::onPlayerClone);
        gameBus.addListener(UltimateWeightNeoForgeEvents::onPlayerChangedDimension);
        gameBus.addListener(UltimateWeightNeoForgeEvents::onLivingFall);
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        UltimateWeight1211.onServerTick(event.getServer());
    }

    private static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UltimateWeight1211.onPlayerJoin(player);
        }
    }

    private static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UltimateWeight1211.onPlayerLeave(player);
        }
    }

    private static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UltimateWeight1211.onPlayerJoin(player);
        }
    }

    private static void onPlayerClone(PlayerEvent.Clone event) {
        UltimateWeight1211.onPlayerClone(event.getOriginal(), event.getEntity());
    }

    private static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UltimateWeight1211.onPlayerJoin(player);
        }
    }

    private static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            event.setDamageMultiplier(
                UltimateWeight1211.adjustFallDamageMultiplier(player, event.getDamageMultiplier())
            );
        }
    }
}
