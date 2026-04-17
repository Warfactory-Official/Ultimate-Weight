package com.warfactory.ultimateweight.forge.client;

import com.warfactory.ultimateweight.UltimateWeightCommon;
import com.warfactory.ultimateweight.v1201.UltimateWeight1201;
import com.warfactory.ultimateweight.v1201.UltimateWeightConfigFile1201;
import com.warfactory.ultimateweight.v1201.client.UltimateWeightClient1201;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
    modid = UltimateWeightCommon.MOD_ID,
    bus = Mod.EventBusSubscriber.Bus.FORGE,
    value = Dist.CLIENT
)
public final class UltimateWeightForgeClientEvents {
    private UltimateWeightForgeClientEvents() {
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
            UltimateWeightClient1201.renderHud(event.getGuiGraphics());
        }
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        UltimateWeight1201.appendTooltip(event.getItemStack(), event.getToolTip(), event.getEntity());
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        UltimateWeight1201.resetClientState();
        UltimateWeightConfigFile1201.reloadFromDisk();
    }
}
