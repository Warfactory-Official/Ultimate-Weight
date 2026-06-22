package com.warfactory.ultimateweight.fabric;

import com.warfactory.ultimateweight.fabric.UltimateWeightFabricNetworking.ConfigFragmentPayload;
import com.warfactory.ultimateweight.fabric.UltimateWeightFabricNetworking.StaminaUpdatePayload;
import com.warfactory.ultimateweight.fabric.UltimateWeightFabricNetworking.WeightUpdatePayload;
import com.warfactory.ultimateweight.v1211.WeightSyncTransport1211;
import com.warfactory.ultimateweight.v1211.network.ConfigFragmentPacket1211;
import com.warfactory.ultimateweight.v1211.network.StaminaUpdatePacket1211;
import com.warfactory.ultimateweight.v1211.network.WeightUpdatePacket1211;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

/** Server-side transport: wraps each shared packet in its payload and sends it to the player. */
public final class UltimateWeightFabricTransport implements WeightSyncTransport1211 {
    @Override
    public void sendConfigFragment(ServerPlayer player, ConfigFragmentPacket1211 packet) {
        ServerPlayNetworking.send(player, new ConfigFragmentPayload(packet));
    }

    @Override
    public void sendWeightUpdate(ServerPlayer player, WeightUpdatePacket1211 packet) {
        ServerPlayNetworking.send(player, new WeightUpdatePayload(packet));
    }

    @Override
    public void sendStaminaUpdate(ServerPlayer player, StaminaUpdatePacket1211 packet) {
        ServerPlayNetworking.send(player, new StaminaUpdatePayload(packet));
    }
}
