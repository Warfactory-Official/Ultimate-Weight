package com.warfactory.ultimateweight.lexforge;

import com.warfactory.ultimateweight.v1211.UltimateWeight1211;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.server.ServerLifecycleHooks;

/** Game-bus event handlers for the LexForge loader, registered manually from the entrypoint. */
public final class UltimateWeightLexForgeEvents {
    private UltimateWeightLexForgeEvents() {
    }

    public static void register(IEventBus gameBus) {
        gameBus.addListener(UltimateWeightLexForgeEvents::onServerTick);
        gameBus.addListener(UltimateWeightLexForgeEvents::onPlayerLogin);
        gameBus.addListener(UltimateWeightLexForgeEvents::onPlayerLogout);
        gameBus.addListener(UltimateWeightLexForgeEvents::onPlayerRespawn);
        gameBus.addListener(UltimateWeightLexForgeEvents::onPlayerClone);
        gameBus.addListener(UltimateWeightLexForgeEvents::onPlayerChangedDimension);
        gameBus.addListener(UltimateWeightLexForgeEvents::onLivingFall);
    }

    private static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            UltimateWeight1211.onServerTick(ServerLifecycleHooks.getCurrentServer());
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
