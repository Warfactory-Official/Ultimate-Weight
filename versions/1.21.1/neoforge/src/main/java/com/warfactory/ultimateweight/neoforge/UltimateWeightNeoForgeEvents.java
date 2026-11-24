package com.warfactory.ultimateweight.neoforge;

import com.warfactory.ultimateweight.v1211.UltimateWeight1211;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Game-bus event handlers for the NeoForge loader, registered manually from the entrypoint. */
public final class UltimateWeightNeoForgeEvents {
    private UltimateWeightNeoForgeEvents() {
    }

    public static void register(IEventBus gameBus) {
        gameBus.addListener(UltimateWeightNeoForgeEvents::onServerTickPre);
        gameBus.addListener(UltimateWeightNeoForgeEvents::onPlayerTick);
        gameBus.addListener(UltimateWeightNeoForgeEvents::onPlayerLogin);
        gameBus.addListener(UltimateWeightNeoForgeEvents::onPlayerLogout);
        gameBus.addListener(UltimateWeightNeoForgeEvents::onPlayerRespawn);
        gameBus.addListener(UltimateWeightNeoForgeEvents::onPlayerClone);
        gameBus.addListener(UltimateWeightNeoForgeEvents::onPlayerChangedDimension);
        gameBus.addListener(UltimateWeightNeoForgeEvents::onLivingFall);
    }

    private static void onServerTickPre(ServerTickEvent.Pre event) {
        UltimateWeight1211.onServerTickStart();
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UltimateWeight1211.onPlayerTick(player);
        }
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
