package com.warfactory.ultimateweight.lexforge.client;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.v1211.UltimateWeight1211;
import com.warfactory.ultimateweight.v1211.UltimateWeightConfigFile1211;
import com.warfactory.ultimateweight.v1211.client.UltimateWeightClient1211;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Client-only handlers (HUD, tooltip, logout reset). The HUD is added through Forge 1.21.1's
 * {@link AddGuiOverlayLayersEvent} (mod bus) + {@code ForgeLayeredDraw}; the tooltip and logout are
 * game-bus events. Registered from the entrypoint only on the physical client.
 */
public final class UltimateWeightLexForgeClientEvents {
    private static final ResourceLocation WEIGHT_HUD_LAYER =
        ResourceLocation.fromNamespaceAndPath(UltimateWeightCommon.MOD_ID, "weight_hud");

    private UltimateWeightLexForgeClientEvents() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(UltimateWeightLexForgeClientEvents::onAddGuiOverlayLayers);
        MinecraftForge.EVENT_BUS.addListener(UltimateWeightLexForgeClientEvents::onTooltip);
        MinecraftForge.EVENT_BUS.addListener(UltimateWeightLexForgeClientEvents::onClientLogout);
    }

    private static void onAddGuiOverlayLayers(AddGuiOverlayLayersEvent event) {
        event.getLayeredDraw().add(
            WEIGHT_HUD_LAYER,
            (LayeredDraw.Layer) (guiGraphics, deltaTracker) -> UltimateWeightClient1211.renderHud(guiGraphics)
        );
    }

    private static void onTooltip(ItemTooltipEvent event) {
        UltimateWeight1211.appendTooltip(event.getItemStack(), event.getToolTip(), event.getEntity());
    }

    private static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        UltimateWeight1211.resetClientState();
        UltimateWeightConfigFile1211.reloadFromDisk();
    }
}
