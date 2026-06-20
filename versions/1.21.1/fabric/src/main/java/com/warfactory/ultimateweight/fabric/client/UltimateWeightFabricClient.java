package com.warfactory.ultimateweight.fabric.client;

import com.warfactory.ultimateweight.fabric.UltimateWeightFabricNetworking.ConfigFragmentPayload;
import com.warfactory.ultimateweight.fabric.UltimateWeightFabricNetworking.StaminaUpdatePayload;
import com.warfactory.ultimateweight.fabric.UltimateWeightFabricNetworking.WeightUpdatePayload;
import com.warfactory.ultimateweight.v1211.UltimateWeight1211;
import com.warfactory.ultimateweight.v1211.UltimateWeightConfigFile1211;
import com.warfactory.ultimateweight.v1211.client.UltimateWeightClient1211;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;

/**
 * Client-only entrypoint: the modern payload receivers, the weight tooltip, the HUD and the logout
 * reset. Receivers hop to the client thread before touching the shared client state. The payload
 * codecs themselves are registered on the common entrypoint
 * ({@link com.warfactory.ultimateweight.fabric.UltimateWeightFabricNetworking#registerPayloadTypes()}).
 */
public final class UltimateWeightFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
            ConfigFragmentPayload.TYPE,
            (payload, context) -> context.client().execute(() -> UltimateWeight1211.receiveConfigFragment(payload.packet()))
        );
        ClientPlayNetworking.registerGlobalReceiver(
            WeightUpdatePayload.TYPE,
            (payload, context) -> context.client().execute(() -> UltimateWeight1211.receiveWeightUpdate(payload.packet()))
        );
        ClientPlayNetworking.registerGlobalReceiver(
            StaminaUpdatePayload.TYPE,
            (payload, context) -> context.client().execute(() -> UltimateWeight1211.receiveStaminaUpdate(payload.packet()))
        );

        // 1.21.1 ItemTooltipCallback adds the Item.TooltipContext + TooltipFlag params; we only need
        // the stack and the line list.
        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipFlag, lines) ->
            UltimateWeight1211.appendTooltip(stack, lines, Minecraft.getInstance().player));
        HudRenderCallback.EVENT.register((graphics, deltaTracker) -> UltimateWeightClient1211.renderHud(graphics));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            UltimateWeight1211.resetClientState();
            UltimateWeightConfigFile1211.reloadFromDisk();
        });
    }
}
