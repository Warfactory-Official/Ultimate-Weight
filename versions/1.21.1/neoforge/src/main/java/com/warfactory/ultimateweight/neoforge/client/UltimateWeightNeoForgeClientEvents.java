package com.warfactory.ultimateweight.neoforge.client;

import com.warfactory.ultimateweight.v1211.UltimateWeight1211;
import com.warfactory.ultimateweight.v1211.UltimateWeightConfigFile1211;
import com.warfactory.ultimateweight.v1211.client.UltimateWeightClient1211;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Client-only game-bus handlers (HUD, tooltip, logout reset). Registered from the entrypoint only
 * when running on the physical client, so this class is never loaded on a dedicated server.
 */
public final class UltimateWeightNeoForgeClientEvents {
    private UltimateWeightNeoForgeClientEvents() {
    }

    public static void register() {
        IEventBus gameBus = NeoForge.EVENT_BUS;
        gameBus.addListener(UltimateWeightNeoForgeClientEvents::onRenderGui);
        gameBus.addListener(UltimateWeightNeoForgeClientEvents::onTooltip);
        gameBus.addListener(UltimateWeightNeoForgeClientEvents::onClientLogout);
    }

    private static void onRenderGui(RenderGuiEvent.Post event) {
        UltimateWeightClient1211.renderHud(event.getGuiGraphics());
    }

    private static void onTooltip(ItemTooltipEvent event) {
        UltimateWeight1211.appendTooltip(event.getItemStack(), event.getToolTip(), event.getEntity());
    }

    private static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        UltimateWeight1211.resetClientState();
        UltimateWeightConfigFile1211.reloadFromDisk();
    }
}
