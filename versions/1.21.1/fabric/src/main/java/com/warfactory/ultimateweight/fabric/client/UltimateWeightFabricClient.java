package com.warfactory.ultimateweight.fabric.client;

import com.warfactory.ultimateweight.v1201.UltimateWeight1201;
import com.warfactory.ultimateweight.v1201.UltimateWeightConfigFile1201;
import com.warfactory.ultimateweight.v1201.client.UltimateWeightClient1201;
import com.warfactory.ultimateweight.v1201.network.ConfigFragmentPacket1201;
import com.warfactory.ultimateweight.v1201.network.StaminaUpdatePacket1201;
import com.warfactory.ultimateweight.v1201.network.WeightUpdatePacket1201;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public final class UltimateWeightFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
            UltimateWeight1201.CONFIG_FRAGMENT_ID,
            (client, handler, buffer, sender) -> {
                ConfigFragmentPacket1201 packet = ConfigFragmentPacket1201.decode(buffer);
                client.execute(() -> UltimateWeight1201.receiveConfigFragment(packet));
            }
        );
        ClientPlayNetworking.registerGlobalReceiver(
            UltimateWeight1201.WEIGHT_UPDATE_ID,
            (client, handler, buffer, sender) -> {
                WeightUpdatePacket1201 packet = WeightUpdatePacket1201.decode(buffer);
                client.execute(() -> UltimateWeight1201.receiveWeightUpdate(packet));
            }
        );
        ClientPlayNetworking.registerGlobalReceiver(
            UltimateWeight1201.STAMINA_UPDATE_ID,
            (client, handler, buffer, sender) -> {
                StaminaUpdatePacket1201 packet = StaminaUpdatePacket1201.decode(buffer);
                client.execute(() -> UltimateWeight1201.receiveStaminaUpdate(packet));
            }
        );

        ItemTooltipCallback.EVENT.register((stack, context, lines) -> UltimateWeight1201.appendTooltip(stack, lines));
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> UltimateWeightClient1201.renderHud(graphics));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            UltimateWeight1201.resetClientState();
            UltimateWeightConfigFile1201.reloadFromDisk();
        });
    }
}
