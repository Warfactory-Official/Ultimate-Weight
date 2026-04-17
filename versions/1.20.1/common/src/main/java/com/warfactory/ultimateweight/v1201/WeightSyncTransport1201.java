package com.warfactory.ultimateweight.v1201;

import com.warfactory.ultimateweight.v1201.network.ConfigFragmentPacket1201;
import com.warfactory.ultimateweight.v1201.network.StaminaUpdatePacket1201;
import com.warfactory.ultimateweight.v1201.network.WeightUpdatePacket1201;
import net.minecraft.server.level.ServerPlayer;

public interface WeightSyncTransport1201 {
    WeightSyncTransport1201 NOOP = new WeightSyncTransport1201() {
        @Override
        public void sendConfigFragment(ServerPlayer player, ConfigFragmentPacket1201 packet) {
        }

        @Override
        public void sendWeightUpdate(ServerPlayer player, WeightUpdatePacket1201 packet) {
        }

        @Override
        public void sendStaminaUpdate(ServerPlayer player, StaminaUpdatePacket1201 packet) {
        }
    };

    void sendConfigFragment(ServerPlayer player, ConfigFragmentPacket1201 packet);

    void sendWeightUpdate(ServerPlayer player, WeightUpdatePacket1201 packet);

    void sendStaminaUpdate(ServerPlayer player, StaminaUpdatePacket1201 packet);
}
